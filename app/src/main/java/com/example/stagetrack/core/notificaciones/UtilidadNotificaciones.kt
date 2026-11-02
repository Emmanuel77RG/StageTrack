package com.example.stagetrack.core.notificaciones

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.stagetrack.MainActivity
import com.example.stagetrack.R

/**
 * Utilidad encargada de crear canales y emitir notificaciones nativas de Android.
 */
object UtilidadNotificaciones {

    const val ID_CANAL = "canal_notificaciones_stagetrack_v2"
    const val NOMBRE_CANAL = "Alertas StageTrack"
    const val DESCRIPCION_CANAL = "Alertas de transferencias de custodia y asignación de eventos"

    fun crearCanalNotificaciones(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importancia = NotificationManager.IMPORTANCE_HIGH
            val canal = NotificationChannel(ID_CANAL, NOMBRE_CANAL, importancia).apply {
                description = DESCRIPCION_CANAL
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(canal)
        }
    }

    @Suppress("MissingPermission")
    @SuppressLint("MissingPermission")
    fun mostrarNotificacionSistema(
        context: Context,
        idNotificacion: Int,
        titulo: String,
        mensaje: String
    ) {
        try {
            crearCanalNotificaciones(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                idNotificacion,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, ID_CANAL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    notificationManager.notify(idNotificacion, builder.build())
                }
            } else {
                notificationManager.notify(idNotificacion, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
