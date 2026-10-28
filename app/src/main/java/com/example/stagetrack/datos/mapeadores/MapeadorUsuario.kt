package com.example.stagetrack.datos.mapeadores

import com.example.stagetrack.datos.local.entidad.EntidadUsuario
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.dominio.modelo.Usuario

/**
 * Mapeador entre la entidad de datos local y el modelo de dominio de Usuario.
 */
object MapeadorUsuario {

    fun aDominio(entidad: EntidadUsuario): Usuario {
        val rolEnum = try {
            when (entidad.rol) {
                "DIRECTOR_TECNICO" -> RolUsuario.SUPERVISOR
                else -> RolUsuario.valueOf(entidad.rol)
            }
        } catch (_: Exception) {
            RolUsuario.TECNICO
        }

        return Usuario(
            id = entidad.id,
            nombre = entidad.nombre,
            correo = entidad.correo,
            rol = rolEnum,
            urlFotografia = entidad.urlFotografia,
            correoRecuperacion = entidad.correoRecuperacion,
            esPrimerInicio = entidad.esPrimerInicio,
            correoVerificado = entidad.correoVerificado,
            fechaCreacion = entidad.fechaCreacion,
            activo = entidad.activo
        )
    }

    fun aEntidad(
        dominio: Usuario,
        claveHash: String
    ): EntidadUsuario {
        return EntidadUsuario(
            id = dominio.id,
            nombre = dominio.nombre,
            correo = dominio.correo,
            claveHash = claveHash,
            rol = dominio.rol.name,
            urlFotografia = dominio.urlFotografia,
            correoRecuperacion = dominio.correoRecuperacion,
            esPrimerInicio = dominio.esPrimerInicio,
            correoVerificado = dominio.correoVerificado,
            fechaCreacion = dominio.fechaCreacion,
            activo = dominio.activo
        )
    }
}
