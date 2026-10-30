package com.example.stagetrack.ui.pantallas

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.stagetrack.core.imagen.recordarBitmapEquipo
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.TransferenciaCustodia
import com.example.stagetrack.dominio.modelo.Usuario
import com.example.stagetrack.ui.componentes.InsigniaSha256
import com.example.stagetrack.ui.viewmodel.EquipoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de Auditoría Global y Trazabilidad (Log de Custodias) para el Administrador General.
 * Muestra el historial inmutable de transferencias, custodios, ubicaciones GPS y sellos SHA-256.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAuditoriaGlobal(
    equipoViewModel: EquipoViewModel,
    listaUsuarios: List<Usuario>,
    onVolverClick: () -> Unit
) {
    val historialGlobal by equipoViewModel.historialGlobalCustodia.collectAsState()
    val equipos by equipoViewModel.listaEquipos.collectAsState()

    var textoBusqueda by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf(FiltroAuditoria.TODOS) }
    var imagenParaModal by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(urlImagen, hashSha256)

    val mapaUsuarios = remember(listaUsuarios) {
        listaUsuarios.associateBy { it.id }
    }
    val mapaEquipos = remember(equipos) {
        equipos.associateBy { it.id }
    }

    val registrosFiltrados = remember(historialGlobal, textoBusqueda, filtroSeleccionado, mapaUsuarios, mapaEquipos) {
        historialGlobal.filter { trans ->
            val equipo = mapaEquipos[trans.idEquipo]
            val emisor = mapaUsuarios[trans.idUsuarioEmisor]?.nombre ?: trans.idUsuarioEmisor
            val receptor = mapaUsuarios[trans.idUsuarioReceptor]?.nombre ?: trans.idUsuarioReceptor

            val coincideTexto = textoBusqueda.isBlank() ||
                (equipo?.nombre?.contains(textoBusqueda, ignoreCase = true) == true) ||
                (equipo?.numeroSerie?.contains(textoBusqueda, ignoreCase = true) == true) ||
                emisor.contains(textoBusqueda, ignoreCase = true) ||
                receptor.contains(textoBusqueda, ignoreCase = true) ||
                trans.hashSha256Evidencia.contains(textoBusqueda, ignoreCase = true) ||
                (trans.notas?.contains(textoBusqueda, ignoreCase = true) == true)

            val coincideFiltro = when (filtroSeleccionado) {
                FiltroAuditoria.TODOS -> true
                FiltroAuditoria.CON_GPS -> trans.latitudGps != null && trans.longitudGps != null
                FiltroAuditoria.CON_EVIDENCIA -> trans.uriFotografiaEvidencia.isNotBlank() || trans.hashSha256Evidencia.isNotBlank()
            }

            coincideTexto && coincideFiltro
        }
    }

    val totalConGps = remember(historialGlobal) { historialGlobal.count { it.latitudGps != null && it.longitudGps != null } }
    val totalConSha256 = remember(historialGlobal) { historialGlobal.count { it.hashSha256Evidencia.isNotBlank() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Auditoría Global y Trazabilidad",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Log inmutable de custodias y evidencias",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolverClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Tarjeta de Resumen / KPIs
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricaResumenAuditoria(
                        modifier = Modifier.weight(1f),
                        valor = "${historialGlobal.size}",
                        etiqueta = "Total Custodias",
                        icono = Icons.Default.History,
                        colorIcono = MaterialTheme.colorScheme.primary
                    )
                    MetricaResumenAuditoria(
                        modifier = Modifier.weight(1f),
                        valor = "$totalConSha256",
                        etiqueta = "Firmas SHA-256",
                        icono = Icons.Default.Security,
                        colorIcono = Color(0xFF4CAF50)
                    )
                    MetricaResumenAuditoria(
                        modifier = Modifier.weight(1f),
                        valor = "$totalConGps",
                        etiqueta = "Ubicación GPS",
                        icono = Icons.Default.LocationOn,
                        colorIcono = Color(0xFF2196F3)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de Búsqueda
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por equipo, usuario o SHA-256...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { textoBusqueda = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Chips de Filtro
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(FiltroAuditoria.entries) { filtro ->
                    FilterChip(
                        selected = filtroSeleccionado == filtro,
                        onClick = { filtroSeleccionado = filtro },
                        label = { Text(filtro.etiqueta) },
                        leadingIcon = if (filtroSeleccionado == filtro) {
                            { Icon(imageVector = Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de Registros de Trazabilidad
            if (registrosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (historialGlobal.isEmpty()) "No hay registros de transferencias de custodia en el sistema."
                            else "No se encontraron transferencias que coincidan con la búsqueda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(registrosFiltrados, key = { it.id }) { trans ->
                        TarjetaRegistroAuditoria(
                            transferencia = trans,
                            equipo = mapaEquipos[trans.idEquipo],
                            usuarioEmisor = mapaUsuarios[trans.idUsuarioEmisor],
                            usuarioReceptor = mapaUsuarios[trans.idUsuarioReceptor],
                            onVerFotoCompleta = { url, hash ->
                                imagenParaModal = Pair(url, hash)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Modal para ver foto de evidencia a tamaño completo
    imagenParaModal?.let { (url, hash) ->
        DialogoFotoEvidenciaCompleta(
            urlFotografia = url,
            hashSha256 = hash,
            onDescartar = { imagenParaModal = null }
        )
    }
}

private enum class FiltroAuditoria(val etiqueta: String) {
    TODOS("Todos los registros"),
    CON_GPS("Con ubicación GPS"),
    CON_EVIDENCIA("Con evidencia SHA-256")
}

@Composable
private fun MetricaResumenAuditoria(
    modifier: Modifier = Modifier,
    valor: String,
    etiqueta: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    colorIcono: Color
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(colorIcono.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = valor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TarjetaRegistroAuditoria(
    transferencia: TransferenciaCustodia,
    equipo: Equipo?,
    usuarioEmisor: Usuario?,
    usuarioReceptor: Usuario?,
    onVerFotoCompleta: (String, String) -> Unit
) {
    val context = LocalContext.current
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()) }
    val fechaTexto = remember(transferencia.fechaHoraTimestamp) {
        formatoFecha.format(Date(transferencia.fechaHoraTimestamp))
    }

    val nombreEmisor = when {
        usuarioEmisor != null -> usuarioEmisor.nombre
        transferencia.idUsuarioEmisor == "bodega" -> "Bodega Principal"
        else -> transferencia.idUsuarioEmisor
    }

    val nombreReceptor = when {
        usuarioReceptor != null -> usuarioReceptor.nombre
        transferencia.idUsuarioReceptor == "bodega" -> "Bodega Principal"
        else -> transferencia.idUsuarioReceptor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Encabezado de la Tarjeta: Fecha y Hash SHA-256
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fechaTexto,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                if (transferencia.hashSha256Evidencia.isNotBlank()) {
                    Surface(
                        color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF388E3C),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SHA-256",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Información del Equipo
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = equipo?.nombre ?: "Equipo (${transferencia.idEquipo})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (equipo != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${equipo.numeroSerie}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Diagrama de Transferencia (Emisor ➔ Receptor)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Emisor",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = nombreEmisor,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(18.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Receptor",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = nombreReceptor,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Ubicación GPS si está registrada
            if (transferencia.latitudGps != null && transferencia.longitudGps != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        val gmmIntentUri = Uri.parse("geo:${transferencia.latitudGps},${transferencia.longitudGps}?q=${transferencia.latitudGps},${transferencia.longitudGps}(Traspaso+StageTrack)")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        context.startActivity(mapIntent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GPS: Lat ${String.format(Locale.getDefault(), "%.4f", transferencia.latitudGps)}, Long ${String.format(Locale.getDefault(), "%.4f", transferencia.longitudGps)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Notas
            if (!transferencia.notas.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Notas: ${transferencia.notas}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Evidencia fotográfica con insignia SHA-256
            if (transferencia.uriFotografiaEvidencia.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                val bitmapEvidencia = recordarBitmapEquipo(context, transferencia.uriFotografiaEvidencia)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            onVerFotoCompleta(transferencia.uriFotografiaEvidencia, transferencia.hashSha256Evidencia)
                        }
                ) {
                    if (bitmapEvidencia != null) {
                        Image(
                            bitmap = bitmapEvidencia.asImageBitmap(),
                            contentDescription = "Evidencia Fotográfica",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ver evidencia fotográfica adjunta",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (transferencia.hashSha256Evidencia.isNotBlank()) {
                        InsigniaSha256(hashSha256 = transferencia.hashSha256Evidencia)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogoFotoEvidenciaCompleta(
    urlFotografia: String,
    hashSha256: String,
    onDescartar: () -> Unit
) {
    val context = LocalContext.current
    val bitmapEvidencia = recordarBitmapEquipo(context, urlFotografia)

    Dialog(onDismissRequest = onDescartar) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Evidencia Fotográfica Certificada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDescartar) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmapEvidencia != null) {
                        Image(
                            bitmap = bitmapEvidencia.asImageBitmap(),
                            contentDescription = "Fotografía de evidencia completa",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Cargando o vista previa no disponible")
                    }
                }

                if (hashSha256.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Firma SHA-256:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = hashSha256,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = onDescartar,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}
