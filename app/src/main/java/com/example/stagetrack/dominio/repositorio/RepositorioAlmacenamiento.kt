package com.example.stagetrack.dominio.repositorio

import com.example.stagetrack.core.comun.Resultado

/**
 * Interfaz de dominio para la gestión y subida de archivos multimedia al almacenamiento.
 */
interface RepositorioAlmacenamiento {
    /**
     * Sube una foto de perfil de usuario al almacenamiento y retorna su URL pública.
     *
     * @param idUsuario Identificador único del usuario.
     * @param bytesImagen Bytes de la imagen comprimida.
     * @return [Resultado.Exito] con la URL pública HTTPS de la imagen o [Resultado.Error] en caso de fallo.
     */
    suspend fun subirFotoPerfil(idUsuario: String, bytesImagen: ByteArray): Resultado<String>

    /**
     * Sube una foto de evidencia fotográfica de daño al almacenamiento y retorna su URL pública.
     *
     * @param idReporte Identificador del reporte de daño.
     * @param bytesImagen Bytes de la imagen comprimida.
     * @return [Resultado.Exito] con la URL pública HTTPS de la imagen.
     */
    suspend fun subirEvidenciaDano(idReporte: String, bytesImagen: ByteArray): Resultado<String>

    /**
     * Sube una fotografía de catálogo de equipo al almacenamiento de Supabase y retorna su URL pública.
     *
     * @param idEquipo Identificador único del equipo.
     * @param bytesImagen Bytes de la imagen comprimida.
     * @return [Resultado.Exito] con la URL pública HTTPS de la imagen almacenada.
     */
    suspend fun subirFotoEquipo(idEquipo: String, bytesImagen: ByteArray): Resultado<String>

    /**
     * Sube una fotografía de evidencia de custodia o devolución al almacenamiento y retorna su URL pública.
     *
     * @param idTransferencia Identificador de la transferencia de custodia o devolución.
     * @param bytesImagen Bytes de la imagen comprimida.
     * @return [Resultado.Exito] con la URL pública HTTPS de la imagen almacenada.
     */
    suspend fun subirEvidenciaCustodia(idTransferencia: String, bytesImagen: ByteArray): Resultado<String>
}
