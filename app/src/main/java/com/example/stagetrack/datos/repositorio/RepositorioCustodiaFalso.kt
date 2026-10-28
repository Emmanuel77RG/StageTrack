package com.example.stagetrack.datos.repositorio

import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.ReporteDano
import com.example.stagetrack.dominio.modelo.TransferenciaCustodia
import com.example.stagetrack.dominio.repositorio.RepositorioCustodia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implementación falsa (mock) del repositorio de transferencias de custodia y reportes de daños.
 */
class RepositorioCustodiaFalso : RepositorioCustodia {

    private val transferencias = MutableStateFlow<List<TransferenciaCustodia>>(emptyList())
    private val reportesDano = MutableStateFlow<List<ReporteDano>>(emptyList())

    override fun obtenerHistorialCustodia(idEquipo: String): Flow<List<TransferenciaCustodia>> {
        return transferencias.map { lista ->
            lista.filter { it.idEquipo == idEquipo }
        }
    }

    override fun obtenerHistorialGlobalCustodia(): Flow<List<TransferenciaCustodia>> {
        return transferencias.asStateFlow()
    }

    override fun obtenerReportesDanoPorEquipo(idEquipo: String): Flow<List<ReporteDano>> {
        return reportesDano.map { lista ->
            lista.filter { it.idEquipo == idEquipo }
        }
    }

    override suspend fun registrarTransferenciaCustodia(transferencia: TransferenciaCustodia): Resultado<Unit> {
        val listaActual = transferencias.value.toMutableList()
        listaActual.add(0, transferencia)
        transferencias.value = listaActual
        return Resultado.Exito(Unit)
    }

    override suspend fun registrarReporteDano(reporte: ReporteDano): Resultado<Unit> {
        val listaActual = reportesDano.value.toMutableList()
        listaActual.add(0, reporte)
        reportesDano.value = listaActual
        return Resultado.Exito(Unit)
    }
}
