package com.example.stagetrack.ui.componentes

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Diálogo interactivo diseñado para ajustar la foto de perfil mediante gestos táctiles de pellizco (zoom),
 * arrastre (pan) y rotación.
 */
@Composable
fun DialogoAjustarImagen(
    uriImagen: Uri,
    onDismiss: () -> Unit,
    onConfirmar: (
        rotacionGrados: Float,
        escala: Float,
        offsetX: Float,
        offsetY: Float,
        tamanoVistaPx: Float
    ) -> Unit
) {
    var escala by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotacionGrados by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustar Foto de Perfil") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Usa tus dedos para pellizcar (zoom), arrastrar o rotar la imagen dentro del círculo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Área de previsualización circular interactiva con detección de gestos táctiles
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, gestureRotation ->
                                escala = (escala * zoom).coerceIn(0.8f, 3.5f)
                                rotacionGrados = (rotacionGrados + gestureRotation) % 360f
                                offset += pan
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = uriImagen,
                        contentDescription = "Previsualización interactiva de la foto",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(220.dp)
                            .graphicsLayer(
                                scaleX = escala,
                                scaleY = escala,
                                rotationZ = rotacionGrados,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Controles auxiliares: Restablecer y Rotar 90°
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            escala = 1.0f
                            offset = Offset.Zero
                            rotacionGrados = 0f
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Restablecer")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restablecer")
                    }

                    OutlinedButton(
                        onClick = { rotacionGrados = (rotacionGrados + 90f) % 360f }
                    ) {
                        Icon(imageVector = Icons.Default.RotateRight, contentDescription = "Rotar 90°")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rotar 90°")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tamanoVistaPx = with(density) { 220.dp.toPx() }
                    onConfirmar(rotacionGrados, escala, offset.x, offset.y, tamanoVistaPx)
                }
            ) {
                Text("Guardar Foto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
