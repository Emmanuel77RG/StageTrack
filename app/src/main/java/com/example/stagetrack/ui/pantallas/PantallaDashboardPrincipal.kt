package com.example.stagetrack.ui.pantallas

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import java.io.File
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import com.example.stagetrack.ui.componentes.DialogoAceptarDevolucionQr
import com.example.stagetrack.ui.viewmodel.EquipoViewModel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.stagetrack.core.imagen.UtilidadImagen
import com.example.stagetrack.core.imagen.recordarBitmapEquipo
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.EstadoEquipo
import com.example.stagetrack.dominio.modelo.EstadoEvento
import com.example.stagetrack.dominio.modelo.Evento
import com.example.stagetrack.dominio.modelo.ModoEscaneoQr
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.dominio.modelo.Usuario
import com.example.stagetrack.ui.componentes.AvatarUsuario
import com.example.stagetrack.ui.componentes.DialogoGestionCategoriasMarcas

/**
 * Dashboard principal modular adaptado específicamente para el rol operativo de cada usuario.
 */
@Composable
fun PantallaDashboardPrincipal(
    usuarioActual: Usuario,
    equipos: List<Equipo>,
    eventos: List<Evento>,
    listaUsuarios: List<Usuario>,
    equipoViewModel: EquipoViewModel? = null,
    onNavegarInventario: (EstadoEquipo?) -> Unit = {},
    onNavegarEventos: () -> Unit,
    onNavegarEscaneoQr: (ModoEscaneoQr) -> Unit,
    onNavegarGestionUsuarios: () -> Unit,
    onNavegarAuditoriaGlobal: () -> Unit = {},
    onNavegarExportacionReportes: () -> Unit = {},
    onNavegarPerfil: () -> Unit,
    onNavegarTransferencia: (Equipo) -> Unit,
    onNavegarReporteDano: (Equipo) -> Unit,
    onNavegarFichaTecnica: (Equipo) -> Unit = {},
    onCerrarSesion: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Encabezado de Usuario
        EncabezadoDashboard(
            usuario = usuarioActual,
            onNavegarPerfil = onNavegarPerfil,
            onCerrarSesion = onCerrarSesion
        )

        // Contenido según el rol operativo del usuario
        when (usuarioActual.rol) {
            RolUsuario.JEFE_BODEGA -> {
                DashboardJefeBodega(
                    equipos = equipos,
                    eventos = eventos,
                    equipoViewModel = equipoViewModel,
                    onNavegarInventario = onNavegarInventario,
                    onNavegarEventos = onNavegarEventos,
                    onNavegarEscaneoQr = onNavegarEscaneoQr
                )
            }
            RolUsuario.TECNICO -> {
                DashboardTecnico(
                    usuario = usuarioActual,
                    equipos = equipos,
                    eventos = eventos,
                    onNavegarInventario = onNavegarInventario,
                    onNavegarEventos = onNavegarEventos,
                    onNavegarEscaneoQr = onNavegarEscaneoQr,
                    onNavegarTransferencia = onNavegarTransferencia,
                    onNavegarReporteDano = onNavegarReporteDano,
                    onNavegarFichaTecnica = onNavegarFichaTecnica
                )
            }
            RolUsuario.SUPERVISOR -> {
                DashboardSupervisor(
                    equipos = equipos,
                    eventos = eventos,
                    onNavegarInventario = onNavegarInventario,
                    onNavegarEventos = onNavegarEventos,
                    onNavegarEscaneoQr = onNavegarEscaneoQr
                )
            }
            RolUsuario.ADMINISTRADOR_GENERAL -> {
                DashboardAdministrador(
                    equipos = equipos,
                    eventos = eventos,
                    listaUsuarios = listaUsuarios,
                    onNavegarGestionUsuarios = onNavegarGestionUsuarios,
                    onNavegarAuditoriaGlobal = onNavegarAuditoriaGlobal,
                    onNavegarExportacionReportes = onNavegarExportacionReportes,
                    onNavegarInventario = onNavegarInventario,
                    onNavegarEventos = onNavegarEventos,
                    onNavegarEscaneoQr = onNavegarEscaneoQr
                )
            }
        }
    }
}

/**
 * Encabezado con información del usuario y rol activo.
 */
