package com.example.stagetrack.datos.repositorio

import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.datos.local.dao.DaoEquipo
import com.example.stagetrack.datos.mapeadores.MapeadorEquipo
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.EstadoEquipo
import com.example.stagetrack.dominio.repositorio.RepositorioEquipo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementación del repositorio de inventario respaldado por la base de datos local Room.
 */
class RepositorioEquipoRoom(
    private val daoEquipo: DaoEquipo
) : RepositorioEquipo {

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
            val entidad = MapeadorEquipo.aEntidad(equipo, sincronizado = false)
            daoEquipo.insertarOActualizarEquipo(entidad)
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e)
        }
    }

    override suspend fun actualizarEquipo(equipo: Equipo): Resultado<Unit> {
        return try {
            val entidad = MapeadorEquipo.aEntidad(equipo, sincronizado = false)
            daoEquipo.insertarOActualizarEquipo(entidad)
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e)
        }
    }

    override suspend fun actualizarEstadoEquipo(
        idEquipo: String,
        nuevoEstado: EstadoEquipo
    ): Resultado<Unit> {
        return try {
            daoEquipo.actualizarEstadoCustodia(
                idEquipo = idEquipo,
                nuevoEstado = nuevoEstado.name,
                idCustodio = null,
                nombreCustodio = null,
                sincronizado = false
            )
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e)
        }
    }

    override suspend fun actualizarEstadoCustodia(
        idEquipo: String,
        nuevoEstado: EstadoEquipo,
        idCustodio: String?,
        nombreCustodio: String?
    ): Resultado<Unit> {
        return try {
            daoEquipo.actualizarEstadoCustodia(
                idEquipo = idEquipo,
                nuevoEstado = nuevoEstado.name,
                idCustodio = idCustodio,
                nombreCustodio = nombreCustodio,
                sincronizado = false
            )
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e)
        }
    }
}
