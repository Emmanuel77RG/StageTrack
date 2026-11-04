package com.example.stagetrack.datos.mapeadores

import com.example.stagetrack.datos.local.entidad.EntidadEvento
import com.example.stagetrack.dominio.modelo.CategoriaEvento
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

        val categoriaEnum = try {
            if (entidad.categoria != null) CategoriaEvento.valueOf(entidad.categoria) else CategoriaEvento.CONCIERTO_SHOW
        } catch (_: Exception) {
            CategoriaEvento.CONCIERTO_SHOW
        }

        val csvTec = entidad.idsTecnicosAsignadosCsv ?: ""
        val listaTecnicos = if (csvTec.isBlank()) {
            emptyList()
        } else {
            csvTec.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }

        val csvEq = entidad.idsEquiposAsignadosCsv ?: ""
        val listaEquipos = if (csvEq.isBlank()) {
            emptyList()
        } else {
            csvEq.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }

        return Evento(
            id = entidad.id ?: "",
            nombre = entidad.nombre ?: "",
            cliente = entidad.cliente ?: "",
            categoria = categoriaEnum,
            fechaInicio = entidad.fechaInicio,
            fechaFin = entidad.fechaFin,
            ubicacion = entidad.ubicacion ?: "",
            idSupervisor = entidad.idSupervisor ?: "",
            nombreSupervisor = entidad.nombreSupervisor ?: "",
            idJefeBodega = entidad.idJefeBodega,
            nombreJefeBodega = entidad.nombreJefeBodega,
            idsTecnicosAsignados = listaTecnicos,
            idsEquiposAsignados = listaEquipos,
            notasMontaje = entidad.notasMontaje,
            estado = estadoEnum
        )
    }

    fun aEntidad(dominio: Evento): EntidadEvento {
        val csvTecnicos = dominio.idsTecnicosAsignados.joinToString(",")
        val csvEquipos = dominio.idsEquiposAsignados.joinToString(",")

        return EntidadEvento(
            id = dominio.id,
            nombre = dominio.nombre,
            cliente = dominio.cliente,
            categoria = dominio.categoria.name,
            fechaInicio = dominio.fechaInicio,
            fechaFin = dominio.fechaFin,
            ubicacion = dominio.ubicacion,
            idSupervisor = dominio.idSupervisor,
            nombreSupervisor = dominio.nombreSupervisor,
            idJefeBodega = dominio.idJefeBodega,
            nombreJefeBodega = dominio.nombreJefeBodega,
            idsTecnicosAsignadosCsv = csvTecnicos,
            idsEquiposAsignadosCsv = csvEquipos,
            notasMontaje = dominio.notasMontaje,
            estado = dominio.estado.name
        )
    }
}
