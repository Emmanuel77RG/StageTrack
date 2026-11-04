package com.example.stagetrack.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.CategoriaEvento
import com.example.stagetrack.dominio.modelo.Equipo
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
        context: Context? = null,
        nombre: String,
        cliente: String,
        categoria: CategoriaEvento = CategoriaEvento.CONCIERTO_SHOW,
        fechaInicio: Long,
        fechaFin: Long,
        ubicacion: String,
        supervisor: Usuario,
        tecnicosAsignados: List<Usuario>,
        equiposAsignados: List<Equipo> = emptyList(),
        notasMontaje: String? = null,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val idEvt = "evt_${System.currentTimeMillis()}"
            val nuevoEvento = Evento(
                id = idEvt,
                nombre = nombre,
                cliente = cliente,
                categoria = categoria,
                fechaInicio = fechaInicio,
                fechaFin = fechaFin,
                ubicacion = ubicacion,
                idSupervisor = supervisor.id,
                nombreSupervisor = supervisor.nombre,
                idsTecnicosAsignados = tecnicosAsignados.map { it.id },
                idsEquiposAsignados = equiposAsignados.map { it.id },
                notasMontaje = notasMontaje,
                estado = EstadoEvento.PLANIFICADO
            )
            val res = repositorioEvento.crearNuevoEvento(nuevoEvento)
            if (res is Resultado.Exito) {
                tecnicosAsignados.forEach { tec ->
                    val titulo = "Convocatoria a Nuevo Evento"
                    val mensaje = "Has sido asignado al proyecto \"$nombre\" en $ubicacion. Revisa tu agenda."
                    com.example.stagetrack.datos.repositorio.GestorNotificaciones.agregarNotificacion(
                        context = null,
                        idUsuarioActualLogueado = supervisor.id,
                        idUsuarioDestino = tec.id,
                        titulo = titulo,
                        mensaje = mensaje,
                        tipo = com.example.stagetrack.dominio.modelo.TipoNotificacion.ASIGNACION_EVENTO,
                        idEntidadRelacionada = idEvt
                    )
                    viewModelScope.launch {
                        try {
                            if (context != null) {
                                com.example.stagetrack.core.notificaciones.UtilidadFcmPush.enviarNotificacionPushDirecta(
                                    context = context,
                                    fcmTokenDestino = "",
                                    titulo = titulo,
                                    mensaje = mensaje,
                                    tipo = "ASIGNACION_EVENTO",
                                    idUsuarioDestino = tec.id
                                )
                            }
                        } catch (_: Exception) {}
                    }
                }

                if (equiposAsignados.isNotEmpty()) {
                    val tituloBodega = "Lista de Empaque para Evento"
                    val mensajeBodega = "Se creó el proyecto \"$nombre\" con ${equiposAsignados.size} equipo(s) reservado(s). Prepara el Checklist de Salida."
                    com.example.stagetrack.datos.repositorio.GestorNotificaciones.agregarNotificacion(
                        context = null,
                        idUsuarioActualLogueado = supervisor.id,
                        idUsuarioDestino = "bodega",
                        titulo = tituloBodega,
                        mensaje = mensajeBodega,
                        tipo = com.example.stagetrack.dominio.modelo.TipoNotificacion.SISTEMA,
                        idEntidadRelacionada = idEvt
                    )
                    viewModelScope.launch {
                        try {
                            if (context != null) {
                                com.example.stagetrack.core.notificaciones.UtilidadFcmPush.enviarNotificacionPushDirecta(
                                    context = context,
                                    fcmTokenDestino = "",
                                    titulo = tituloBodega,
                                    mensaje = mensajeBodega,
                                    tipo = "SISTEMA",
                                    idUsuarioDestino = "bodega"
                                )
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
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

    fun asignarPersonalAEvento(
        context: Context? = null,
        evento: Evento,
        jefeBodega: Usuario?,
        tecnicos: List<Usuario>,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val eventoActualizado = evento.copy(
                idJefeBodega = jefeBodega?.id,
                nombreJefeBodega = jefeBodega?.nombre,
                idsTecnicosAsignados = tecnicos.map { it.id }
            )
            val res = repositorioEvento.crearNuevoEvento(eventoActualizado)
            if (res is Resultado.Exito) {
                tecnicos.forEach { tec ->
                    val titulo = "Convocatoria a Evento"
                    val mensaje = "Has sido asignado al proyecto \"${evento.nombre}\" en ${evento.ubicacion}."
                    com.example.stagetrack.datos.repositorio.GestorNotificaciones.agregarNotificacion(
                        context = null,
                        idUsuarioActualLogueado = evento.idSupervisor,
                        idUsuarioDestino = tec.id,
                        titulo = titulo,
                        mensaje = mensaje,
                        tipo = com.example.stagetrack.dominio.modelo.TipoNotificacion.ASIGNACION_EVENTO,
                        idEntidadRelacionada = evento.id
                    )
                    viewModelScope.launch {
                        try {
                            if (context != null) {
                                com.example.stagetrack.core.notificaciones.UtilidadFcmPush.enviarNotificacionPushDirecta(
                                    context = context,
                                    fcmTokenDestino = "",
                                    titulo = titulo,
                                    mensaje = mensaje,
                                    tipo = "ASIGNACION_EVENTO",
                                    idUsuarioDestino = tec.id
                                )
                            }
                        } catch (_: Exception) {}
                    }
                }

                if (jefeBodega != null) {
                    val tituloBodega = "Asignación como Jefe de Bodega"
                    val mensajeBodega = "Has sido designado responsable de Bodega para el evento \"${evento.nombre}\"."
                    com.example.stagetrack.datos.repositorio.GestorNotificaciones.agregarNotificacion(
                        context = null,
                        idUsuarioActualLogueado = evento.idSupervisor,
                        idUsuarioDestino = jefeBodega.id,
                        titulo = tituloBodega,
                        mensaje = mensajeBodega,
                        tipo = com.example.stagetrack.dominio.modelo.TipoNotificacion.SISTEMA,
                        idEntidadRelacionada = evento.id
                    )
                    viewModelScope.launch {
                        try {
                            if (context != null) {
                                com.example.stagetrack.core.notificaciones.UtilidadFcmPush.enviarNotificacionPushDirecta(
                                    context = context,
                                    fcmTokenDestino = "",
                                    titulo = tituloBodega,
                                    mensaje = mensajeBodega,
                                    tipo = "SISTEMA",
                                    idUsuarioDestino = jefeBodega.id
                                )
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
            onResultado(res)
        }
    }

    fun reservarEquiposAEvento(
        context: Context? = null,
        evento: Evento,
        equipos: List<Equipo>,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val eventoActualizado = evento.copy(
                idsEquiposAsignados = equipos.map { it.id }
            )
            val res = repositorioEvento.crearNuevoEvento(eventoActualizado)
            if (res is Resultado.Exito && equipos.isNotEmpty()) {
                val destBodega = evento.idJefeBodega ?: "bodega"
                val titulo = "Lista de Empaque Actualizada"
                val mensaje = "Se reservaron ${equipos.size} equipo(s) para el proyecto \"${evento.nombre}\". Prepara el Checklist de Salida."
                com.example.stagetrack.datos.repositorio.GestorNotificaciones.agregarNotificacion(
                    context = null,
                    idUsuarioActualLogueado = evento.idSupervisor,
                    idUsuarioDestino = destBodega,
                    titulo = titulo,
                    mensaje = mensaje,
                    tipo = com.example.stagetrack.dominio.modelo.TipoNotificacion.SISTEMA,
                    idEntidadRelacionada = evento.id
                )
                viewModelScope.launch {
                    try {
                        if (context != null) {
                            com.example.stagetrack.core.notificaciones.UtilidadFcmPush.enviarNotificacionPushDirecta(
                                context = context,
                                fcmTokenDestino = "",
                                titulo = titulo,
                                mensaje = mensaje,
                                tipo = "SISTEMA",
                                idUsuarioDestino = destBodega
                            )
                        }
                    } catch (_: Exception) {}
                }
            }
            onResultado(res)
        }
    }

    fun actualizarEventoExistente(
        eventoActualizado: Evento,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioEvento.actualizarEvento(eventoActualizado)
            onResultado(res)
        }
    }
}
