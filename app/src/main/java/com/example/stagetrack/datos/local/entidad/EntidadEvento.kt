package com.example.stagetrack.datos.local.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla 'eventos' en Room para la persistencia local de proyectos y espectáculos.
 */
@Entity(tableName = "eventos")
data class EntidadEvento(
    @PrimaryKey val id: String,
    val nombre: String,
    val cliente: String,
    val categoria: String = "CONCIERTO_SHOW",
    val fechaInicio: Long,
    val fechaFin: Long,
    val ubicacion: String,
    val idSupervisor: String,
    val nombreSupervisor: String,
    val idJefeBodega: String? = null,
    val nombreJefeBodega: String? = null,
    val idsTecnicosAsignadosCsv: String = "",
    val idsEquiposAsignadosCsv: String = "",
    val notasMontaje: String? = null,
    val estado: String
)
