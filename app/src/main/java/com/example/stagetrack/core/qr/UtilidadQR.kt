package com.example.stagetrack.core.qr

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.print.PrintHelper
import com.example.stagetrack.dominio.modelo.Equipo
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Utilidad para la generación de código QR en formato gráfico,
 * la creación de etiquetas impresas de 4x4 cm y su exportación en formato JPG.
 */
object UtilidadQR {

    /**
     * Genera un Bitmap en blanco y negro con el código QR codificado a partir de una cadena de texto.
     */
    fun generarBitmapQr(texto: String, tamanoPx: Int = 512): Bitmap {
        val hints = HashMap<EncodeHintType, Any>()
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        hints[EncodeHintType.MARGIN] = 1

        val bitMatrix = MultiFormatWriter().encode(
            texto,
            BarcodeFormat.QR_CODE,
            tamanoPx,
            tamanoPx,
            hints
        )

        val ancho = bitMatrix.width
        val alto = bitMatrix.height
        val pixeles = IntArray(ancho * alto)

        for (y in 0 until alto) {
            val offset = y * ancho
            for (x in 0 until ancho) {
                pixeles[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }

        val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixeles, 0, ancho, 0, 0, ancho, alto)
        return bitmap
    }

    /**
     * Crea un Bitmap optimizado con dimensiones de 4cm x 4cm a 300 DPI (472x472 px).
     * Incluye encabezado de marca, nombre del equipo, código de barras/QR y número de serie.
     */
    fun generarEtiqueta4x4cm(equipo: Equipo, dpi: Int = 300): Bitmap {
        // 4 cm = 1.5748 pulgadas -> 1.5748 * 300 DPI ≈ 472 píxeles
        val tamanoPx = (4.0 / 2.54 * dpi).toInt().coerceAtLeast(400)

        val bitmapResultado = Bitmap.createBitmap(tamanoPx, tamanoPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapResultado)

        // Fondo blanco
        canvas.drawColor(Color.WHITE)

        // Borde fino gris para delimitar la etiqueta
        val paintBorde = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRect(Rect(0, 0, tamanoPx, tamanoPx), paintBorde)

        // Encabezado "StageTrack"
        val paintMarca = Paint().apply {
            color = Color.DKGRAY
            textSize = tamanoPx * 0.055f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("STAGE TRACK", tamanoPx / 2f, tamanoPx * 0.09f, paintMarca)

        // Nombre del equipo
        val paintNombre = Paint().apply {
            color = Color.BLACK
            textSize = tamanoPx * 0.065f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val nombreCorto = if (equipo.nombre.length > 20) equipo.nombre.take(18) + "..." else equipo.nombre
        canvas.drawText(nombreCorto, tamanoPx / 2f, tamanoPx * 0.17f, paintNombre)

        // Generar QR interno
        val tamanoQrPx = (tamanoPx * 0.65f).toInt()
        val bitmapQr = generarBitmapQr(equipo.codigoQR, tamanoQrPx)

        val posX = (tamanoPx - tamanoQrPx) / 2f
        val posY = tamanoPx * 0.20f
        canvas.drawBitmap(bitmapQr, posX, posY, null)

        // Número de Serie / QR Texto al pie
        val paintTextoSerie = Paint().apply {
            color = Color.BLACK
            textSize = tamanoPx * 0.048f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("N/S: ${equipo.numeroSerie}", tamanoPx / 2f, tamanoPx * 0.89f, paintTextoSerie)

        val paintTextoQr = Paint().apply {
            color = Color.GRAY
            textSize = tamanoPx * 0.040f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(equipo.codigoQR, tamanoPx / 2f, tamanoPx * 0.95f, paintTextoQr)

        return bitmapResultado
    }

    /**
     * Exporta el Bitmap generado a la galería/almacenamiento del dispositivo en formato JPG.
     * Retorna el URI del archivo guardado o null si ocurre un error.
     */
    fun exportarImagenJpg(context: Context, bitmap: Bitmap, nombreEquipo: String): Uri? {
        val nombreLimpio = nombreEquipo.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val nombreArchivo = "QR_${nombreLimpio}_${System.currentTimeMillis()}.jpg"

        var outputStream: OutputStream? = null
        var uriResultado: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/StageTrack_QRs")
                }

                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    outputStream = resolver.openOutputStream(imageUri)
                    uriResultado = imageUri
                }
            } else {
                val directorioImagenes = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val carpetaStageTrack = File(directorioImagenes, "StageTrack_QRs")
                if (!carpetaStageTrack.exists()) {
                    carpetaStageTrack.mkdirs()
                }
                val archivoImagen = File(carpetaStageTrack, nombreArchivo)
                outputStream = FileOutputStream(archivoImagen)
                uriResultado = Uri.fromFile(archivoImagen)
            }

            outputStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return uriResultado
    }

    /**
     * Envía la etiqueta Bitmap del código QR directamente al servicio de impresión de Android
     * (soporta impresoras Wi-Fi, Bluetooth o exportar a PDF).
     */
    fun imprimirQr(context: Context, bitmap: Bitmap, nombreEquipo: String) {
        val printHelper = PrintHelper(context).apply {
            scaleMode = PrintHelper.SCALE_MODE_FIT
        }
        val nombreTrabajo = "QR_StageTrack_${nombreEquipo.take(15)}"
        printHelper.printBitmap(nombreTrabajo, bitmap)
    }
}
