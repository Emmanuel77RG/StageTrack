package com.example.stagetrack.datos.repositorio

import android.content.Context
import com.example.stagetrack.core.notificaciones.UtilidadNotificaciones
import com.example.stagetrack.dominio.modelo.NotificacionApp
import com.example.stagetrack.dominio.modelo.TipoNotificacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor singleton de notificaciones locales e historial para el usuario actual.
 */
object GestorNotificaciones {

    var repositorioAuthFirebase: RepositorioAutenticacionFirebase? = null
    var contextAplicacion: Context? = null
    var idUsuarioLogueadoActual: String? = null
    var usuarioRolLogueadoActual: com.example.stagetrack.dominio.modelo.RolUsuario? = null

    private val _notificaciones = MutableStateFlow<List<NotificacionApp>>(emptyList())
    val notificaciones: StateFlow<List<NotificacionApp>> = _notificaciones.asStateFlow()

    fun agregarNotificacion(
        context: Context? = null,
        idUsuarioActualLogueado: String? = null,
        idUsuarioDestino: String,
        titulo: String,
        mensaje: String,
        tipo: TipoNotificacion,
        idEntidadRelacionada: String? = null
    ) {
        val nuevaNotif = NotificacionApp(
            idUsuarioDestino = idUsuarioDestino,
            titulo = titulo,
            mensaje = mensaje,
            tipo = tipo,
            idEntidadRelacionada = idEntidadRelacionada
        )

        val idUsuarioLocal = idUsuarioLogueadoActual ?: idUsuarioActualLogueado
        val esJefeBodega = usuarioRolLogueadoActual == com.example.stagetrack.dominio.modelo.RolUsuario.JEFE_BODEGA

        val esParaEsteUsuarioLocal = idUsuarioLocal != null && (
            idUsuarioLocal == idUsuarioDestino ||
            (idUsuarioDestino == "bodega" && esJefeBodega) ||
            idUsuarioDestino == "todos"
        )

        // Agregar al historial in-app local ÚNICAMENTE si la notificación pertenece a este usuario activo
        if (esParaEsteUsuarioLocal) {
            _notificaciones.value = listOf(nuevaNotif) + _notificaciones.value
        }

        // Si la notificación es enviada por este usuario HACIA OTRO usuario, publicarla en Firestore
        if (idUsuarioActualLogueado != null && idUsuarioActualLogueado != idUsuarioDestino) {
            repositorioAuthFirebase?.publicarNotificacionEnFirestore(
                idUsuarioEmisor = idUsuarioActualLogueado,
                idUsuarioDestino = idUsuarioDestino,
                titulo = titulo,
                mensaje = mensaje,
                tipo = tipo.name,
                idEntidadRelacionada = idEntidadRelacionada
            )
        }

        // Solo mostrar la notificación nativa del sistema si este dispositivo pertenece al destinatario
        val esParaEsteDispositivo = idUsuarioActualLogueado == null ||
                idUsuarioActualLogueado == idUsuarioDestino ||
                (idUsuarioDestino == "bodega" && esJefeBodega)

        if (context != null && esParaEsteDispositivo) {
            val idNum = (nuevaNotif.fechaTimestamp % 100000).toInt()
            UtilidadNotificaciones.mostrarNotificacionSistema(
                context = context,
                idNotificacion = idNum,
                titulo = titulo,
                mensaje = mensaje
            )
        }
    }

    fun marcarComoLeida(idNotificacion: String) {
        _notificaciones.value = _notificaciones.value.map { n ->
            if (n.id == idNotificacion) n.copy(leida = true) else n
        }
    }

    fun marcarTodasComoLeidas(idUsuario: String) {
        val esJefeBodega = usuarioRolLogueadoActual == com.example.stagetrack.dominio.modelo.RolUsuario.JEFE_BODEGA
        _notificaciones.value = _notificaciones.value.map { n ->
            if (n.idUsuarioDestino == idUsuario || (n.idUsuarioDestino == "bodega" && esJefeBodega) || n.idUsuarioDestino == "todos") n.copy(leida = true) else n
        }
    }
}
