package com.example.stagetrack.datos.repositorio

import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.EstadoEquipo
import com.example.stagetrack.dominio.repositorio.RepositorioEquipo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implementación falsa (mock) del repositorio de inventario con datos de prueba iniciales.
 */
class RepositorioEquipoFalso : RepositorioEquipo {

    private val listaEquipos = MutableStateFlow(
        listOf(
            Equipo(
                id = "eq_01",
                codigoQR = "QR-AUDIO-CONSOLE-X32",
                nombre = "Consola Behringer X32",
                categoria = "Audio",
                numeroSerie = "SN-982134",
                estadoActual = EstadoEquipo.EN_BODEGA,
                ubicacionActual = "Bodega Principal"
            ),
            Equipo(
                id = "eq_02",
                codigoQR = "QR-LIGHT-ROBOTIC-ROBE",
                nombre = "Luminaria Robótica Robe Pointe",
                categoria = "Iluminación",
                numeroSerie = "SN-443120",
                estadoActual = EstadoEquipo.EN_CUSTODIA,
                idCustodioActual = "usr_01",
                nombreCustodioActual = "Carlos Rodríguez",
                ubicacionActual = "Escenario Principal"
            ),
            Equipo(
                id = "eq_03",
                codigoQR = "QR-LED-SCREEN-P3",
                nombre = "Pantalla LED P3.9 (Gabinete 50x50)",
                categoria = "Video",
                numeroSerie = "SN-771239",
                estadoActual = EstadoEquipo.EN_TRANSITO,
                ubicacionActual = "Camión de Flete 2"
            )
        )
    )

    override fun obtenerTodosLosEquipos(): Flow<List<Equipo>> = listaEquipos.asStateFlow()

    override fun obtenerEquipoPorCodigoQr(codigoQr: String): Flow<Equipo?> {
        return listaEquipos.map { equipos ->
            equipos.find { it.codigoQR.equals(codigoQr, ignoreCase = true) }
        }
    }

    override suspend fun registrarNuevoEquipo(equipo: Equipo): Resultado<Unit> {
        val listaActual = listaEquipos.value.toMutableList()
        listaActual.add(equipo)
        listaEquipos.value = listaActual
        return Resultado.Exito(Unit)
    }

    override suspend fun actualizarEquipo(equipo: Equipo): Resultado<Unit> {
        val listaActual = listaEquipos.value.toMutableList()
        val indice = listaActual.indexOfFirst { it.id == equipo.id }
        if (indice != -1) {
            listaActual[indice] = equipo
            listaEquipos.value = listaActual
            return Resultado.Exito(Unit)
        }
        return Resultado.Error(Exception("Equipo no encontrado con id ${equipo.id}"))
    }

    override suspend fun actualizarEstadoEquipo(
        idEquipo: String,
        nuevoEstado: EstadoEquipo
    ): Resultado<Unit> {
        val listaActual = listaEquipos.value.toMutableList()
        val indice = listaActual.indexOfFirst { it.id == idEquipo }
        if (indice != -1) {
            val equipoActualizado = listaActual[indice].copy(estadoActual = nuevoEstado)
            listaActual[indice] = equipoActualizado
            listaEquipos.value = listaActual
            return Resultado.Exito(Unit)
        }
        return Resultado.Error(Exception("Equipo no encontrado con id $idEquipo"))
    }

    override suspend fun actualizarEstadoCustodia(
        idEquipo: String,
        nuevoEstado: EstadoEquipo,
        idCustodio: String?,
        nombreCustodio: String?
    ): Resultado<Unit> {
        val listaActual = listaEquipos.value.toMutableList()
        val indice = listaActual.indexOfFirst { it.id == idEquipo }
        if (indice != -1) {
            val equipoActualizado = listaActual[indice].copy(
                estadoActual = nuevoEstado,
                idCustodioActual = idCustodio,
                nombreCustodioActual = nombreCustodio
            )
            listaActual[indice] = equipoActualizado
            listaEquipos.value = listaActual
            return Resultado.Exito(Unit)
        }
        return Resultado.Error(Exception("Equipo no encontrado con id $idEquipo"))
    }
}
