package com.example.stagetrack.dominio.modelo

/**
 * Rol del usuario en la operación de producción de eventos en vivo.
 */
enum class RolUsuario {
    ADMINISTRADOR_GENERAL,
    JEFE_BODEGA,
    TECNICO,
    SUPERVISOR
}

/**
 * Representa a un usuario dentro de la plataforma StageTrack.
 */
data class Usuario(
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: RolUsuario,
    val urlFotografia: String? = null,
    val correoRecuperacion: String? = null,
    val esPrimerInicio: Boolean = false,
    val correoVerificado: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val activo: Boolean = true
)