@Composable
private fun EncabezadoDashboard(
    usuario: Usuario,
    onNavegarPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavegarPerfil() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarUsuario(
                    urlFotografia = usuario.urlFotografia,
                    nombreUsuario = usuario.nombre,
                    tamano = 52.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "StageTrack",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = usuario.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = etiquetaRolTexto(usuario.rol),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onCerrarSesion) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Cerrar sesión",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Dashboard para Jefe de Bodega: Enfoque en inventario, despacho y escaneo QR.
 */
@Composable
private fun DashboardJefeBodega(
    equipos: List<Equipo>,
    eventos: List<Evento>,
    equipoViewModel: EquipoViewModel? = null,
    onNavegarInventario: (EstadoEquipo?) -> Unit,
    onNavegarEventos: () -> Unit,
    onNavegarEscaneoQr: (ModoEscaneoQr) -> Unit
) {
    val enBodegaCount = equipos.count { it.estadoActual == EstadoEquipo.EN_BODEGA }
    val enCustodiaCount = equipos.count { it.estadoActual == EstadoEquipo.EN_CUSTODIA || it.estadoActual == EstadoEquipo.EN_TRANSITO }
    val danadosCount = equipos.count { it.estadoActual == EstadoEquipo.REPORTADO_DANADO }

    val equiposEnDevolucion = equipos.filter { it.estadoActual == EstadoEquipo.EN_PROCESO_DEVOLUCION }
    var equipoParaAceptarDevolucion by remember { mutableStateOf<Equipo?>(null) }

    // Row de KPIs (Interactivos)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TarjetaMetricaKpi(
            titulo = "En Bodega",
            valor = enBodegaCount.toString(),
            icono = Icons.Default.Inventory2,
            colorContenedor = MaterialTheme.colorScheme.primaryContainer,
            colorTexto = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f),
            onClick = { onNavegarInventario(EstadoEquipo.EN_BODEGA) }
        )
        TarjetaMetricaKpi(
            titulo = "Despachados",
            valor = enCustodiaCount.toString(),
            icono = Icons.AutoMirrored.Filled.Send,
            colorContenedor = MaterialTheme.colorScheme.secondaryContainer,
            colorTexto = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f),
            onClick = { onNavegarInventario(EstadoEquipo.EN_CUSTODIA) }
        )
        TarjetaMetricaKpi(
            titulo = "Averiados",
            valor = danadosCount.toString(),
            icono = Icons.Default.Warning,
            colorContenedor = MaterialTheme.colorScheme.errorContainer,
            colorTexto = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f),
            onClick = { onNavegarInventario(EstadoEquipo.REPORTADO_DANADO) }
        )
    }

    if (equiposEnDevolucion.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MoveToInbox,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Equipos en Devolución (${equiposEnDevolucion.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                equiposEnDevolucion.forEach { equipoDev ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = equipoDev.nombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Devuelto por: ${equipoDev.nombreCustodioActual ?: "Técnico"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "N/S: ${equipoDev.numeroSerie}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            if (equipoViewModel != null) {
                                Button(
                                    onClick = { equipoParaAceptarDevolucion = equipoDev },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Aceptar (QR)", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (equipoParaAceptarDevolucion != null && equipoViewModel != null) {
        DialogoAceptarDevolucionQr(
            equipo = equipoParaAceptarDevolucion!!,
            equipoViewModel = equipoViewModel,
            onDescartar = { equipoParaAceptarDevolucion = null },
            onDevolucionExitosa = { equipoParaAceptarDevolucion = null }
        )
    }

    // Acción Reina: Escaneo QR de Despacho
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Escanear QR de Despacho",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Entrada, salida o verificación rápida de equipos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onNavegarEscaneoQr(ModoEscaneoQr.DESPACHO_TRASPASO) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Escanear", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Grid de Módulos Operativos
    Text(
        text = "Módulos de Gestión",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TarjetaAccionModulo(
            titulo = "Gestión de Bodega",
            descripcion = "Alta de catálogo, seriales e impresión de QR",
            icono = Icons.AutoMirrored.Filled.List,
            textoBoton = "Inventario (${equipos.size})",
            onClick = { onNavegarInventario(null) },
            modifier = Modifier.weight(1f)
        )

        TarjetaAccionModulo(
            titulo = "Eventos Activos",
            descripcion = "Preparación y empaque para producciones",
            icono = Icons.Default.Event,
            textoBoton = "Ver Eventos (${eventos.size})",
            onClick = onNavegarEventos,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Dashboard para Técnico: Enfoque en sus equipos en custodia y eventos asignados.
 */
@Composable
private fun DashboardTecnico(
    usuario: Usuario,
    equipos: List<Equipo>,
    eventos: List<Evento>,
    onNavegarInventario: (EstadoEquipo?) -> Unit,
    onNavegarEventos: () -> Unit,
    onNavegarEscaneoQr: (ModoEscaneoQr) -> Unit,
    onNavegarTransferencia: (Equipo) -> Unit,
    onNavegarReporteDano: (Equipo) -> Unit,
    onNavegarFichaTecnica: (Equipo) -> Unit
) {
    val misEquipos = equipos.filter { it.idCustodioActual == usuario.id }
    val misEventos = eventos.filter { it.idsTecnicosAsignados.contains(usuario.id) }

    // Row de KPIs del Técnico (Interactivos)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TarjetaMetricaKpi(
            titulo = "A mi Cargo",
            valor = misEquipos.size.toString(),
            icono = Icons.Default.Build,
            colorContenedor = MaterialTheme.colorScheme.secondaryContainer,
            colorTexto = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f),
            onClick = { onNavegarInventario(null) }
        )
        TarjetaMetricaKpi(
            titulo = "Mis Eventos",
            valor = misEventos.size.toString(),
            icono = Icons.Default.Event,
            colorContenedor = MaterialTheme.colorScheme.tertiaryContainer,
            colorTexto = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f),
            onClick = onNavegarEventos
        )
    }

    // Sección: Mis Equipos en Custodia
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Mis Equipos en Custodia",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (misEquipos.isNotEmpty()) {
            Text(
                text = "${misEquipos.size} asignados",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (misEquipos.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No tienes equipos a tu cargo en este momento",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(misEquipos) { equipo ->
                TarjetaEquipoAsignadoTecnico(
                    equipo = equipo,
                    onVerFichaTecnica = { onNavegarFichaTecnica(equipo) },
                    onTransferir = { onNavegarTransferencia(equipo) },
                    onReportarDano = { onNavegarReporteDano(equipo) }
                )
            }
        }
    }

    // Acciones del Técnico
    Text(
        text = "Acciones de Campo",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TarjetaAccionModulo(
            titulo = "Escanear / Traspaso",
            descripcion = "Recibir o entregar custodia por QR",
            icono = Icons.Default.QrCodeScanner,
            textoBoton = "Escanear QR",
            onClick = { onNavegarEscaneoQr(ModoEscaneoQr.RECEPCION_CUSTODIA) },
            modifier = Modifier.weight(1f)
        )

        TarjetaAccionModulo(
            titulo = "Catálogo General",
            descripcion = "Consultar inventario global de producción",
            icono = Icons.AutoMirrored.Filled.List,
            textoBoton = "Ver Inventario",
            onClick = { onNavegarInventario(null) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Tarjeta individual para equipos en custodia del técnico.
 */
@Composable
private fun TarjetaEquipoAsignadoTecnico(
    equipo: Equipo,
    onVerFichaTecnica: (() -> Unit)? = null,
    onTransferir: () -> Unit,
    onReportarDano: () -> Unit
) {
    val context = LocalContext.current
    val bitmapEquipo = recordarBitmapEquipo(context, equipo.urlImagen)

    Card(
        modifier = Modifier
            .width(220.dp)
            .then(
                if (onVerFichaTecnica != null) {
                    Modifier.clickable { onVerFichaTecnica() }
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
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
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = equipo.nombre,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "S/N: ${equipo.numeroSerie}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onTransferir,
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Text("Traspasar", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onReportarDano,
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp, horizontal = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Dañado", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * Dashboard para Supervisor: Enfoque en proyectos, trazabilidad e incidencias.
 */
@Composable
private fun DashboardSupervisor(
    equipos: List<Equipo>,
    eventos: List<Evento>,
    onNavegarInventario: (EstadoEquipo?) -> Unit,
    onNavegarEventos: () -> Unit,
    onNavegarEscaneoQr: (ModoEscaneoQr) -> Unit
) {
    val eventosActivos = eventos.count { it.estado == EstadoEvento.EN_CURSO || it.estado == EstadoEvento.PLANIFICADO }
    val equiposEnCustodia = equipos.count { it.estadoActual == EstadoEquipo.EN_CUSTODIA }
    val reportesDano = equipos.count { it.estadoActual == EstadoEquipo.REPORTADO_DANADO }

    // Row de KPIs (Interactivos)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TarjetaMetricaKpi(
            titulo = "Proyectos",
            valor = eventosActivos.toString(),
            icono = Icons.Default.Event,
            colorContenedor = MaterialTheme.colorScheme.tertiaryContainer,
            colorTexto = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f),
            onClick = onNavegarEventos
        )
        TarjetaMetricaKpi(
            titulo = "En Escenario",
            valor = equiposEnCustodia.toString(),
            icono = Icons.Default.Build,
            colorContenedor = MaterialTheme.colorScheme.primaryContainer,
            colorTexto = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f),
            onClick = { onNavegarInventario(EstadoEquipo.EN_CUSTODIA) }
        )
        TarjetaMetricaKpi(
            titulo = "Averías",
            valor = reportesDano.toString(),
            icono = Icons.Default.Warning,
            colorContenedor = MaterialTheme.colorScheme.errorContainer,
            colorTexto = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f),
            onClick = { onNavegarInventario(EstadoEquipo.REPORTADO_DANADO) }
        )
    }

    if (reportesDano > 0) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
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
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "$reportesDano equipo(s) con reporte de daño requieren atención.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // Módulos
    Text(
        text = "Supervisión Operativa",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TarjetaAccionModulo(
            titulo = "Gestión de Eventos",
            descripcion = "Crear producciones y convocar técnicos",
            icono = Icons.Default.Event,
            textoBoton = "Gestionar Eventos",
            onClick = onNavegarEventos,
            modifier = Modifier.weight(1f)
        )

        TarjetaAccionModulo(
            titulo = "Trazabilidad Inventario",
            descripcion = "Supervisar custodias y ubicación de activos",
            icono = Icons.AutoMirrored.Filled.List,
            textoBoton = "Ver Inventario",
            onClick = { onNavegarInventario(null) },
            modifier = Modifier.weight(1f)
        )
    }

    OutlinedButton(
        onClick = { onNavegarEscaneoQr(ModoEscaneoQr.CONSULTAR_FICHA) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Consultar Ficha Técnica por QR")
    }
}

/**
 * Dashboard para Administrador General: Control de usuarios, catálogo e insumos.
 */
@Composable
private fun DashboardAdministrador(
    equipos: List<Equipo>,
    eventos: List<Evento>,
    listaUsuarios: List<Usuario>,
    onNavegarGestionUsuarios: () -> Unit,
    onNavegarAuditoriaGlobal: () -> Unit,
    onNavegarExportacionReportes: () -> Unit,
    onNavegarInventario: (EstadoEquipo?) -> Unit,
    onNavegarEventos: () -> Unit,
    onNavegarEscaneoQr: (ModoEscaneoQr) -> Unit
) {
    var mostrarDialogoCategoriasMarcas by remember { mutableStateOf(false) }

    // KPIs Globales (Interactivos)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TarjetaMetricaKpi(
            titulo = "Usuarios",
            valor = listaUsuarios.size.toString(),
            icono = Icons.Default.People,
            colorContenedor = MaterialTheme.colorScheme.primaryContainer,
            colorTexto = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f),
            onClick = onNavegarGestionUsuarios
        )
        TarjetaMetricaKpi(
            titulo = "Total Equipos",
            valor = equipos.size.toString(),
            icono = Icons.Default.Inventory2,
            colorContenedor = MaterialTheme.colorScheme.secondaryContainer,
            colorTexto = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f),
            onClick = { onNavegarInventario(null) }
        )
        TarjetaMetricaKpi(
            titulo = "Eventos",
            valor = eventos.size.toString(),
            icono = Icons.Default.Event,
            colorContenedor = MaterialTheme.colorScheme.tertiaryContainer,
            colorTexto = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f),
            onClick = onNavegarEventos
        )
    }

    // Card Destacada de Auditoría Global y Trazabilidad
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Auditoría Global y Trazabilidad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Historial inmutable de transferencias de custodia, geolocalización GPS y evidencias con firma SHA-256.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onNavegarAuditoriaGlobal,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Text("Ver Panel de Auditoría", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Card Destacada de Exportación de Reportes Ejecutivos (Paso 5)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Exportación de Reportes Ejecutivos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Genera y exporta informes consolidados (CSV/Texto) de inventario, valoración de activos y reporte global de daños.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onNavegarExportacionReportes,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    contentColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text("Exportar Reportes Ejecutivos", fontWeight = FontWeight.Bold)
            }
        }
    }

    OutlinedButton(
        onClick = { mostrarDialogoCategoriasMarcas = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(imageVector = Icons.Default.Category, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Gestionar Categorías y Marcas Oficiales", fontWeight = FontWeight.Bold)
    }

    if (mostrarDialogoCategoriasMarcas) {
        DialogoGestionCategoriasMarcas(
            onDismiss = { mostrarDialogoCategoriasMarcas = false }
        )
    }
}

/**
 * Componente genérico para métricas KPI en tarjetas pequeñas.
 */
@Composable
private fun TarjetaMetricaKpi(
    titulo: String,
    valor: String,
    icono: ImageVector,
    colorContenedor: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        colors = CardDefaults.cardColors(containerColor = colorContenedor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = valor,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto
                )
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colorTexto.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = colorTexto.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Componente para tarjetas de módulo en cuadrículas.
 */
@Composable
private fun TarjetaAccionModulo(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    textoBoton: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(textoBoton, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

private fun etiquetaRolTexto(rol: RolUsuario?): String {
    return when (rol) {
        RolUsuario.ADMINISTRADOR_GENERAL -> "Administrador General"
        RolUsuario.JEFE_BODEGA -> "Jefe de Bodega"
        RolUsuario.TECNICO -> "Técnico de Producción"
        RolUsuario.SUPERVISOR -> "Supervisor"
        null -> "Invitado"
    }
}
