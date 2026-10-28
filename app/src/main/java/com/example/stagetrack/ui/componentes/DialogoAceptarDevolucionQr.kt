package com.example.stagetrack.ui.componentes

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.ui.viewmodel.EquipoViewModel

/**
 * Diálogo para el Jefe de Bodega que integra la cámara QR para validar físicamente
 * la devolución de un equipo e reingresarlo al inventario disponible de bodega.
 */
@Composable
fun DialogoAceptarDevolucionQr(
    equipo: Equipo,
    equipoViewModel: EquipoViewModel,
    onDescartar: () -> Unit,
    onDevolucionExitosa: () -> Unit
) {
    val context = LocalContext.current
    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDescartar,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Validar Devolución con QR", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Equipo a recibir:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = equipo.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "QR Esperado: ${equipo.codigoQR}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "Apunta la cámara a la etiqueta QR del equipo físico para confirmar su reingreso a bodega:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    QrCameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        onQrEscaneado = { codigoQrEscaneado ->
                            if (!cargando) {
                                if (codigoQrEscaneado.trim() == equipo.codigoQR.trim()) {
                                    cargando = true
                                    mensajeError = null
                                    equipoViewModel.confirmarDevolucionEnBodega(equipo.id) { res ->
                                        cargando = false
                                        when (res) {
                                            is Resultado.Exito -> {
                                                Toast.makeText(
                                                    context,
                                                    "¡Devolución confirmada! Equipo reingresado a Bodega Principal.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                onDevolucionExitosa()
                                            }
                                            is Resultado.Error -> {
                                                mensajeError = res.mensaje ?: res.excepcion.localizedMessage
                                            }
                                            is Resultado.Cargando -> {}
                                        }
                                    }
                                } else {
                                    mensajeError = "El código QR ($codigoQrEscaneado) no coincide con el esperado (${equipo.codigoQR})"
                                }
                            }
                        }
                    )
                }

                mensajeError?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        cargando = true
                        mensajeError = null
                        equipoViewModel.confirmarDevolucionEnBodega(equipo.id) { res ->
                            cargando = false
                            when (res) {
                                is Resultado.Exito -> {
                                    Toast.makeText(
                                        context,
                                        "¡Devolución confirmada manualmente! Equipo reingresado a Bodega.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onDevolucionExitosa()
                                }
                                is Resultado.Error -> {
                                    mensajeError = res.mensaje ?: res.excepcion.localizedMessage
                                }
                                is Resultado.Cargando -> {}
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirmar Devolución Manualmente", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDescartar) {
                Text("Cancelar")
            }
        }
    )
}
