package com.example.stagetrack.core.config

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor centralizado del catálogo administrable de categorías de equipos.
 */
object CatalogoCategoriasMarcas {

    private val categoriasIniciales = mutableListOf(
        "Audio",
        "Iluminación",
        "Video",
        "Estructura",
        "Rigging",
        "Cables y Energía",
        "Efectos Especiales",
        "Consolas / DMX"
    )

    private val _listaCategorias = MutableStateFlow<List<String>>(categoriasIniciales.toList())
    val listaCategorias: StateFlow<List<String>> = _listaCategorias.asStateFlow()

    fun agregarCategoria(nombre: String): Boolean {
        val limpia = nombre.trim()
        if (limpia.isNotBlank() && !_listaCategorias.value.any { it.equals(limpia, ignoreCase = true) }) {
            val nuevaLista = _listaCategorias.value.toMutableList()
            nuevaLista.add(limpia)
            _listaCategorias.value = nuevaLista
            return true
        }
        return false
    }

    fun eliminarCategoria(nombre: String): Boolean {
        if (_listaCategorias.value.size > 1) {
            val nuevaLista = _listaCategorias.value.toMutableList()
            val rem = nuevaLista.removeIf { it.equals(nombre, ignoreCase = true) }
            if (rem) {
                _listaCategorias.value = nuevaLista
                return true
            }
        }
        return false
    }
}
