package com.example.stagetrack.core.notificaciones

import android.util.Log
import com.example.stagetrack.datos.repositorio.GestorNotificaciones
import com.example.stagetrack.dominio.modelo.TipoNotificacion
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Servicio en segundo plano para recibir mensajes Push de Firebase Cloud Messaging (FCM),
 * incluso cuando la aplicación está cerrada.
 */
class ServicioMensajeriaFirebase : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("ServicioFirebase", "Nuevo FCM Token de dispositivo: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("ServicioFirebase", "Mensaje Push FCM recibido: ${remoteMessage.data}")

        val titulo = remoteMessage.notification?.title ?: remoteMessage.data["titulo"] ?: "StageTrack"
        val mensaje = remoteMessage.notification?.body ?: remoteMessage.data["mensaje"] ?: "Nueva notificación recibida"
        val tipoStr = remoteMessage.data["tipo"] ?: "SISTEMA"

        val tipo = try {
            TipoNotificacion.valueOf(tipoStr)
        } catch (_: Exception) {
            TipoNotificacion.SISTEMA
        }

        val idDestino = remoteMessage.data["idUsuarioDestino"] ?: "todos"

        // Agregar al historial in-app de forma silenciosa
        GestorNotificaciones.agregarNotificacion(
            context = null,
            idUsuarioDestino = idDestino,
            titulo = titulo,
            mensaje = mensaje,
            tipo = tipo
        )

        // Dibujar notificación nativa en la bandeja del dispositivo
        val idNotif = (System.currentTimeMillis() % 100000).toInt()
        UtilidadNotificaciones.mostrarNotificacionSistema(
            context = applicationContext,
            idNotificacion = idNotif,
            titulo = titulo,
            mensaje = mensaje
        )
    }
}
