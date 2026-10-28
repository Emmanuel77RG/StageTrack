package com.example.stagetrack.ui.pantallas

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import com.example.stagetrack.ui.componentes.InsigniaSha256
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.NivelSeveridadDano
import com.example.stagetrack.ui.componentes.CameraPreview
import com.example.stagetrack.ui.viewmodel.AutenticacionViewModel
import com.example.stagetrack.ui.viewmodel.EquipoViewModel
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaReporteDano(
    equipo: Equipo,
    autenticacionViewModel: AutenticacionViewModel,
    equipoViewModel: EquipoViewModel,
    onVolverClick: () -> Unit,
    onReporteExitoso: () -> Unit
) {
    val usuarioReportador by autenticacionViewModel.usuarioActual.collectAsState()

    var descripcionDano by remember { mutableStateOf("") }
    var severidadSeleccionada by remember { mutableStateOf(NivelSeveridadDano.MODERADO) }
    var mostrandoCamara by remember { mutableStateOf(false) }
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var bytesFoto by remember { mutableStateOf<ByteArray?>(null) }
    var hashSha256Foto by remember { mutableStateOf<String?>(null) }

    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    if (mostrandoCamara) {
        CameraPreview(
            onFotoCapturada = { bitmap, hash ->
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                bytesFoto = stream.toByteArray()
                fotoBitmap = bitmap
                hashSha256Foto = hash
                mostrandoCamara = false
            },
            onCancelar = { mostrandoCamara = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Reportar Daño de Equipo") },
                    navigationIcon = {
                        IconButton(onClick = onVolverClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Equipo Afectado:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = equipo.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "N/S: ${equipo.numeroSerie} | Categoría: ${equipo.categoria}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Text(
                    text = "Reportado por: ${usuarioReportador?.nombre ?: "Sin sesión"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Nivel de Severidad del Daño:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NivelSeveridadDano.entries.forEach { severidad ->
                        FilterChip(
                            selected = severidadSeleccionada == severidad,
                            onClick = { severidadSeleccionada = severidad },
                            label = { Text(etiquetaSeveridad(severidad)) }
                        )
                    }
                }

                OutlinedTextField(
                    value = descripcionDano,
                    onValueChange = { descripcionDano = it },
                    label = { Text("Descripción detallada de la falla o daño") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Text(
                    text = "Evidencia Fotográfica del Daño (CameraX + SHA-256):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (fotoBitmap != null && hashSha256Foto != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Image(
                                bitmap = fotoBitmap!!.asImageBitmap(),
                                contentDescription = "Foto evidencia de daño",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                            InsigniaSha256(hashSha256 = hashSha256Foto!!)
                        }
                    }
                }

                Button(
                    onClick = { mostrandoCamara = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (fotoBitmap == null) "Capturar Evidencia Fotográfica" else "Cambiar Evidencia Fotográfica")
                }

                if (mensajeError != null) {
                    Text(
                        text = mensajeError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    enabled = !cargando && descripcionDano.isNotBlank() && bytesFoto != null && usuarioReportador != null,
                    onClick = {
                        cargando = true
                        mensajeError = null
                        equipoViewModel.registrarReporteDano(
                            idEquipo = equipo.id,
                            idUsuarioReportador = usuarioReportador!!.id,
                            descripcionDano = descripcionDano.trim(),
                            severidad = severidadSeleccionada,
                            bytesFotografia = bytesFoto!!,
                            hashSha256 = hashSha256Foto ?: ""
                        ) { resultado ->
                            cargando = false
                            when (resultado) {
                                is Resultado.Exito -> onReporteExitoso()
                                is Resultado.Error -> mensajeError = resultado.mensaje ?: resultado.excepcion.localizedMessage
                                is Resultado.Cargando -> {}
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (cargando) "Guardando Reporte..." else "Registrar Reporte de Daño", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun etiquetaSeveridad(severidad: NivelSeveridadDano): String {
    return when (severidad) {
        NivelSeveridadDano.LEVE -> "Leve"
        NivelSeveridadDano.MODERADO -> "Moderado"
        NivelSeveridadDano.CRITICO -> "Crítico"
    }
}
