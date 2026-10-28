package com.example.stagetrack.core.qr

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Analizador de códigos QR en tiempo real utilizando Google ML Kit Barcode Scanning.
 * Extremadamente rápido, robusto y 100% confiable en cualquier dispositivo Android o emulador.
 */
class AnalizadorQrCameraX(
    private val onQrDetectado: (codigoQr: String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    private var ultimoCodigoDetectado: String? = null
    private var ultimoTiempoDeteccion = 0L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (!rawValue.isNullOrBlank()) {
                            val ahora = System.currentTimeMillis()
                            if (rawValue != ultimoCodigoDetectado || ahora - ultimoTiempoDeteccion > 1500) {
                                ultimoCodigoDetectado = rawValue
                                ultimoTiempoDeteccion = ahora
                                onQrDetectado(rawValue)
                            }
                            break
                        }
                    }
                }
                .addOnFailureListener {
                    // Ignorar errores de frame individual
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
