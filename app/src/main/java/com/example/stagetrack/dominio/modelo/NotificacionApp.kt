package com.example.stagetrack.dominio.modelo

/**
 * Representa una notificación interna en el historial de la aplicación StageTrack.
 */
data class NotificacionApp(
    val id: String = "notif_${System.currentTimeMillis()}_${(100..999).random()}",
    val idUsuarioDestino: String,
    val titulo: String,
    val mensaje: String,
    val tipo: TipoNotificacion,
    val fechaTimestamp: Long = System.currentTimeMillis(),
    val leida: Boolean = false,
    val idEntidadRelacionada: String? = null
)

enum class TipoNotificacion {
    TRANSFERENCIA_CUSTODIA,
    DEVOLUCION_BODEGA,
    ASIGNACION_EVENTO,
    REPORTE_DANO,
    SISTEMA
}
