package com.example.stagetrack.datos.local.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla 'reportes_dano' en Room para registrar daños e incidencias.
 */
@Entity(tableName = "reportes_dano")
data class EntidadReporteDano(
    @PrimaryKey val id: String,
    val idEquipo: String,
    val idUsuarioReportador: String,
    val fechaHoraTimestamp: Long,
    val descripcionDano: String,
    val severidad: String,
    val uriFotografiaEvidencia: String,
    val hashSha256Evidencia: String,
    val sincronizado: Boolean = false
)
