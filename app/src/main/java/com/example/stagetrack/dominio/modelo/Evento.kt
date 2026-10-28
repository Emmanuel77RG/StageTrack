package com.example.stagetrack.dominio.modelo

/**
 * Estado operativo de un evento o proyecto de producción.
 */
enum class EstadoEvento {
    PLANIFICADO,
    EN_CURSO,
    FINALIZADO,
    CANCELADO
}

/**
 * Representa un evento o espectáculo registrado en StageTrack.
 */
data class Evento(
    val id: String,
    val nombre: String,
    val cliente: String,
    val fechaInicio: Long,
    val fechaFin: Long,
    val ubicacion: String,
    val idSupervisor: String,
    val nombreSupervisor: String,
    val idsTecnicosAsignados: List<String> = emptyList(),
    val estado: EstadoEvento = EstadoEvento.PLANIFICADO
)
