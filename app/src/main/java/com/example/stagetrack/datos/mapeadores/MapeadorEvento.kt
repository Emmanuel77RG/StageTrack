package com.example.stagetrack.datos.mapeadores

import com.example.stagetrack.datos.local.entidad.EntidadEvento
import com.example.stagetrack.dominio.modelo.EstadoEvento
import com.example.stagetrack.dominio.modelo.Evento

/**
 * Mapeador entre la entidad de datos local y el modelo de dominio de Evento.
 */
object MapeadorEvento {

    fun aDominio(entidad: EntidadEvento): Evento {
        val estadoEnum = try {
            if (entidad.estado != null) EstadoEvento.valueOf(entidad.estado) else EstadoEvento.PLANIFICADO
        } catch (_: Exception) {
            EstadoEvento.PLANIFICADO
        }

        val csv = entidad.idsTecnicosAsignadosCsv ?: ""
        val listaTecnicos = if (csv.isBlank()) {
            emptyList()
        } else {
            csv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }

        return Evento(
            id = entidad.id ?: "",
            nombre = entidad.nombre ?: "",
            cliente = entidad.cliente ?: "",
            fechaInicio = entidad.fechaInicio,
            fechaFin = entidad.fechaFin,
            ubicacion = entidad.ubicacion ?: "",
            idSupervisor = entidad.idSupervisor ?: "",
            nombreSupervisor = entidad.nombreSupervisor ?: "",
            idsTecnicosAsignados = listaTecnicos,
            estado = estadoEnum
        )
    }

    fun aEntidad(dominio: Evento): EntidadEvento {
        val csvTecnicos = dominio.idsTecnicosAsignados.joinToString(",")

        return EntidadEvento(
            id = dominio.id,
            nombre = dominio.nombre,
            cliente = dominio.cliente,
            fechaInicio = dominio.fechaInicio,
            fechaFin = dominio.fechaFin,
            ubicacion = dominio.ubicacion,
            idSupervisor = dominio.idSupervisor,
            nombreSupervisor = dominio.nombreSupervisor,
            idsTecnicosAsignadosCsv = csvTecnicos,
            estado = dominio.estado.name
        )
    }
}
