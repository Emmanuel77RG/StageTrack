package com.example.stagetrack.core.imagen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Utilidad encargada del procesamiento, compresión, codificación Base64
 * y decodificación híbrida universal (archivos, Base64 y descargas de red HTTP/HTTPS) de imágenes.
 */
object UtilidadImagen {

    /**
     * Convierte y comprime un Bitmap a una cadena en formato Base64.
     */
    fun bitmapABase64(bitmap: Bitmap, calidad: Int = 75): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, calidad, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * Extrae los bytes en formato JPEG de cualquier fuente (Base64, URI o Archivo local).
     */
    fun obtenerBytesImagen(context: Context, pathOrBase64: String?): ByteArray? {
        if (pathOrBase64.isNullOrBlank()) return null
        val input = pathOrBase64.trim()

        try {
            val limpiaBase64 = if (input.contains(",")) input.substringAfter(",") else input
            if (limpiaBase64.length > 100 && !input.startsWith("http") && !input.startsWith("content:")) {
                val bytes = Base64.decode(limpiaBase64, Base64.DEFAULT)
                if (bytes != null && bytes.isNotEmpty()) return bytes
            }
        } catch (_: Exception) {}

        try {
            if (input.startsWith("content://") || input.startsWith("file://")) {
                val uri = Uri.parse(input)
                return context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
        } catch (_: Exception) {}

        try {
            val file = File(input)
            if (file.exists()) return file.readBytes()
        } catch (_: Exception) {}

        return null
    }

    /**
     * Procesa, rota y comprime una imagen seleccionada desde una URI para su subida.
     */
    fun procesarYComprimirImagen(
        context: Context,
        uri: Uri,
        rotacionAdicionalGrados: Float = 0f,
        escala: Float = 1.0f,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        tamanoVistaPx: Float = 0f
    ): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            var bitmap = BitmapFactory.decodeStream(inputStream) ?: return null

            if (rotacionAdicionalGrados != 0f) {
                val matrix = Matrix()
                matrix.postRotate(rotacionAdicionalGrados)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.toByteArray()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Decodifica una imagen desde cualquier fuente híbrida (red HTTPS, Base64, URI o File).
     */
    fun decodificarImagenHibrida(context: Context, pathOrBase64: String?): Bitmap? {
        if (pathOrBase64.isNullOrBlank()) return null

        val input = pathOrBase64.trim()

        // 1. Descarga desde red HTTP / HTTPS (Supabase Storage)
        if (input.startsWith("http://") || input.startsWith("https://")) {
            try {
                val url = URL(input)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                connection.connect()
                connection.inputStream.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) return bmp
                }
            } catch (_: Exception) {
                // Continuar si no se pudo descargar de red
            }
        }

        // 2. Intentar decodificar como Base64 (los JPEGs en Base64 frecuentemente empiezan por /9j/)
        try {
            val limpiaBase64 = if (input.contains(",")) {
                input.substringAfter(",")
            } else {
                input
            }
            if (limpiaBase64.length > 100 && !input.startsWith("http") && !input.startsWith("content:")) {
                val decodedBytes = Base64.decode(limpiaBase64, Base64.DEFAULT)
                if (decodedBytes != null && decodedBytes.isNotEmpty()) {
                    val bmp = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    if (bmp != null) return bmp
                }
            }
        } catch (_: Exception) {
            // Continuar con los siguientes métodos si no era Base64 válido
        }

        // 3. Intentar decodificar como URI del sistema
        if (input.startsWith("content://") || input.startsWith("file://")) {
            try {
                val uri = Uri.parse(input)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) return bmp
                }
            } catch (_: Exception) {
                // Continuar
            }
        }

        // 4. Intentar decodificar como archivo local
        try {
            val archivo = File(input)
            if (archivo.exists()) {
                val bmp = BitmapFactory.decodeFile(archivo.absolutePath)
                if (bmp != null) return bmp
            }
        } catch (_: Exception) {
            // Continuar
        }

        // 5. Intentar decodificación final con ContentResolver
        try {
            val uri = Uri.parse(input)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bmp = BitmapFactory.decodeStream(stream)
                if (bmp != null) return bmp
            }
        } catch (_: Exception) {
            // Nulo si falla
        }

        return null
    }
}

/**
 * Helper Composable para cargar y recordar un Bitmap de forma asíncrona en hilo de red (Dispatchers.IO).
 */
@Composable
fun recordarBitmapEquipo(context: Context, urlImagen: String?): Bitmap? {
    val state = produceState<Bitmap?>(initialValue = null, key1 = urlImagen) {
        value = withContext(Dispatchers.IO) {
            UtilidadImagen.decodificarImagenHibrida(context, urlImagen)
        }
    }
    return state.value
}
