package com.example.stagetrack.core.cripto

import org.junit.Assert.assertEquals
import org.junit.Test

class UtilidadHashTest {

    @Test
    fun calcularSha256_textoConocido_retornaHashSHA256Correcto() {
        val textoPrueba = "test"
        // Hash SHA-256 estándar para "test"
        val hashEsperado = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"
        val hashCalculado = UtilidadHash.calcularSha256(textoPrueba)
        assertEquals(hashEsperado, hashCalculado)
    }

    @Test
    fun calcularSha256_bytesIdenticos_retornaMismoHash() {
        val bytes = byteArrayOf(10, 20, 30, 40)
        val hash1 = UtilidadHash.calcularSha256(bytes)
        val hash2 = UtilidadHash.calcularSha256(bytes)
        assertEquals(hash1, hash2)
    }
}
