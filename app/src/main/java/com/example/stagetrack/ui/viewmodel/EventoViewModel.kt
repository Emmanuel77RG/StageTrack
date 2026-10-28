package com.example.stagetrack.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.EstadoEvento
import com.example.stagetrack.dominio.modelo.Evento
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.dominio.modelo.Usuario
import com.example.stagetrack.dominio.repositorio.RepositorioEvento
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de la gestión de eventos y proyectos por rol de usuario.
 */
class EventoViewModel(
    private val repositorioEvento: RepositorioEvento
) : ViewModel() {

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val listaEventos: StateFlow<List<Evento>> = _usuarioActual
        .flatMapLatest { usuario ->
            if (usuario?.rol == RolUsuario.TECNICO) {
                repositorioEvento.obtenerEventosPorTecnico(usuario.id)
            } else {
                repositorioEvento.obtenerTodosLosEventos()
            }
        }
        .catch { e ->
            Log.e("EventoViewModel", "Error al obtener lista de eventos: ${e.message}", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun establecerUsuarioActual(usuario: Usuario?) {
        _usuarioActual.value = usuario
    }

    fun crearNuevoEvento(
        nombre: String,
        cliente: String,
        fechaInicio: Long,
        fechaFin: Long,
        ubicacion: String,
        supervisor: Usuario,
        tecnicosAsignados: List<Usuario>,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val nuevoEvento = Evento(
                id = "evt_${System.currentTimeMillis()}",
                nombre = nombre,
                cliente = cliente,
                fechaInicio = fechaInicio,
                fechaFin = fechaFin,
                ubicacion = ubicacion,
                idSupervisor = supervisor.id,
                nombreSupervisor = supervisor.nombre,
                idsTecnicosAsignados = tecnicosAsignados.map { it.id },
                estado = EstadoEvento.PLANIFICADO
            )
            val res = repositorioEvento.crearNuevoEvento(nuevoEvento)
            onResultado(res)
        }
    }

    fun cambiarEstadoEvento(
        idEvento: String,
        nuevoEstado: EstadoEvento,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioEvento.actualizarEstadoEvento(idEvento, nuevoEstado)
            onResultado(res)
        }
    }
}
