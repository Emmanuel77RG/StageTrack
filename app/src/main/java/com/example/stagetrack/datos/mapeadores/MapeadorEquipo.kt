package com.example.stagetrack.datos.mapeadores

import com.example.stagetrack.datos.local.entidad.EntidadEquipo
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.EstadoEquipo

/**
 * Mapeador entre la entidad de datos local y el modelo de dominio de Equipo.
 */
object MapeadorEquipo {

    fun aDominio(entidad: EntidadEquipo): Equipo {
        val estadoEnum = try {
            EstadoEquipo.valueOf(entidad.estadoActual)
        } catch (_: Exception) {
            EstadoEquipo.EN_BODEGA
        }

        return Equipo(
            id = entidad.id,
            codigoQR = entidad.codigoQR,
            nombre = entidad.nombre,
            categoria = entidad.categoria,
            numeroSerie = entidad.numeroSerie,
            estadoActual = estadoEnum,
            idCustodioActual = entidad.idCustodioActual,
            nombreCustodioActual = entidad.nombreCustodioActual,
            ubicacionActual = entidad.ubicacionActual,
            urlImagen = entidad.urlImagen
        )
    }

    fun aEntidad(dominio: Equipo, sincronizado: Boolean = false): EntidadEquipo {
        return EntidadEquipo(
            id = dominio.id,
            codigoQR = dominio.codigoQR,
            nombre = dominio.nombre,
            categoria = dominio.categoria,
            numeroSerie = dominio.numeroSerie,
            estadoActual = dominio.estadoActual.name,
            idCustodioActual = dominio.idCustodioActual,
            nombreCustodioActual = dominio.nombreCustodioActual,
            ubicacionActual = dominio.ubicacionActual,
            urlImagen = dominio.urlImagen,
            sincronizado = sincronizado
        )
    }
}
