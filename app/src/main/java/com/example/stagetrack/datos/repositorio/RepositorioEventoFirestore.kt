package com.example.stagetrack.datos.repositorio

import android.util.Log
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.datos.local.dao.DaoEvento
import com.example.stagetrack.datos.local.entidad.EntidadEvento
import com.example.stagetrack.datos.mapeadores.MapeadorEvento
import com.example.stagetrack.dominio.modelo.EstadoEvento
import com.example.stagetrack.dominio.modelo.Evento
import com.example.stagetrack.dominio.repositorio.RepositorioEvento
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de eventos con sincronización híbrida (Room + Cloud Firestore).
 */
class RepositorioEventoFirestore(
    private val daoEvento: DaoEvento,
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : RepositorioEvento {

    private val coleccionEventos = firestore.collection("eventos")

    init {
        try {
            coleccionEventos.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("RepositorioEventoFirestore", "Aviso en listener de Firestore eventos: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    scope.launch {
                        val entidades = snapshot.documents.mapNotNull { doc ->
                            try {
                                EntidadEvento(
                                    id = doc.id,
                                    nombre = doc.getString("nombre") ?: "",
                                    cliente = doc.getString("cliente") ?: "",
                                    fechaInicio = doc.getLong("fechaInicio") ?: System.currentTimeMillis(),
                                    fechaFin = doc.getLong("fechaFin") ?: System.currentTimeMillis(),
                                    ubicacion = doc.getString("ubicacion") ?: "",
                                    idSupervisor = doc.getString("idSupervisor") ?: "",
                                    nombreSupervisor = doc.getString("nombreSupervisor") ?: "",
                                    idsTecnicosAsignadosCsv = doc.getString("idsTecnicosAsignadosCsv") ?: "",
                                    estado = doc.getString("estado") ?: EstadoEvento.PLANIFICADO.name
                                )
                            } catch (_: Exception) {
                                null
                            }
                        }
                        if (entidades.isNotEmpty()) {
                            daoEvento.insertarOActualizarEventos(entidades)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RepositorioEventoFirestore", "Error al conectar escuchador de eventos: ${e.message}", e)
        }
    }

    override fun obtenerTodosLosEventos(): Flow<List<Evento>> {
        return daoEvento.obtenerTodosLosEventos().map { lista ->
            lista.map { MapeadorEvento.aDominio(it) }
        }
    }

    override fun obtenerEventosPorTecnico(idTecnico: String): Flow<List<Evento>> {
        return daoEvento.obtenerEventosPorTecnico(idTecnico).map { lista ->
            lista.map { MapeadorEvento.aDominio(it) }
        }
    }

    override suspend fun crearNuevoEvento(evento: Evento): Resultado<Unit> {
        return try {
            val entidad = MapeadorEvento.aEntidad(evento)
            daoEvento.insertarOActualizarEvento(entidad)

            val mapaData = hashMapOf(
                "id" to evento.id,
                "nombre" to evento.nombre,
                "cliente" to evento.cliente,
                "fechaInicio" to evento.fechaInicio,
                "fechaFin" to evento.fechaFin,
                "ubicacion" to evento.ubicacion,
                "idSupervisor" to evento.idSupervisor,
                "nombreSupervisor" to evento.nombreSupervisor,
                "idsTecnicosAsignadosCsv" to evento.idsTecnicosAsignados.joinToString(","),
                "estado" to evento.estado.name
            )
            coleccionEventos.document(evento.id).set(mapaData).await()

            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Exito(Unit)
        }
    }

    override suspend fun actualizarEstadoEvento(
        idEvento: String,
        estado: EstadoEvento
    ): Resultado<Unit> {
        return try {
            daoEvento.actualizarEstadoEvento(idEvento, estado.name)
            coleccionEventos.document(idEvento).update("estado", estado.name).await()
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Exito(Unit)
        }
    }
}
