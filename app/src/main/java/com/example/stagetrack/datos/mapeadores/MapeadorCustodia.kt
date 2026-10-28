package com.example.stagetrack.datos.mapeadores

import com.example.stagetrack.datos.local.entidad.EntidadReporteDano
import com.example.stagetrack.datos.local.entidad.EntidadTransferenciaCustodia
import com.example.stagetrack.dominio.modelo.NivelSeveridadDano
import com.example.stagetrack.dominio.modelo.ReporteDano
import com.example.stagetrack.dominio.modelo.TransferenciaCustodia

/**
 * Mapeadores bidireccionales entre entidades locales y dominio para custodia y reportes.
 */
object MapeadorCustodia {

    fun transferenciaADominio(entidad: EntidadTransferenciaCustodia): TransferenciaCustodia {
        return TransferenciaCustodia(
            id = entidad.id,
            idEquipo = entidad.idEquipo,
            idUsuarioEmisor = entidad.idUsuarioEmisor,
            idUsuarioReceptor = entidad.idUsuarioReceptor,
            fechaHoraTimestamp = entidad.fechaHoraTimestamp,
            uriFotografiaEvidencia = entidad.uriFotografiaEvidencia,
            hashSha256Evidencia = entidad.hashSha256Evidencia,
            latitudGps = entidad.latitudGps,
            longitudGps = entidad.longitudGps,
            notas = entidad.notas
        )
    }

    fun transferenciaAEntidad(dominio: TransferenciaCustodia, sincronizado: Boolean = false): EntidadTransferenciaCustodia {
        return EntidadTransferenciaCustodia(
            id = dominio.id,
            idEquipo = dominio.idEquipo,
            idUsuarioEmisor = dominio.idUsuarioEmisor,
            idUsuarioReceptor = dominio.idUsuarioReceptor,
            fechaHoraTimestamp = dominio.fechaHoraTimestamp,
            uriFotografiaEvidencia = dominio.uriFotografiaEvidencia,
            hashSha256Evidencia = dominio.hashSha256Evidencia,
            latitudGps = dominio.latitudGps,
            longitudGps = dominio.longitudGps,
            notas = dominio.notas,
            sincronizado = sincronizado
        )
    }

    fun reporteDanoADominio(entidad: EntidadReporteDano): ReporteDano {
        val severidadEnum = try {
            NivelSeveridadDano.valueOf(entidad.severidad)
        } catch (_: Exception) {
            NivelSeveridadDano.MODERADO
        }

        return ReporteDano(
            id = entidad.id,
            idEquipo = entidad.idEquipo,
            idUsuarioReportador = entidad.idUsuarioReportador,
            fechaHoraTimestamp = entidad.fechaHoraTimestamp,
            descripcionDano = entidad.descripcionDano,
            severidad = severidadEnum,
            uriFotografiaEvidencia = entidad.uriFotografiaEvidencia,
            hashSha256Evidencia = entidad.hashSha256Evidencia
        )
    }

    fun reporteDanoAEntidad(dominio: ReporteDano, sincronizado: Boolean = false): EntidadReporteDano {
        return EntidadReporteDano(
            id = dominio.id,
            idEquipo = dominio.idEquipo,
            idUsuarioReportador = dominio.idUsuarioReportador,
            fechaHoraTimestamp = dominio.fechaHoraTimestamp,
            descripcionDano = dominio.descripcionDano,
            severidad = dominio.severidad.name,
            uriFotografiaEvidencia = dominio.uriFotografiaEvidencia,
            hashSha256Evidencia = dominio.hashSha256Evidencia,
            sincronizado = sincronizado
        )
    }
}
