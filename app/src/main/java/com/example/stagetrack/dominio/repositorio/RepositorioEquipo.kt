package com.example.stagetrack.dominio.repositorio

import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.EstadoEquipo
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para la gestión e inspección del inventario de equipos.
 */
interface RepositorioEquipo {
    fun obtenerTodosLosEquipos(): Flow<List<Equipo>>
    fun obtenerEquipoPorCodigoQr(codigoQr: String): Flow<Equipo?>
    suspend fun registrarNuevoEquipo(equipo: Equipo): Resultado<Unit>
    suspend fun actualizarEquipo(equipo: Equipo): Resultado<Unit>
    suspend fun actualizarEstadoEquipo(idEquipo: String, nuevoEstado: EstadoEquipo): Resultado<Unit>
    suspend fun actualizarEstadoCustodia(
        idEquipo: String,
        nuevoEstado: EstadoEquipo,
        idCustodio: String?,
        nombreCustodio: String?
    ): Resultado<Unit>
}
