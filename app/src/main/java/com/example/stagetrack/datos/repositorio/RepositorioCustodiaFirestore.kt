package com.example.stagetrack.datos.repositorio

import android.util.Log
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.datos.local.dao.DaoCustodia
import com.example.stagetrack.datos.local.dao.DaoReporteDano
import com.example.stagetrack.datos.local.entidad.EntidadReporteDano
import com.example.stagetrack.datos.local.entidad.EntidadTransferenciaCustodia
import com.example.stagetrack.datos.mapeadores.MapeadorCustodia
import com.example.stagetrack.dominio.modelo.ReporteDano
import com.example.stagetrack.dominio.modelo.TransferenciaCustodia
import com.example.stagetrack.dominio.repositorio.RepositorioCustodia
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de transferencias de custodia y reportes con sincronización híbrida (Room + Cloud Firestore).
 */
class RepositorioCustodiaFirestore(
    private val daoCustodia: DaoCustodia,
    private val daoReporteDano: DaoReporteDano,
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : RepositorioCustodia {

    private val coleccionTransferencias = firestore.collection("transferencias")
    private val coleccionReportesDano = firestore.collection("reportes_dano")

    init {
        try {
            coleccionTransferencias.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("RepositorioCustodiaFirestore", "Aviso en listener de transferencias: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    scope.launch {
                        val entidades = snapshot.documents.mapNotNull { doc ->
                            try {
                                EntidadTransferenciaCustodia(
                                    id = doc.id,
                                    idEquipo = doc.getString("idEquipo") ?: "",
                                    idUsuarioEmisor = doc.getString("idUsuarioEmisor") ?: "",
                                    idUsuarioReceptor = doc.getString("idUsuarioReceptor") ?: "",
                                    fechaHoraTimestamp = doc.getLong("fechaHoraTimestamp") ?: System.currentTimeMillis(),
                                    uriFotografiaEvidencia = doc.getString("uriFotografiaEvidencia") ?: "",
                                    hashSha256Evidencia = doc.getString("hashSha256Evidencia") ?: "",
                                    latitudGps = doc.getDouble("latitudGps"),
                                    longitudGps = doc.getDouble("longitudGps"),
                                    notas = doc.getString("notas"),
                                    sincronizado = true
                                )
                            } catch (_: Exception) {
                                null
                            }
                        }
                        entidades.forEach { daoCustodia.insertarTransferencia(it) }
                    }
                }
            }

            coleccionReportesDano.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("RepositorioCustodiaFirestore", "Aviso en listener de reportes de daño: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    scope.launch {
                        val entidades = snapshot.documents.mapNotNull { doc ->
                            try {
                                EntidadReporteDano(
                                    id = doc.id,
                                    idEquipo = doc.getString("idEquipo") ?: "",
                                    idUsuarioReportador = doc.getString("idUsuarioReportador") ?: "",
                                    fechaHoraTimestamp = doc.getLong("fechaHoraTimestamp") ?: System.currentTimeMillis(),
                                    descripcionDano = doc.getString("descripcionDano") ?: "",
                                    severidad = doc.getString("severidad") ?: "MODERADO",
                                    uriFotografiaEvidencia = doc.getString("uriFotografiaEvidencia") ?: "",
                                    hashSha256Evidencia = doc.getString("hashSha256Evidencia") ?: "",
                                    sincronizado = true
                                )
                            } catch (_: Exception) {
                                null
                            }
                        }
                        entidades.forEach { daoReporteDano.insertarReporteDano(it) }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RepositorioCustodiaFirestore", "Error al conectar escuchadores de custodia: ${e.message}", e)
        }
    }

    override fun obtenerHistorialCustodia(idEquipo: String): Flow<List<TransferenciaCustodia>> {
        return daoCustodia.obtenerHistorialCustodia(idEquipo).map { lista ->
            lista.map { MapeadorCustodia.transferenciaADominio(it) }
        }
    }

    override fun obtenerHistorialGlobalCustodia(): Flow<List<TransferenciaCustodia>> {
        return daoCustodia.obtenerHistorialGlobalCustodia().map { lista ->
            lista.map { MapeadorCustodia.transferenciaADominio(it) }
        }
    }

    override fun obtenerReportesDanoPorEquipo(idEquipo: String): Flow<List<ReporteDano>> {
        return daoReporteDano.obtenerReportesDanoPorEquipo(idEquipo).map { lista ->
            lista.map { MapeadorCustodia.reporteDanoADominio(it) }
        }
    }

    override suspend fun registrarTransferenciaCustodia(transferencia: TransferenciaCustodia): Resultado<Unit> {
        return try {
            val entidad = MapeadorCustodia.transferenciaAEntidad(transferencia, sincronizado = true)
            daoCustodia.insertarTransferencia(entidad)

            val mapaData = hashMapOf(
                "id" to transferencia.id,
                "idEquipo" to transferencia.idEquipo,
                "idUsuarioEmisor" to transferencia.idUsuarioEmisor,
                "idUsuarioReceptor" to transferencia.idUsuarioReceptor,
                "fechaHoraTimestamp" to transferencia.fechaHoraTimestamp,
                "uriFotografiaEvidencia" to transferencia.uriFotografiaEvidencia,
                "hashSha256Evidencia" to transferencia.hashSha256Evidencia,
                "latitudGps" to transferencia.latitudGps,
                "longitudGps" to transferencia.longitudGps,
                "notas" to transferencia.notas
            )
            coleccionTransferencias.document(transferencia.id).set(mapaData).await()

            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Exito(Unit)
        }
    }

    override suspend fun registrarReporteDano(reporte: ReporteDano): Resultado<Unit> {
        return try {
            val entidad = MapeadorCustodia.reporteDanoAEntidad(reporte, sincronizado = true)
            daoReporteDano.insertarReporteDano(entidad)

            val mapaData = hashMapOf(
                "id" to reporte.id,
                "idEquipo" to reporte.idEquipo,
                "idUsuarioReportador" to reporte.idUsuarioReportador,
                "fechaHoraTimestamp" to reporte.fechaHoraTimestamp,
                "descripcionDano" to reporte.descripcionDano,
                "severidad" to reporte.severidad.name,
                "uriFotografiaEvidencia" to reporte.uriFotografiaEvidencia,
                "hashSha256Evidencia" to reporte.hashSha256Evidencia
            )
            coleccionReportesDano.document(reporte.id).set(mapaData).await()

            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Exito(Unit)
        }
    }
}
