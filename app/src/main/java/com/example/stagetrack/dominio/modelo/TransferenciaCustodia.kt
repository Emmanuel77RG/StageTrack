package com.example.stagetrack.dominio.modelo

/**
 * Registro inmutable de transferencia de custodia con evidencia fotográfica criptográfica.
 */
data class TransferenciaCustodia(
    val id: String,
    val idEquipo: String,
    val idUsuarioEmisor: String,
    val idUsuarioReceptor: String,
    val fechaHoraTimestamp: Long = System.currentTimeMillis(),
    val uriFotografiaEvidencia: String,
    val hashSha256Evidencia: String,
    val latitudGps: Double? = null,
    val longitudGps: Double? = null,
    val notas: String? = null
)
