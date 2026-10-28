package com.example.stagetrack.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.ModoEscaneoQr
import com.example.stagetrack.ui.componentes.QrCameraPreview

/**
 * Pantalla operativa de escaneo de códigos QR en tiempo real con selector rápido de respaldo para pruebas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEscaneoQr(
    modo: ModoEscaneoQr = ModoEscaneoQr.CONSULTAR_FICHA,
    equipos: List<Equipo> = emptyList(),
    onVolverClick: () -> Unit,
    onQrEscaneado: (codigoQr: String) -> Unit
) {
    var codigoManual by remember { mutableStateOf("") }

    val tituloTopBar = when (modo) {
        ModoEscaneoQr.CONSULTAR_FICHA -> "Consulta de Ficha Técnica"
        ModoEscaneoQr.RECEPCION_CUSTODIA -> "Recepción de Custodia"
        ModoEscaneoQr.DESPACHO_TRASPASO -> "Despacho o Traspaso"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tituloTopBar, fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner de Indicación de Modo Activo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (modo) {
                        ModoEscaneoQr.CONSULTAR_FICHA -> MaterialTheme.colorScheme.secondaryContainer
                        ModoEscaneoQr.RECEPCION_CUSTODIA -> MaterialTheme.colorScheme.tertiaryContainer
                        ModoEscaneoQr.DESPACHO_TRASPASO -> MaterialTheme.colorScheme.primaryContainer
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (modo) {
                            ModoEscaneoQr.CONSULTAR_FICHA -> Icons.Default.Info
                            ModoEscaneoQr.RECEPCION_CUSTODIA -> Icons.Default.MoveToInbox
                            ModoEscaneoQr.DESPACHO_TRASPASO -> Icons.Default.QrCodeScanner
                        },
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = when (modo) {
                                ModoEscaneoQr.CONSULTAR_FICHA -> "Modo Consulta: Inspeccionar datos del activo"
                                ModoEscaneoQr.RECEPCION_CUSTODIA -> "Modo Recepción: Confirmar entrada / custodia"
                                ModoEscaneoQr.DESPACHO_TRASPASO -> "Modo Despacho: Iniciar entrega de equipo"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Apunta la cámara al código QR o selecciona abajo un equipo.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Visor de Cámara activo en tiempo real
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                QrCameraPreview(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    onQrEscaneado = onQrEscaneado
                )
            }

            // Selector Rápido de Respaldo (para dispositivos con cámara limitada en pruebas)
            if (equipos.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Selección rápida (Respaldo si la cámara no enfoca):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(equipos) { eq ->
                                AssistChip(
                                    onClick = { onQrEscaneado(eq.codigoQR) },
                                    label = { Text("${eq.nombre} (${eq.numeroSerie})") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Opción de búsqueda manual de respaldo
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "O ingresa el código manualmente:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = codigoManual,
                    onValueChange = { codigoManual = it },
                    label = { Text("Código QR o Número de Serie") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    enabled = codigoManual.isNotBlank(),
                    onClick = { onQrEscaneado(codigoManual.trim()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Procesar Código", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
