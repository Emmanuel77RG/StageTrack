package com.example.stagetrack.datos.repositorio

import android.util.Log
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.datos.local.dao.DaoEquipo
import com.example.stagetrack.datos.local.entidad.EntidadEquipo
import com.example.stagetrack.datos.mapeadores.MapeadorEquipo
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.EstadoEquipo
import com.example.stagetrack.dominio.repositorio.RepositorioEquipo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de inventario con sincronización híbrida offline-first en tiempo real (Room + Cloud Firestore).
 */
class RepositorioEquipoFirestore(
    private val daoEquipo: DaoEquipo,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : RepositorioEquipo {

    private val coleccionEquipos = firestore.collection("equipos")

    init {
        try {
            coleccionEquipos.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("RepositorioEquipoFirestore", "Aviso en listener de Firestore equipos: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    scope.launch {
                        val entidades = snapshot.documents.mapNotNull { doc ->
                            try {
                                EntidadEquipo(
                                    id = doc.id,
                                    codigoQR = doc.getString("codigoQR") ?: "",
                                    nombre = doc.getString("nombre") ?: "",
                                    categoria = doc.getString("categoria") ?: "",
                                    numeroSerie = doc.getString("numeroSerie") ?: "",
                                    estadoActual = doc.getString("estadoActual") ?: EstadoEquipo.EN_BODEGA.name,
                                    idCustodioActual = doc.getString("idCustodioActual"),
                                    nombreCustodioActual = doc.getString("nombreCustodioActual"),
                                    ubicacionActual = doc.getString("ubicacionActual") ?: "Bodega Principal",
                                    urlImagen = doc.getString("urlImagen"),
                                    sincronizado = true
                                )
                            } catch (_: Exception) {
                                null
                            }
                        }
                        if (entidades.isNotEmpty()) {
                            daoEquipo.insertarOActualizarEquipos(entidades)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RepositorioEquipoFirestore", "Error al conectar escuchador de Firestore: ${e.message}", e)
        }
    }

    override fun obtenerTodosLosEquipos(): Flow<List<Equipo>> {
        return daoEquipo.obtenerTodosLosEquipos().map { lista ->
            lista.map { MapeadorEquipo.aDominio(it) }
        }
    }

    override fun obtenerEquipoPorCodigoQr(codigoQr: String): Flow<Equipo?> {
        return daoEquipo.obtenerEquipoPorCodigoQr(codigoQr).map { entidad ->
            entidad?.let { MapeadorEquipo.aDominio(it) }
        }
    }

    override suspend fun registrarNuevoEquipo(equipo: Equipo): Resultado<Unit> {
        return try {
            val mapaData = hashMapOf(
                "id" to equipo.id,
                "codigoQR" to equipo.codigoQR,
                "nombre" to equipo.nombre,
                "categoria" to equipo.categoria,
                "numeroSerie" to equipo.numeroSerie,
                "estadoActual" to equipo.estadoActual.name,
                "idCustodioActual" to equipo.idCustodioActual,
                "nombreCustodioActual" to equipo.nombreCustodioActual,
                "ubicacionActual" to equipo.ubicacionActual,
                "urlImagen" to equipo.urlImagen
            )
            coleccionEquipos.document(equipo.id).set(mapaData, SetOptions.merge()).await()

            val entidad = MapeadorEquipo.aEntidad(equipo, sincronizado = true)
            daoEquipo.insertarOActualizarEquipo(entidad)
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Log.w("RepositorioEquipoFirestore", "Fallo al sincronizar alta de equipo con Firestore (modo offline): ${e.message}")
            val entidadLocal = MapeadorEquipo.aEntidad(equipo, sincronizado = false)
            daoEquipo.insertarOActualizarEquipo(entidadLocal)
            Resultado.Exito(Unit)
        }
    }

    override suspend fun actualizarEquipo(equipo: Equipo): Resultado<Unit> {
        return try {
            val mapaData = hashMapOf(
                "id" to equipo.id,
                "codigoQR" to equipo.codigoQR,
                "nombre" to equipo.nombre,
                "categoria" to equipo.categoria,
                "numeroSerie" to equipo.numeroSerie,
                "estadoActual" to equipo.estadoActual.name,
                "idCustodioActual" to equipo.idCustodioActual,
                "nombreCustodioActual" to equipo.nombreCustodioActual,
                "ubicacionActual" to equipo.ubicacionActual,
                "urlImagen" to equipo.urlImagen
            )
            coleccionEquipos.document(equipo.id).set(mapaData, SetOptions.merge()).await()

            val entidad = MapeadorEquipo.aEntidad(equipo, sincronizado = true)
            daoEquipo.insertarOActualizarEquipo(entidad)
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Log.w("RepositorioEquipoFirestore", "Fallo al sincronizar actualización con Firestore (modo offline): ${e.message}")
            val entidadLocal = MapeadorEquipo.aEntidad(equipo, sincronizado = false)
            daoEquipo.insertarOActualizarEquipo(entidadLocal)
            Resultado.Exito(Unit)
        }
    }

    override suspend fun actualizarEstadoEquipo(
        idEquipo: String,
        nuevoEstado: EstadoEquipo
    ): Resultado<Unit> {
        return actualizarEstadoCustodia(idEquipo, nuevoEstado, null, null)
    }

    override suspend fun actualizarEstadoCustodia(
        idEquipo: String,
        nuevoEstado: EstadoEquipo,
        idCustodio: String?,
        nombreCustodio: String?
    ): Resultado<Unit> {
        return try {
            val mapaActualizacion = hashMapOf<String, Any?>(
                "estadoActual" to nuevoEstado.name,
                "idCustodioActual" to idCustodio,
                "nombreCustodioActual" to nombreCustodio
            )
            coleccionEquipos.document(idEquipo).update(mapaActualizacion).await()

            daoEquipo.actualizarEstadoCustodia(
                idEquipo = idEquipo,
                nuevoEstado = nuevoEstado.name,
                idCustodio = idCustodio,
                nombreCustodio = nombreCustodio,
                sincronizado = true
            )
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Log.w("RepositorioEquipoFirestore", "Fallo al actualizar estado en Firestore en tiempo real: ${e.message}")
            daoEquipo.actualizarEstadoCustodia(
                idEquipo = idEquipo,
                nuevoEstado = nuevoEstado.name,
                idCustodio = idCustodio,
                nombreCustodio = nombreCustodio,
                sincronizado = false
            )
            Resultado.Exito(Unit)
        }
    }
}
