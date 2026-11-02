package com.example.stagetrack.core.notificaciones

import android.content.Context
import android.util.Log
import com.example.stagetrack.R
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Utilidad encargada de enviar notificaciones Push mediante la API oficial FCM V1 de Google,
 * autenticada con la Clave de Cuenta de Servicio privada.
 */
object UtilidadFcmPush {

    private const val FCM_V1_URL = "https://fcm.googleapis.com/v1/projects/stagetrack-ee371/messages:send"
    private const val MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging"

    @Volatile
    private var tokenOAuthCache: String? = null

    @Volatile
    private var expiraTokenTimestamp: Long = 0

    private fun obtenerAccessTokenGoogle(context: Context): String? {
        val ahora = System.currentTimeMillis()
        if (tokenOAuthCache != null && ahora < (expiraTokenTimestamp - 60000)) {
            return tokenOAuthCache
        }

        return try {
            val resId = context.resources.getIdentifier(
                "stagetrack_ee371_firebase_adminsdk_fbsvc_af887b08e5",
                "raw",
                context.packageName
            )
            if (resId == 0) return null
            val inputStream: InputStream = context.resources.openRawResource(resId)
            val credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf(MESSAGING_SCOPE))
            credentials.refreshIfExpired()
            val accessToken = credentials.accessToken
            tokenOAuthCache = accessToken.tokenValue
            expiraTokenTimestamp = accessToken.expirationTime.time
            Log.d("UtilidadFcmPush", "Token OAuth2 FCM V1 generado exitosamente con Google Admin SDK")
            tokenOAuthCache
        } catch (e: Exception) {
            Log.e("UtilidadFcmPush", "Aviso Token OAuth2 FCM V1: ${e.message}")
            null
        }
    }

    suspend fun enviarNotificacionPushDirecta(
        context: Context,
        fcmTokenDestino: String = "",
        titulo: String,
        mensaje: String,
        tipo: String = "TRANSFERENCIA_CUSTODIA",
        idUsuarioDestino: String
    ) = withContext(Dispatchers.IO) {
        val accessToken = obtenerAccessTokenGoogle(context.applicationContext)
        if (accessToken.isNullOrBlank()) {
            Log.w("UtilidadFcmPush", "No se pudo obtener Access Token OAuth2 para FCM V1.")
            return@withContext
        }

        val destinoFinal = when {
            fcmTokenDestino.isNotBlank() && fcmTokenDestino.length > 20 -> fcmTokenDestino
            idUsuarioDestino.isNotBlank() -> "usuario_$idUsuarioDestino"
            else -> "todos"
        }

        try {
            val url = URL(FCM_V1_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Authorization", "Bearer $accessToken")
            conn.doOutput = true

            val jsonMessage = JSONObject().apply {
                if (destinoFinal.startsWith("usuario_") || destinoFinal == "todos") {
                    put("topic", destinoFinal)
                } else {
                    put("token", destinoFinal)
                }

                put("notification", JSONObject().apply {
                    put("title", titulo)
                    put("body", mensaje)
                })

                put("data", JSONObject().apply {
                    put("titulo", titulo)
                    put("mensaje", mensaje)
                    put("tipo", tipo)
                    put("idUsuarioDestino", idUsuarioDestino)
                })

                put("android", JSONObject().apply {
                    put("priority", "HIGH")
                    put("notification", JSONObject().apply {
                        put("sound", "default")
                        put("channel_id", UtilidadNotificaciones.ID_CANAL)
                    })
                })
            }

            val jsonRoot = JSONObject().apply {
                put("message", jsonMessage)
            }

            val writer = OutputStreamWriter(conn.outputStream, "UTF-8")
            writer.write(jsonRoot.toString())
            writer.flush()
            writer.close()

            val responseCode = conn.responseCode
            val responseMsg = if (responseCode == 200) "EXITO" else conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            Log.d("UtilidadFcmPush", "Petición FCM V1 enviada a Google ($destinoFinal). Respuesta HTTP: $responseCode ($responseMsg)")
        } catch (e: Exception) {
            Log.e("UtilidadFcmPush", "Error al despachar mensaje FCM V1 a Google: ${e.message}", e)
        }
    }
}
