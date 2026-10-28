package com.example.stagetrack.dominio.modelo

/**
 * Severidad del daño o incidencia detectada en el equipo.
 */
enum class NivelSeveridadDano {
    LEVE,
    MODERADO,
    CRITICO
}

/**
 * Registro de reporte de daños o faltantes en un activo técnico.
 */
data class ReporteDano(
    val id: String,
    val idEquipo: String,
    val idUsuarioReportador: String,
    val fechaHoraTimestamp: Long = System.currentTimeMillis(),
    val descripcionDano: String,
    val severidad: NivelSeveridadDano,
    val uriFotografiaEvidencia: String,
    val hashSha256Evidencia: String
)
