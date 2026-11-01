package com.example.stagetrack.ui.pantallas

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import com.example.stagetrack.core.imagen.UtilidadImagen
import com.example.stagetrack.core.imagen.recordarBitmapEquipo
import com.example.stagetrack.ui.componentes.DialogoVerReporteDano
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.collectAsState
import com.example.stagetrack.core.config.CatalogoCategoriasMarcas
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.core.qr.UtilidadQR
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.EstadoEquipo
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.dominio.modelo.Usuario
import com.example.stagetrack.ui.componentes.CameraPreview
import com.example.stagetrack.ui.viewmodel.EquipoViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaInventarioEquipos(
    equipoViewModel: EquipoViewModel,
    usuarioActual: Usuario? = null,
    filtroEstadoInicial: EstadoEquipo? = null,
    onVolverClick: () -> Unit,
    onTransferirCustodiaClick: (Equipo) -> Unit,
    onReportarDanoClick: (Equipo) -> Unit
) {
    val equipos by equipoViewModel.listaEquipos.collectAsState()

    var busquedaTexto by remember { mutableStateOf("") }
    var estadoFiltroSeleccionado by remember(filtroEstadoInicial) { mutableStateOf<EstadoEquipo?>(filtroEstadoInicial) }
    var categoriaFiltroSeleccionada by remember { mutableStateOf<String?>(null) }
    var mostrarModalFiltros by remember { mutableStateOf(false) }
    var vistaTecnicoSeleccionada by remember { mutableStateOf(0) } // 0 = Mis Equipos, 1 = Catálogo General
    var mostrarDialogoNuevoEquipo by remember { mutableStateOf(false) }
    var equipoParaEditar by remember { mutableStateOf<Equipo?>(null) }
    var equipoParaVerQr by remember { mutableStateOf<Equipo?>(null) }
    var equipoParaVerReporteDano by remember { mutableStateOf<Equipo?>(null) }
    var equipoParaEnviarMantenimiento by remember { mutableStateOf<Equipo?>(null) }
    var equipoParaFinalizarMantenimiento by remember { mutableStateOf<Equipo?>(null) }
    var mostrarDialogoRestablecerTodo by remember { mutableStateOf(false) }

    val reportesDanoParaEquipo by (
        equipoParaVerReporteDano?.let { eq ->
            equipoViewModel.obtenerReportesDanoPorEquipo(eq.id).collectAsState(initial = emptyList())
        } ?: remember { mutableStateOf(emptyList()) }
    )

    // Control de permisos y visibilidad según el rol del usuario
    val esJefeBodega = usuarioActual?.rol == RolUsuario.JEFE_BODEGA
    val esAdmin = usuarioActual?.rol == RolUsuario.ADMINISTRADOR_GENERAL
    val esSupervisor = usuarioActual?.rol == RolUsuario.SUPERVISOR
    val esTecnico = usuarioActual?.rol == RolUsuario.TECNICO

    val puedeEditarEquipo = esJefeBodega || esAdmin
    val puedeRegistrarEquipo = esJefeBodega || esAdmin
    val puedeGenerarQr = esJefeBodega || esAdmin

    // Para el Técnico se le permite alternar entre "Mis Equipos" y la consulta del "Catálogo General"
    val equiposVisiblesPorRol = if (esTecnico && usuarioActual != null) {
        if (vistaTecnicoSeleccionada == 0) {
            equipos.filter { it.idCustodioActual == usuarioActual.id || it.estadoActual == EstadoEquipo.EN_TRANSITO }
        } else {
            equipos
        }
    } else {
        equipos
    }

    val equiposFiltrados = equiposVisiblesPorRol.filter { equipo ->
        val coincideTexto = equipo.nombre.contains(busquedaTexto, ignoreCase = true) ||
                equipo.numeroSerie.contains(busquedaTexto, ignoreCase = true) ||
                equipo.codigoQR.contains(busquedaTexto, ignoreCase = true) ||
                equipo.categoria.contains(busquedaTexto, ignoreCase = true)

        val esArchivado = equipo.estadoActual == EstadoEquipo.DADO_DE_BAJA
        val coincideEstado = if (estadoFiltroSeleccionado != null) {
            equipo.estadoActual == estadoFiltroSeleccionado
        } else {
            !esArchivado
        }

        val coincideCategoria = if (!categoriaFiltroSeleccionada.isNullOrBlank()) {
            equipo.categoria.equals(categoriaFiltroSeleccionada, ignoreCase = true)
        } else {
            true
        }

        coincideTexto && coincideEstado && coincideCategoria
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (esTecnico) {
                                if (vistaTecnicoSeleccionada == 0) "Mis Equipos en Custodia" else "Catálogo General de Bodega"
                            } else "Inventario Global de Bodega",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (esTecnico) {
                                if (vistaTecnicoSeleccionada == 0) "Equipos Asignados a Mi Nombre" else "Consulta de Stock Disponible"
                            } else "Catálogo Maestro de Activos",
                            style = MaterialTheme.typography.labelSmall,
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
                actions = {
                    if (esJefeBodega || esAdmin) {
                        IconButton(onClick = { mostrarDialogoRestablecerTodo = true }) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Restablecer Todo a Bodega",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (puedeRegistrarEquipo) {
                FloatingActionButton(onClick = { mostrarDialogoNuevoEquipo = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Registrar Equipo"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Selector de pestaña de vista rápida para Técnicos
            if (esTecnico) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val misEquiposCount = remember(equipos, usuarioActual) {
                        equipos.count { it.idCustodioActual == usuarioActual?.id }
                    }
                    FilterChip(
                        selected = vistaTecnicoSeleccionada == 0,
                        onClick = { vistaTecnicoSeleccionada = 0 },
                        label = { Text("Mis Equipos ($misEquiposCount)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = vistaTecnicoSeleccionada == 1,
                        onClick = { vistaTecnicoSeleccionada = 1 },
                        label = { Text("Catálogo General (${equipos.size})") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = busquedaTexto,
                    onValueChange = { busquedaTexto = it },
                    label = { Text("Buscar por nombre, serie o QR") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                val hayFiltroActivo = estadoFiltroSeleccionado != null || !categoriaFiltroSeleccionada.isNullOrBlank()

                Button(
                    onClick = { mostrarModalFiltros = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hayFiltroActivo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (hayFiltroActivo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtros",
                        modifier = Modifier.size(20.dp)
                    )
                    if (hayFiltroActivo) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("•", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (estadoFiltroSeleccionado != null || !categoriaFiltroSeleccionada.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    estadoFiltroSeleccionado?.let { est ->
                        AssistChip(
                            onClick = { estadoFiltroSeleccionado = null },
                            label = { Text("Estado: ${obtenerEtiquetaEstado(est)} ✕") }
                        )
                    }
                    categoriaFiltroSeleccionada?.let { cat ->
                        AssistChip(
                            onClick = { categoriaFiltroSeleccionada = null },
                            label = { Text("Categoría: $cat ✕") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (equiposFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron equipos registrados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(equiposFiltrados, key = { it.id }) { equipo ->
                        val esMio = (usuarioActual != null && equipo.idCustodioActual == usuarioActual.id)
                        val esReceptorTransito = (usuarioActual != null && equipo.estadoActual == EstadoEquipo.EN_TRANSITO)

                        // Permisos dinámicos de acción operativa según rol y posesión
                        val permiteAccionesCustodia = when {
                            esJefeBodega || esAdmin || esSupervisor -> true
                            esTecnico -> esMio || esReceptorTransito
                            else -> false
                        }

                        val context = LocalContext.current
                        TarjetaEquipoItem(
                            equipo = equipo,
                            esFiltroDano = (estadoFiltroSeleccionado == EstadoEquipo.REPORTADO_DANADO),
                            puedeGenerarQr = puedeGenerarQr,
                            onVerQrClick = if (puedeGenerarQr) { { equipoParaVerQr = equipo } } else null,
                            onTransferirCustodiaClick = if (permiteAccionesCustodia) { { onTransferirCustodiaClick(equipo) } } else null,
                            onReportarDanoClick = if (permiteAccionesCustodia) { { onReportarDanoClick(equipo) } } else null,
                            onVerReporteDanoClick = { equipoParaVerReporteDano = equipo },
                            onEnviarAMantenimientoClick = if (esJefeBodega || esAdmin) {
                                { equipoParaEnviarMantenimiento = equipo }
                            } else null,
                            onFinalizarMantenimientoClick = if (esJefeBodega || esAdmin) {
                                { equipoParaFinalizarMantenimiento = equipo }
                            } else null,
                            onEditarClick = if (puedeEditarEquipo) { { equipoParaEditar = equipo } } else null,
                            onDarDeBajaClick = if (puedeEditarEquipo && equipo.estadoActual != EstadoEquipo.DADO_DE_BAJA) {
                                { equipoViewModel.darDeBajaEquipo(equipo.id, "Baja de activo") { _ -> } }
                            } else null,
                            onRestaurarClick = if (puedeEditarEquipo && equipo.estadoActual == EstadoEquipo.DADO_DE_BAJA) {
                                { equipoViewModel.actualizarEstadoEquipo(equipo.id, EstadoEquipo.EN_BODEGA) { _ -> } }
                            } else null
                        )
                    }
                }
            }
        }

        equipoParaVerReporteDano?.let { equipoDañado ->
            DialogoVerReporteDano(
                equipo = equipoDañado,
                reportes = reportesDanoParaEquipo,
                onDismiss = { equipoParaVerReporteDano = null }
            )
        }

        equipoParaEnviarMantenimiento?.let { equipo ->
            val context = LocalContext.current
            AlertDialog(
                onDismissRequest = { equipoParaEnviarMantenimiento = null },
                title = {
                    Text("Confirmar Envío a Mantenimiento", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "¿Confirmas que deseas transferir el equipo \"${equipo.nombre}\" (N/S: ${equipo.numeroSerie}) al taller o área de Mantenimiento?"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            equipoViewModel.enviarAEnMantenimiento(equipo.id) { res ->
                                if (res is Resultado.Exito) {
                                    Toast.makeText(context, "Equipo enviado a Mantenimiento / Taller", Toast.LENGTH_SHORT).show()
                                }
                            }
                            equipoParaEnviarMantenimiento = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Sí, Enviar a Taller", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { equipoParaEnviarMantenimiento = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        equipoParaFinalizarMantenimiento?.let { equipo ->
            val context = LocalContext.current
            AlertDialog(
                onDismissRequest = { equipoParaFinalizarMantenimiento = null },
                title = {
                    Text("Confirmar Retorno a Bodega", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "¿Confirmas que la reparación del equipo \"${equipo.nombre}\" ha finalizado y el activo vuelve a estar disponible en Bodega Principal?"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            equipoViewModel.finalizarMantenimientoYDevolverABodega(equipo.id) { res ->
                                if (res is Resultado.Exito) {
                                    Toast.makeText(context, "Reparación finalizada. Equipo devuelto a Bodega", Toast.LENGTH_SHORT).show()
                                }
                            }
                            equipoParaFinalizarMantenimiento = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Sí, Finalizar y Devolver", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { equipoParaFinalizarMantenimiento = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (mostrarDialogoRestablecerTodo) {
            val context = LocalContext.current
            AlertDialog(
                onDismissRequest = { mostrarDialogoRestablecerTodo = false },
                title = { Text("Restablecer Todo a Bodega Principal", fontWeight = FontWeight.Bold) },
                text = {
                    Text("¿Confirmas que deseas devolver TODOS los equipos registrados al stock disponible en 'Bodega Principal' (limpiando custodios y ubicaciones de eventos/tránsito)?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            equipoViewModel.restablecerTodosLosEquiposABodega { res ->
                                if (res is Resultado.Exito) {
                                    Toast.makeText(context, "Todos los equipos han sido devueltos a Bodega Principal", Toast.LENGTH_LONG).show()
                                }
                            }
                            mostrarDialogoRestablecerTodo = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Sí, Restablecer Todo", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoRestablecerTodo = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (mostrarModalFiltros) {
            ModalBottomSheet(
                onDismissRequest = { mostrarModalFiltros = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filtros de Inventario",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (estadoFiltroSeleccionado != null || !categoriaFiltroSeleccionada.isNullOrBlank()) {
                            TextButton(onClick = {
                                estadoFiltroSeleccionado = null
                                categoriaFiltroSeleccionada = null
                            }) {
                                Text("Limpiar Filtros")
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Text(
                        text = "Estado Operativo del Activo:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    val listaEstados = remember { EstadoEquipo.entries }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = estadoFiltroSeleccionado == null,
                            onClick = { estadoFiltroSeleccionado = null },
                            label = { Text("Todos los Estados") }
                        )
                        listaEstados.forEach { estado ->
                            FilterChip(
                                selected = estadoFiltroSeleccionado == estado,
                                onClick = {
                                    estadoFiltroSeleccionado = if (estadoFiltroSeleccionado == estado) null else estado
                                },
                                label = { Text(obtenerEtiquetaEstado(estado)) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(colorPorEstado(estado), CircleShape)
                                    )
                                }
                            )
                        }
                    }

                    val categoriasDisponibles = remember(equipos) {
                        equipos.map { it.categoria }.distinct().sorted()
                    }

                    if (categoriasDisponibles.isNotEmpty()) {
                        Text(
                            text = "Categoría de Equipo:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = categoriaFiltroSeleccionada == null,
                                onClick = { categoriaFiltroSeleccionada = null },
                                label = { Text("Todas las Categorías") }
                            )
                            categoriasDisponibles.forEach { cat ->
                                FilterChip(
                                    selected = categoriaFiltroSeleccionada == cat,
                                    onClick = {
                                        categoriaFiltroSeleccionada = if (categoriaFiltroSeleccionada == cat) null else cat
                                    },
                                    label = { Text(cat) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { mostrarModalFiltros = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Aplicar y Ver Resultados (${equiposFiltrados.size})", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        if (mostrarDialogoNuevoEquipo) {
            val context = LocalContext.current
            DialogoRegistroEquipo(
                onDescartar = { mostrarDialogoNuevoEquipo = false },
                onGuardar = { nombre, categoria, serie, qr, urlImagen, onResultado ->
                    equipoViewModel.registrarNuevoEquipo(
                        context = context,
                        nombre = nombre,
                        categoria = categoria,
                        numeroSerie = serie,
                        codigoQr = qr,
                        urlImagen = urlImagen,
                        onResultado = onResultado
                    )
                }
            )
        }

        equipoParaEditar?.let { equipo ->
            val context = LocalContext.current
            DialogoEditarEquipo(
                equipo = equipo,
                onDescartar = { equipoParaEditar = null },
                onGuardarCambios = { equipoActualizado, onResultado ->
                    equipoViewModel.actualizarEquipoExistente(
                        context = context,
                        equipoActualizado = equipoActualizado,
                        onResultado = onResultado
                    )
                }
            )
        }

        equipoParaVerQr?.let { equipo ->
            DialogoVerQrEquipo(
                equipo = equipo,
                onDescartar = { equipoParaVerQr = null }
            )
        }
    }
}

@Composable
fun TarjetaEquipoItem(
    equipo: Equipo,
    esFiltroDano: Boolean = false,
    puedeGenerarQr: Boolean = false,
    onVerQrClick: (() -> Unit)? = null,
    onTransferirCustodiaClick: (() -> Unit)? = null,
    onReportarDanoClick: (() -> Unit)? = null,
    onVerReporteDanoClick: (() -> Unit)? = null,
    onEnviarAMantenimientoClick: (() -> Unit)? = null,
    onFinalizarMantenimientoClick: (() -> Unit)? = null,
    onEditarClick: (() -> Unit)? = null,
    onDarDeBajaClick: (() -> Unit)? = null,
    onRestaurarClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Encabezado superior: Nombre y Badge de Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = equipo.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = colorPorEstado(equipo.estadoActual).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colorPorEstado(equipo.estadoActual))
                ) {
                    Text(
                        text = obtenerEtiquetaEstado(equipo.estadoActual),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorPorEstado(equipo.estadoActual),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fila central: Fotografía de evidencia + Detalles organizados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                val context = LocalContext.current
                val bitmapEquipo = recordarBitmapEquipo(context, equipo.urlImagen)

                Box(
                    modifier = Modifier
                        .size(80.dp)
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
                            contentDescription = "Sin imagen",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row {
                        Text(
                            text = "Categoría: ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = equipo.categoria,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row {
                        Text(
                            text = "N/S: ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = equipo.numeroSerie,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row {
                        Text(
                            text = "QR: ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = equipo.codigoQR,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row {
                        Text(
                            text = "Ubicación: ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = equipo.ubicacionActual,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!equipo.nombreCustodioActual.isNullOrBlank()) {
                        Row {
                            Text(
                                text = "Custodio: ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = equipo.nombreCustodioActual,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.tertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Línea divisora
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botones de acción inferiores con comportamiento dinámico inteligente
            val esEquipoDanado = equipo.estadoActual == EstadoEquipo.REPORTADO_DANADO
            val esEnMantenimiento = equipo.estadoActual == EstadoEquipo.EN_MANTENIMIENTO

            if (esEquipoDanado || esFiltroDano) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onVerReporteDanoClick?.invoke() },
                        enabled = onVerReporteDanoClick != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver Daño", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }

                    if (onEnviarAMantenimientoClick != null) {
                        Button(
                            onClick = { onEnviarAMantenimientoClick.invoke() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9800),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("A Mantenimiento", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            } else if (esEnMantenimiento) {
                if (onFinalizarMantenimientoClick != null) {
                    Button(
                        onClick = { onFinalizarMantenimientoClick.invoke() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reparación Finalizada ➔ Retornar a Bodega", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (puedeGenerarQr && onVerQrClick != null) {
                        OutlinedButton(
                            onClick = onVerQrClick,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Ver QR",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ver QR", style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onEditarClick != null) {
                            IconButton(
                                onClick = onEditarClick,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar Equipo",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        if (onReportarDanoClick != null) {
                            OutlinedButton(
                                onClick = onReportarDanoClick,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Daño", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        if (onTransferirCustodiaClick != null) {
                            val textoCustodia = when (equipo.estadoActual) {
                                EstadoEquipo.EN_TRANSITO -> "Aceptar"
                                EstadoEquipo.EN_BODEGA -> "Despachar"
                                EstadoEquipo.EN_CUSTODIA -> "Traspasar"
                                else -> "Custodia"
                            }
                            Button(
                                onClick = onTransferirCustodiaClick,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(textoCustodia, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            val esArchivado = equipo.estadoActual == EstadoEquipo.DADO_DE_BAJA

            if (onDarDeBajaClick != null || onRestaurarClick != null) {
                Spacer(modifier = Modifier.height(10.dp))
                if (esArchivado && onRestaurarClick != null) {
                    Button(
                        onClick = onRestaurarClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restaurar / Activar Activo", fontWeight = FontWeight.Bold)
                    }
                } else if (!esArchivado && onDarDeBajaClick != null) {
                    OutlinedButton(
                        onClick = onDarDeBajaClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dar de Baja (Archivar)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoVerQrEquipo(
    equipo: Equipo,
    onDescartar: () -> Unit
) {
    val context = LocalContext.current

    // Generar la etiqueta de 4x4 cm a 300 DPI
    val bitmapEtiqueta = remember(equipo) {
        UtilidadQR.generarEtiqueta4x4cm(equipo, dpi = 300)
    }

    AlertDialog(
        onDismissRequest = onDescartar,
        title = {
            Column {
                Text("Etiqueta QR (4x4 cm)")
                Text(
                    text = equipo.nombre,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Previsualización de la etiqueta gráfica 4x4 cm
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmapEtiqueta.asImageBitmap(),
                        contentDescription = "Código QR de ${equipo.nombre}",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    text = "Formato preparado para impresión física de 4x4 cm (JPG / Impresora).",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                    Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val uriResult = UtilidadQR.exportarImagenJpg(context, bitmapEtiqueta, equipo.nombre)
                            if (uriResult != null) {
                                Toast.makeText(
                                    context,
                                    "Etiqueta JPG guardada en Galería / Imágenes",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Error al exportar la imagen JPG",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exportar JPG")
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            UtilidadQR.imprimirQr(context, bitmapEtiqueta, equipo.nombre)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Imprimir")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDescartar) {
                Text("Cerrar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoRegistroEquipo(
    onDescartar: () -> Unit,
    onGuardar: (
        nombre: String,
        categoria: String,
        numeroSerie: String,
        codigoQr: String,
        urlImagen: String?,
        onResultado: (Resultado<Unit>) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Audio") }
    var numeroSerie by remember { mutableStateOf("") }
    var codigoQr by remember { mutableStateOf("") }
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var urlImagenGuardada by remember { mutableStateOf<String?>(null) }
    var mostrandoCamara by remember { mutableStateOf(false) }

    var mensajeError by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    if (mostrandoCamara) {
        Dialog(
            onDismissRequest = { mostrandoCamara = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CameraPreview(
                    onFotoCapturada = { bitmap, _ ->
                        try {
                            val base64Str = UtilidadImagen.bitmapABase64(bitmap)
                            fotoBitmap = bitmap
                            urlImagenGuardada = base64Str
                        } catch (e: Exception) {
                            mensajeError = "Error al guardar la foto: ${e.message}"
                        }
                        mostrandoCamara = false
                    },
                    onCancelar = { mostrandoCamara = false }
                )
            }
        }
    } else {
        AlertDialog(
            onDismissRequest = onDescartar,
            title = { Text("Registrar Nuevo Equipo") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (mensajeError != null) {
                        Text(
                            text = mensajeError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del Equipo *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val categoriasReg by CatalogoCategoriasMarcas.listaCategorias.collectAsState()
                    var menuCatRegExp by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = menuCatRegExp,
                        onExpandedChange = { menuCatRegExp = !menuCatRegExp }
                    ) {
                        OutlinedTextField(
                            value = categoria,
                            onValueChange = { categoria = it },
                            label = { Text("Categoría Oficial *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuCatRegExp) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = menuCatRegExp,
                            onDismissRequest = { menuCatRegExp = false }
                        ) {
                            categoriasReg.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        categoria = cat
                                        menuCatRegExp = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = numeroSerie,
                        onValueChange = { numeroSerie = it },
                        label = { Text("Número de Serie (N/S) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = codigoQr,
                        onValueChange = { codigoQr = it },
                        label = { Text("Código QR personalizado") },
                        placeholder = { Text("Autogenerado si se deja en blanco") },
                        supportingText = { Text("Si se deja vacío, la app creará un código único automáticamente.") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Evidencia Fotográfica del Equipo *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (fotoBitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                bitmap = fotoBitmap!!.asImageBitmap(),
                                contentDescription = "Evidencia del equipo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Fotografía requerida",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { mostrandoCamara = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (fotoBitmap == null) "Tomar Foto del Equipo" else "Cambiar Fotografía")
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !cargando && nombre.isNotBlank() && numeroSerie.isNotBlank() && urlImagenGuardada != null,
                    onClick = {
                        cargando = true
                        mensajeError = null
                        val qrFinal = codigoQr.ifBlank { "QR-${System.currentTimeMillis()}" }
                        onGuardar(nombre, categoria, numeroSerie, qrFinal, urlImagenGuardada) { resultado ->
                            cargando = false
                            when (resultado) {
                                is Resultado.Exito -> onDescartar()
                                is Resultado.Error -> mensajeError = resultado.excepcion.message
                                is Resultado.Cargando -> {}
                            }
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDescartar) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoEditarEquipo(
    equipo: Equipo,
    onDescartar: () -> Unit,
    onGuardarCambios: (
        equipoActualizado: Equipo,
        onResultado: (Resultado<Unit>) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf(equipo.nombre) }
    var categoria by remember { mutableStateOf(equipo.categoria) }
    var numeroSerie by remember { mutableStateOf(equipo.numeroSerie) }
    var codigoQr by remember { mutableStateOf(equipo.codigoQR) }
    var ubicacion by remember { mutableStateOf(equipo.ubicacionActual) }

    val bitmapInicial = recordarBitmapEquipo(context, equipo.urlImagen)
    var fotoBitmap by remember(bitmapInicial) {
        mutableStateOf<Bitmap?>(bitmapInicial)
    }
    var urlImagenGuardada by remember { mutableStateOf(equipo.urlImagen) }
    var mostrandoCamara by remember { mutableStateOf(false) }

    var mensajeError by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    if (mostrandoCamara) {
        Dialog(
            onDismissRequest = { mostrandoCamara = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CameraPreview(
                    onFotoCapturada = { bitmap, _ ->
                        try {
                            val base64Str = UtilidadImagen.bitmapABase64(bitmap)
                            fotoBitmap = bitmap
                            urlImagenGuardada = base64Str
                        } catch (e: Exception) {
                            mensajeError = "Error al actualizar la foto: ${e.message}"
                        }
                        mostrandoCamara = false
                    },
                    onCancelar = { mostrandoCamara = false }
                )
            }
        }
    } else {
        AlertDialog(
            onDismissRequest = onDescartar,
            title = { Text("Editar Equipo de Bodega") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (mensajeError != null) {
                        Text(
                            text = mensajeError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del Equipo *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val categoriasEdit by CatalogoCategoriasMarcas.listaCategorias.collectAsState()
                    var menuCatEditExp by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = menuCatEditExp,
                        onExpandedChange = { menuCatEditExp = !menuCatEditExp }
                    ) {
                        OutlinedTextField(
                            value = categoria,
                            onValueChange = { categoria = it },
                            label = { Text("Categoría Oficial *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuCatEditExp) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = menuCatEditExp,
                            onDismissRequest = { menuCatEditExp = false }
                        ) {
                            categoriasEdit.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        categoria = cat
                                        menuCatEditExp = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = numeroSerie,
                        onValueChange = { numeroSerie = it },
                        label = { Text("Número de Serie (N/S) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = codigoQr,
                        onValueChange = { codigoQr = it },
                        label = { Text("Código QR personalizado *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = ubicacion,
                        onValueChange = { ubicacion = it },
                        label = { Text("Ubicación en Bodega / Recinto") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Fotografía de Evidencia",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (fotoBitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                bitmap = fotoBitmap!!.asImageBitmap(),
                                contentDescription = "Evidencia del equipo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { mostrandoCamara = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (fotoBitmap == null) "Capturar Fotografía" else "Actualizar Fotografía")
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !cargando && nombre.isNotBlank() && numeroSerie.isNotBlank() && codigoQr.isNotBlank(),
                    onClick = {
                        cargando = true
                        mensajeError = null
                        val equipoEditado = equipo.copy(
                            nombre = nombre,
                            categoria = categoria,
                            numeroSerie = numeroSerie,
                            codigoQR = codigoQr,
                            ubicacionActual = ubicacion,
                            urlImagen = urlImagenGuardada
                        )
                        onGuardarCambios(equipoEditado) { resultado ->
                            cargando = false
                            when (resultado) {
                                is Resultado.Exito -> onDescartar()
                                is Resultado.Error -> mensajeError = resultado.excepcion.message
                                is Resultado.Cargando -> {}
                            }
                        }
                    }
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = onDescartar) {
                    Text("Cancelar")
                }
            }
        )
    }
}

fun obtenerEtiquetaEstado(estado: EstadoEquipo): String {
    return when (estado) {
        EstadoEquipo.EN_BODEGA -> "En Bodega"
        EstadoEquipo.EN_CUSTODIA -> "En Custodia"
        EstadoEquipo.EN_TRANSITO -> "En Tránsito"
        EstadoEquipo.EN_PROCESO_DEVOLUCION -> "En Devolución"
        EstadoEquipo.REPORTADO_DANADO -> "Reportado Dañado"
        EstadoEquipo.EN_MANTENIMIENTO -> "En Mantenimiento"
        EstadoEquipo.DEVUELTO -> "Devuelto"
        EstadoEquipo.DADO_DE_BAJA -> "Dado de Baja (Archivado)"
    }
}

@Composable
fun colorPorEstado(estado: EstadoEquipo) = when (estado) {
    EstadoEquipo.EN_BODEGA -> MaterialTheme.colorScheme.primary
    EstadoEquipo.EN_CUSTODIA -> MaterialTheme.colorScheme.tertiary
    EstadoEquipo.EN_TRANSITO -> MaterialTheme.colorScheme.secondary
    EstadoEquipo.EN_PROCESO_DEVOLUCION -> MaterialTheme.colorScheme.tertiary
    EstadoEquipo.REPORTADO_DANADO -> MaterialTheme.colorScheme.error
    EstadoEquipo.EN_MANTENIMIENTO -> Color(0xFFFF9800)
    EstadoEquipo.DEVUELTO -> MaterialTheme.colorScheme.outline
    EstadoEquipo.DADO_DE_BAJA -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
}
