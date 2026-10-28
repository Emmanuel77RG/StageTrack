package com.example.stagetrack.datos.repositorio

import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.datos.local.dao.DaoCustodia
import com.example.stagetrack.datos.local.dao.DaoReporteDano
import com.example.stagetrack.datos.mapeadores.MapeadorCustodia
import com.example.stagetrack.dominio.modelo.ReporteDano
import com.example.stagetrack.dominio.modelo.TransferenciaCustodia
import com.example.stagetrack.dominio.repositorio.RepositorioCustodia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementación del repositorio de transferencias y reportes respaldado por Room.
 */
class RepositorioCustodiaRoom(
    private val daoCustodia: DaoCustodia,
    private val daoReporteDano: DaoReporteDano
) : RepositorioCustodia {

    override fun obtenerHistorialCustodia(idEquipo: String): Flow<List<TransferenciaCustodia>> {
        return daoCustodia.obtenerHistorialCustodia(idEquipo).map { lista ->
            lista.map { MapeadorCustodia.transferenciaADominio(it) }
        }
    }

    override fun obtenerHistorialGlobalCustodia(): Flow<List<TransferenciaCustodia>> {
        return daoCustodia.obtenerHistorialGlobalCustodia().map { lista ->
            lista.map { MapeadorCustodia.transferenciaADominio(it) }
        }
    }

    override fun obtenerReportesDanoPorEquipo(idEquipo: String): Flow<List<ReporteDano>> {
        return daoReporteDano.obtenerReportesDanoPorEquipo(idEquipo).map { lista ->
            lista.map { MapeadorCustodia.reporteDanoADominio(it) }
        }
    }

    override suspend fun registrarTransferenciaCustodia(transferencia: TransferenciaCustodia): Resultado<Unit> {
        return try {
            val entidad = MapeadorCustodia.transferenciaAEntidad(transferencia, sincronizado = false)
            daoCustodia.insertarTransferencia(entidad)
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e)
        }
    }

    override suspend fun registrarReporteDano(reporte: ReporteDano): Resultado<Unit> {
        return try {
            val entidad = MapeadorCustodia.reporteDanoAEntidad(reporte, sincronizado = false)
            daoReporteDano.insertarReporteDano(entidad)
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e)
        }
    }
}
