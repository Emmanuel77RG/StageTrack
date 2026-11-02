package com.example.stagetrack.core.sincronizacion

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.stagetrack.datos.local.basedatos.BaseDatosStageTrack
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Worker de WorkManager que se ejecuta automáticamente cuando el dispositivo recupera la conexión a internet
 * para sincronizar todos los registros locales pendientes (modo offline) con Cloud Firestore.
 */
class SincronizadorOfflineWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val baseDatos = BaseDatosStageTrack.obtenerInstancia(applicationContext)
            val firestore = FirebaseFirestore.getInstance()

            var totalSincronizados = 0

            // 1. Sincronizar Equipos pendientes (sincronizado = 0)
            val equiposPendientes = baseDatos.daoEquipo().obtenerEquiposPendientesSincronizacion()
            for (eq in equiposPendientes) {
                try {
                    val mapaEq = hashMapOf(
                        "id" to eq.id,
                        "codigoQR" to eq.codigoQR,
                        "nombre" to eq.nombre,
                        "categoria" to eq.categoria,
                        "numeroSerie" to eq.numeroSerie,
                        "estadoActual" to eq.estadoActual,
                        "idCustodioActual" to eq.idCustodioActual,
                        "nombreCustodioActual" to eq.nombreCustodioActual,
                        "ubicacionActual" to eq.ubicacionActual,
                        "urlImagen" to eq.urlImagen
                    )
                    firestore.collection("equipos").document(eq.id).set(mapaEq, SetOptions.merge()).await()
                    baseDatos.daoEquipo().insertarOActualizarEquipo(eq.copy(sincronizado = true))
                    totalSincronizados++
                } catch (e: Exception) {
                    Log.w("SincronizadorOffline", "Aviso al subir equipo offline ${eq.id}: ${e.message}")
                }
            }

            // 2. Sincronizar Transferencias de Custodia pendientes
            val transferenciasPendientes = baseDatos.daoCustodia().obtenerTransferenciasPendientesSincronizacion()
            for (tr in transferenciasPendientes) {
                try {
                    val mapaTr = hashMapOf(
                        "id" to tr.id,
                        "idEquipo" to tr.idEquipo,
                        "idUsuarioEmisor" to tr.idUsuarioEmisor,
                        "idUsuarioReceptor" to tr.idUsuarioReceptor,
                        "fechaHoraTimestamp" to tr.fechaHoraTimestamp,
                        "uriFotografiaEvidencia" to tr.uriFotografiaEvidencia,
                        "hashSha256Evidencia" to tr.hashSha256Evidencia,
                        "latitudGps" to tr.latitudGps,
                        "longitudGps" to tr.longitudGps,
                        "notas" to tr.notas
                    )
                    firestore.collection("transferencias").document(tr.id).set(mapaTr, SetOptions.merge()).await()
                    baseDatos.daoCustodia().insertarTransferencia(tr.copy(sincronizado = true))
                    totalSincronizados++
                } catch (e: Exception) {
                    Log.w("SincronizadorOffline", "Aviso al subir custodia offline ${tr.id}: ${e.message}")
                }
            }

            // 3. Sincronizar Reportes de Daño pendientes
            val reportesPendientes = baseDatos.daoReporteDano().obtenerReportesDanoPendientesSincronizacion()
            for (rep in reportesPendientes) {
                try {
                    val mapaRep = hashMapOf(
                        "id" to rep.id,
                        "idEquipo" to rep.idEquipo,
                        "idUsuarioReportador" to rep.idUsuarioReportador,
                        "fechaHoraTimestamp" to rep.fechaHoraTimestamp,
                        "descripcionDano" to rep.descripcionDano,
                        "severidad" to rep.severidad,
                        "uriFotografiaEvidencia" to rep.uriFotografiaEvidencia,
                        "hashSha256Evidencia" to rep.hashSha256Evidencia
                    )
                    firestore.collection("reportes_dano").document(rep.id).set(mapaRep, SetOptions.merge()).await()
                    baseDatos.daoReporteDano().insertarReporteDano(rep.copy(sincronizado = true))
                    totalSincronizados++
                } catch (e: Exception) {
                    Log.w("SincronizadorOffline", "Aviso al subir reporte daño offline ${rep.id}: ${e.message}")
                }
            }

            if (totalSincronizados > 0) {
                Log.d("SincronizadorOffline", "Sincronización offline exitosa. $totalSincronizados registros subidos a Firestore.")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SincronizadorOffline", "Error en proceso de sincronización offline: ${e.message}", e)
            Result.retry()
        }
    }
}
