package com.example.stagetrack.datos.local.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla 'transferencias_custodia' en Room para el registro inmutable de responsabilidad.
 */
@Entity(tableName = "transferencias_custodia")
data class EntidadTransferenciaCustodia(
    @PrimaryKey val id: String,
    val idEquipo: String,
    val idUsuarioEmisor: String,
    val idUsuarioReceptor: String,
    val fechaHoraTimestamp: Long,
    val uriFotografiaEvidencia: String,
    val hashSha256Evidencia: String,
    val latitudGps: Double? = null,
    val longitudGps: Double? = null,
    val notas: String? = null,
    val sincronizado: Boolean = false
)
