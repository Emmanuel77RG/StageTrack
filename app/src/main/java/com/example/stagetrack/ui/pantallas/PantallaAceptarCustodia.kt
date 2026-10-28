package com.example.stagetrack.ui.pantallas

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.io.File
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.core.imagen.UtilidadImagen
import com.example.stagetrack.core.imagen.recordarBitmapEquipo
import com.example.stagetrack.core.ubicacion.UtilidadUbicacionGps
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.Usuario
import com.example.stagetrack.ui.componentes.QrCameraPreview
import com.example.stagetrack.ui.viewmodel.EquipoViewModel

/**
 * Pantalla que permite al usuario receptor (ej. Técnico) confirmar la recepción
 * y tomar la custodia activa de un equipo, exigiendo la validación física de lectura QR.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAceptarCustodia(
    equipo: Equipo,
    usuarioActual: Usuario,
    equipoViewModel: EquipoViewModel,
    qrVerificadoInicial: Boolean = false,
    onVolverClick: () -> Unit,
    onRecepcionExitosa: () -> Unit
) {
    val context = LocalContext.current
    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var qrVerificado by remember { mutableStateOf(qrVerificadoInicial) }
    var mostrandoEscanerQr by remember { mutableStateOf(false) }

    if (mostrandoEscanerQr) {
        Dialog(
            onDismissRequest = { mostrandoEscanerQr = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Apunta al código QR impreso en '${equipo.nombre}' para validar la entrega física:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    QrCameraPreview(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        onQrEscaneado = { codigo ->
                            val coincide = codigo.equals(equipo.codigoQR, ignoreCase = true) ||
                                    codigo.equals(equipo.numeroSerie, ignoreCase = true) ||
                                    codigo.equals(equipo.id, ignoreCase = true)
                            if (coincide) {
                                qrVerificado = true
                                mensajeError = null
                                mostrandoEscanerQr = false
                            } else {
                                mensajeError = "El código QR escaneado ($codigo) no coincide con este equipo (${equipo.nombre})."
                                mostrandoEscanerQr = false
                            }
                        }
                    )

                    OutlinedButton(
                        onClick = {
                            qrVerificado = true
                            mensajeError = null
                            mostrandoEscanerQr = false
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("⚡ Simular Escaneo Exitoso (Pruebas)")
                    }

                    OutlinedButton(
                        onClick = { mostrandoEscanerQr = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar Escaneo")
                    }
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Aceptar Custodia de Equipo", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onVolverClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Ficha del Equipo
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Detalles del Equipo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val bitmapEquipo = recordarBitmapEquipo(context, equipo.urlImagen)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (bitmapEquipo != null) {
                                        Image(
                                            bitmap = bitmapEquipo.asImageBitmap(),
                                            contentDescription = "Foto de ${equipo.nombre}",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Inventory,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = equipo.nombre,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Categoría: ${equipo.categoria}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Número de Serie: ${equipo.numeroSerie}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Código QR: ${equipo.codigoQR}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Card de Validación Obligatoria por QR
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (qrVerificado)
                                MaterialTheme.colorScheme.tertiaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (qrVerificado) Icons.Default.CheckCircle else Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = if (qrVerificado) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (qrVerificado) "Verificación Física de QR: Exitosa" else "Validación de QR Obligatoria",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (qrVerificado) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (qrVerificado)
                                    "Se ha verificado la lectura física del código QR registrado para este equipo."
                                else
                                    "Para garantizar el control de stock, debes escanear el código QR grabado en el equipo físico antes de aceptar la custodia.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (qrVerificado) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                            )

                            if (!qrVerificado) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = { mostrandoEscanerQr = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Escanear QR de Equipo para Validar", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Ficha de Custodio Destino
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Custodio Destino Registrado",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Receptor: ${usuarioActual.nombre}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Correo: ${usuarioActual.correo}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Al presionar la confirmación, certificarás la recepción de este activo y la custodia operativa quedará asignada formalmente a tu nombre.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    mensajeError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        enabled = qrVerificado && !cargando,
                        onClick = {
                            cargando = true
                            mensajeError = null
                            val coords = UtilidadUbicacionGps.obtenerCoordenadasGpsActuales(context)
                            equipoViewModel.confirmarRecepcionCustodia(
                                idEquipo = equipo.id,
                                idUsuarioReceptor = usuarioActual.id,
                                nombreUsuarioReceptor = usuarioActual.nombre
                            ) { res ->
                                cargando = false
                                when (res) {
                                    is Resultado.Exito -> onRecepcionExitosa()
                                    is Resultado.Error -> mensajeError = res.mensaje ?: res.excepcion.localizedMessage
                                    is Resultado.Cargando -> {}
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (qrVerificado) "Confirmar Recepción y Aceptar Custodia" else "Debes Validar QR para Confirmar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
