package com.example.stagetrack.dominio.modelo

/**
 * Estado logístico operativo del equipo o flight case.
 */
enum class EstadoEquipo {
    EN_BODEGA,
    EN_TRANSITO,
    EN_CUSTODIA,
    EN_PROCESO_DEVOLUCION,
    REPORTADO_DANADO,
    EN_MANTENIMIENTO,
    DEVUELTO,
    DADO_DE_BAJA
}

/**
 * Representa un activo técnico o flight case registrado en el inventario.
 */
data class Equipo(
    val id: String,
    val codigoQR: String,
    val nombre: String,
    val categoria: String,
    val numeroSerie: String,
    val estadoActual: EstadoEquipo,
    val idCustodioActual: String? = null,
    val nombreCustodioActual: String? = null,
    val ubicacionActual: String = "Bodega Principal",
    val urlImagen: String? = null
)
