package com.example.stagetrack.datos.repositorio

import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.datos.local.dao.DaoEvento
import com.example.stagetrack.datos.mapeadores.MapeadorEvento
import com.example.stagetrack.dominio.modelo.EstadoEvento
import com.example.stagetrack.dominio.modelo.Evento
import com.example.stagetrack.dominio.repositorio.RepositorioEvento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementación del repositorio de eventos respaldado por la base de datos local Room.
 */
class RepositorioEventoRoom(
    private val daoEvento: DaoEvento
) : RepositorioEvento {

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
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e)
        }
    }

    override suspend fun actualizarEstadoEvento(
        idEvento: String,
        estado: EstadoEvento
    ): Resultado<Unit> {
        return try {
            daoEvento.actualizarEstadoEvento(idEvento, estado.name)
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e)
        }
    }
}
