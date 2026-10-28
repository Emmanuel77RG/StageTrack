package com.example.stagetrack.datos.local.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla 'usuarios' en Room para la persistencia local de usuarios y autenticación.
 */
@Entity(tableName = "usuarios")
data class EntidadUsuario(
    @PrimaryKey val id: String,
    val nombre: String,
    val correo: String,
    val claveHash: String,
    val rol: String,
    val urlFotografia: String? = null,
    val correoRecuperacion: String? = null,
    val esPrimerInicio: Boolean = false,
    val correoVerificado: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val activo: Boolean = true
)
