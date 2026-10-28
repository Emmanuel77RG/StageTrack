package com.example.stagetrack.ui.componentes

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.stagetrack.core.cripto.UtilidadHash
import java.util.concurrent.Executor

/**
 * Composable que integra CameraX para la captura de fotos de evidencia
 * y el cálculo automático local de su firma criptográfica SHA-256.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onFotoCapturada: (bitmap: Bitmap, hashSha256: String) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var tienePermisoCamara by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcherPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        tienePermisoCamara = concedido
    }

    LaunchedEffect(Unit) {
        if (!tienePermisoCamara) {
            launcherPermiso.launch(Manifest.permission.CAMERA)
        }
    }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var fotoPreviewBitmap: Bitmap? by remember { mutableStateOf(null) }
    var hashSha256Calculado: String? by remember { mutableStateOf(null) }

    if (!tienePermisoCamara) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Se requiere permiso de cámara para capturar la evidencia fotográfica.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { launcherPermiso.launch(Manifest.permission.CAMERA) }) {
                    Text("Conceder Permiso")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onCancelar) {
                    Text("Cancelar")
                }
            }
        }
    } else if (fotoPreviewBitmap != null && hashSha256Calculado != null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Previsualización de Evidencia",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Image(
                bitmap = fotoPreviewBitmap!!.asImageBitmap(),
                contentDescription = "Evidencia de daño",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Firma SHA-256 Local:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = hashSha256Calculado!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = {
                    fotoPreviewBitmap = null
                    hashSha256Calculado = null
                }) {
                    Text("Reintentar")
                }

                Button(onClick = {
                    onFotoCapturada(fotoPreviewBitmap!!, hashSha256Calculado!!)
                }) {
                    Text("Usar Foto")
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            Log.e("CameraPreview", "Error al vincular CameraX: ${e.message}", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(onClick = onCancelar) {
                    Text("Cancelar")
                }

                Button(onClick = {
                    val capture = imageCapture ?: return@Button
                    val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

                    capture.takePicture(
                        mainExecutor,
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val rotationDegrees = image.imageInfo.rotationDegrees
                                val buffer = image.planes[0].buffer
                                val bytes = ByteArray(buffer.remaining())
                                buffer.get(bytes)
                                image.close()

                                val rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                val rotatedBitmap = if (rotationDegrees != 0 && rawBitmap != null) {
                                    val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                                    Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
                                } else {
                                    rawBitmap
                                }

                                val stream = java.io.ByteArrayOutputStream()
                                rotatedBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                                val finalBytes = stream.toByteArray()
                                val hashCalculado = UtilidadHash.calcularSha256(finalBytes)

                                fotoPreviewBitmap = rotatedBitmap
                                hashSha256Calculado = hashCalculado
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraPreview", "Error capturando imagen: ${exception.message}", exception)
                            }
                        }
                    )
                }) {
                    Text("Tomar Foto")
                }
            }
        }
    }
}
