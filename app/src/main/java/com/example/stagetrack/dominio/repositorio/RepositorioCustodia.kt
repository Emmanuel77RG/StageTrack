package com.example.stagetrack.dominio.repositorio

import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.ReporteDano
import com.example.stagetrack.dominio.modelo.TransferenciaCustodia
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para la transferencia de custodia y registro de reportes de daño.
 */
interface RepositorioCustodia {
    fun obtenerHistorialCustodia(idEquipo: String): Flow<List<TransferenciaCustodia>>
    fun obtenerHistorialGlobalCustodia(): Flow<List<TransferenciaCustodia>>
    fun obtenerReportesDanoPorEquipo(idEquipo: String): Flow<List<ReporteDano>>
    suspend fun registrarTransferenciaCustodia(transferencia: TransferenciaCustodia): Resultado<Unit>
    suspend fun registrarReporteDano(reporte: ReporteDano): Resultado<Unit>
}
