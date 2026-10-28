package com.example.stagetrack.core.cripto

import java.security.MessageDigest

/**
 * Utilidad criptográfica para generar hashes SHA-256 inmutables
 * sobre imágenes o arreglos de bytes.
 */
object UtilidadHash {

    /**
     * Genera un hash SHA-256 en formato hexadecimal
     * a partir de un arreglo de bytes (ejemplo: bytes de la imagen capturada).
     */
    fun calcularSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Genera un hash SHA-256 a partir de una cadena de texto UTF-8.
     */
    fun calcularSha256(texto: String): String {
        return calcularSha256(texto.toByteArray(Charsets.UTF_8))
    }
}
