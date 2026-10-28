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
    val fechaInicio: Long,
    val fechaFin: Long,
    val ubicacion: String,
    val idSupervisor: String,
    val nombreSupervisor: String,
    val idsTecnicosAsignadosCsv: String,
    val estado: String
)
