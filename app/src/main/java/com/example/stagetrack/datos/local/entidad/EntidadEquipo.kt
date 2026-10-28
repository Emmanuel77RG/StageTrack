package com.example.stagetrack.datos.local.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla 'equipos' en Room para la persistencia local del inventario de activos.
 */
@Entity(tableName = "equipos")
data class EntidadEquipo(
    @PrimaryKey val id: String,
    val codigoQR: String,
    val nombre: String,
    val categoria: String,
    val numeroSerie: String,
    val estadoActual: String,
    val idCustodioActual: String? = null,
    val nombreCustodioActual: String? = null,
    val ubicacionActual: String = "Bodega Principal",
    val urlImagen: String? = null,
    val sincronizado: Boolean = false
)
