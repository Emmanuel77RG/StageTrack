package com.example.stagetrack.dominio.modelo

/**
 * Categoría o tipo de producción del proyecto.
 */
enum class CategoriaEvento {
    CONCIERTO_SHOW,
    CORPORATIVO,
    FESTIVAL,
    TEATRO,
    OTRO
}

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
    val categoria: CategoriaEvento = CategoriaEvento.CONCIERTO_SHOW,
    val fechaInicio: Long,
    val fechaFin: Long,
    val ubicacion: String,
    val idSupervisor: String,
    val nombreSupervisor: String,
    val idJefeBodega: String? = null,
    val nombreJefeBodega: String? = null,
    val idsTecnicosAsignados: List<String> = emptyList(),
    val idsEquiposAsignados: List<String> = emptyList(),
    val notasMontaje: String? = null,
    val estado: EstadoEvento = EstadoEvento.PLANIFICADO
)
