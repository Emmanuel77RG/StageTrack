package com.example.stagetrack.ui.componentes

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.EstadoEquipo
import com.example.stagetrack.core.imagen.UtilidadImagen
import com.example.stagetrack.core.imagen.recordarBitmapEquipo
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.dominio.modelo.Usuario
import com.example.stagetrack.ui.pantallas.colorPorEstado
import com.example.stagetrack.ui.pantallas.obtenerEtiquetaEstado
import java.io.File

/**
 * Diálogo interactivo con la Ficha Técnica completa del activo,
 * diseñado para la inspección visual mediante escaneo de código QR.
 */
@Composable
fun DialogoFichaTecnicaEquipo(
    equipo: Equipo,
    usuarioActual: Usuario? = null,
    onDescartar: () -> Unit,
    onTransferirCustodiaClick: (() -> Unit)? = null,
    onReportarDanoClick: (() -> Unit)? = null,
    onDarDeBajaClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val bitmapEquipo = recordarBitmapEquipo(context, equipo.urlImagen)

    val esDanado = equipo.estadoActual == EstadoEquipo.REPORTADO_DANADO
    val esCustodioActual = usuarioActual != null && equipo.idCustodioActual == usuarioActual.id
    val esGestionador = usuarioActual?.rol == RolUsuario.JEFE_BODEGA ||
            usuarioActual?.rol == RolUsuario.ADMINISTRADOR_GENERAL ||
            usuarioActual?.rol == RolUsuario.SUPERVISOR

    AlertDialog(
        onDismissRequest = onDescartar,
        title = {
            Column {
                Text(
                    text = "Ficha Técnica de Activo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = equipo.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Badge de Estado Actual Highlight
                Surface(
                    color = colorPorEstado(equipo.estadoActual).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colorPorEstado(equipo.estadoActual)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Estado Operativo:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = colorPorEstado(equipo.estadoActual)
                        )
                        Text(
                            text = obtenerEtiquetaEstado(equipo.estadoActual),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorPorEstado(equipo.estadoActual)
                        )
                    }
                }

                // Fotografía de Evidencia HD
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmapEquipo != null) {
                        Image(
                            bitmap = bitmapEquipo.asImageBitmap(),
                            contentDescription = "Fotografía de ${equipo.nombre}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sin fotografía cargada",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Especificaciones Técnicas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ElementoDetalleFicha("Categoría", equipo.categoria)
                        ElementoDetalleFicha("Número de Serie (N/S)", equipo.numeroSerie)
                        ElementoDetalleFicha("Código QR", equipo.codigoQR)
                        ElementoDetalleFicha("Ubicación Actual", equipo.ubicacionActual)

                        if (!equipo.nombreCustodioActual.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            ElementoDetalleFicha(
                                titulo = "Custodio Responsable",
                                valor = equipo.nombreCustodioActual,
                                esResaltado = true
                            )
                        }
                    }
                }

                // Alerta si el equipo está reportado como dañado
                if (esDanado) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Este equipo tiene una incidencia o reporte de daño registrado.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (esGestionador && equipo.estadoActual != EstadoEquipo.DADO_DE_BAJA && onDarDeBajaClick != null) {
                    OutlinedButton(
                        onClick = onDarDeBajaClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Dar de Baja", style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (!esDanado && (esCustodioActual || esGestionador) && onReportarDanoClick != null) {
                    OutlinedButton(
                        onClick = onReportarDanoClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dañado", style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (!esDanado && (esCustodioActual || esGestionador) && onTransferirCustodiaClick != null) {
                    Button(onClick = onTransferirCustodiaClick) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Traspasar", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDescartar) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun ElementoDetalleFicha(
    titulo: String,
    valor: String,
    esResaltado: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$titulo:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (esResaltado) FontWeight.Bold else FontWeight.Normal,
            color = if (esResaltado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
