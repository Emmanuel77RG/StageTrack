package com.example.stagetrack.core.notificaciones

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.stagetrack.datos.repositorio.GestorNotificaciones
import com.example.stagetrack.dominio.modelo.TipoNotificacion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Trabajador en segundo plano registrado en el sistema operativo Android (WorkManager)
 * que consulta Firestore y emite notificaciones en la bandeja superior del teléfono
 * incluso cuando StageTrack está totalmente cerrada.
 */
class NotificacionBackgroundWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("notificaciones")
                .whereEqualTo("leida", false)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents.forEach { doc ->
                    val idDestino = doc.getString("idUsuarioDestino") ?: ""
                    val titulo = doc.getString("titulo") ?: "StageTrack"
                    val mensaje = doc.getString("mensaje") ?: "Nueva actualización de inventario"
                    val tipoStr = doc.getString("tipo") ?: "SISTEMA"

                    val tipo = try {
                        TipoNotificacion.valueOf(tipoStr)
                    } catch (_: Exception) {
                        TipoNotificacion.SISTEMA
                    }

                    // Emitir notificación nativa a la bandeja del dispositivo
                    val idNum = (System.currentTimeMillis() % 100000).toInt()
                    UtilidadNotificaciones.mostrarNotificacionSistema(
                        context = appContext,
                        idNotificacion = idNum,
                        titulo = titulo,
                        mensaje = mensaje
                    )

                    GestorNotificaciones.agregarNotificacion(
                        context = null,
                        idUsuarioDestino = idDestino,
                        titulo = titulo,
                        mensaje = mensaje,
                        tipo = tipo
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.w("NotifBackgroundWorker", "Aviso en ejecucion WorkManager segundo plano: ${e.message}")
            Result.retry()
        }
    }
}
