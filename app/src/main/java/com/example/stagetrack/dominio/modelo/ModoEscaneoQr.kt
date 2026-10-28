package com.example.stagetrack.dominio.modelo

/**
 * Propósito u objetivo operativo al activar el lector de código QR.
 */
enum class ModoEscaneoQr {
    /**
     * Consulta e inspección de la Ficha Técnica, trazabilidad y estado del equipo.
     */
    CONSULTAR_FICHA,

    /**
     * Recepción y confirmación de custodia de equipos en tránsito o devolución.
     */
    RECEPCION_CUSTODIA,

    /**
     * Despacho desde bodega o traspaso directo de custodia a otro usuario.
     */
    DESPACHO_TRASPASO
}
