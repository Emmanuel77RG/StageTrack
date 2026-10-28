package com.example.stagetrack.dominio.repositorio

import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.EstadoEvento
import com.example.stagetrack.dominio.modelo.Evento
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de repositorio para las operaciones de eventos y proyectos.
 */
interface RepositorioEvento {
    fun obtenerTodosLosEventos(): Flow<List<Evento>>
    fun obtenerEventosPorTecnico(idTecnico: String): Flow<List<Evento>>
    suspend fun crearNuevoEvento(evento: Evento): Resultado<Unit>
    suspend fun actualizarEstadoEvento(idEvento: String, estado: EstadoEvento): Resultado<Unit>
}
