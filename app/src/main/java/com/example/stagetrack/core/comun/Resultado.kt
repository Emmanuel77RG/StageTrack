package com.example.stagetrack.core.comun

/**
 * Clase sellada genérica para representar el resultado de operaciones asíncronas o de datos.
 */
sealed class Resultado<out T> {
    data class Exito<out T>(val datos: T) : Resultado<T>()
    data class Error(val excepcion: Throwable, val mensaje: String? = excepcion.localizedMessage) : Resultado<Nothing>()
    data object Cargando : Resultado<Nothing>()
}
