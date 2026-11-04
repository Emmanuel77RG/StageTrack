package com.example.stagetrack.ui.pantallas

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.CategoriaEvento
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.EstadoEquipo
import com.example.stagetrack.dominio.modelo.EstadoEvento
import com.example.stagetrack.dominio.modelo.Evento
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.dominio.modelo.Usuario
import com.example.stagetrack.ui.componentes.AvatarUsuario
import com.example.stagetrack.ui.viewmodel.AutenticacionViewModel
import com.example.stagetrack.ui.viewmodel.EquipoViewModel
import com.example.stagetrack.ui.viewmodel.EventoViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionEventos(
    eventoViewModel: EventoViewModel,
    autenticacionViewModel: AutenticacionViewModel,
    equipoViewModel: EquipoViewModel? = null,
    onVolverClick: () -> Unit,
    onVerInventarioEventoClick: (Evento) -> Unit,
    onVerDetalleEventoClick: (Evento) -> Unit = {}
) {
    val usuarioActual by autenticacionViewModel.usuarioActual.collectAsState()
    val listaUsuarios by autenticacionViewModel.listaUsuarios.collectAsState()
    val todosEquipos by equipoViewModel?.listaEquipos?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    LaunchedEffect(usuarioActual) {
        eventoViewModel.establecerUsuarioActual(usuarioActual)
    }

    val eventos by eventoViewModel.listaEventos.collectAsState()

    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var eventoParaEditar by remember { mutableStateOf<Evento?>(null) }
    var eventoParaAsignarPersonal by remember { mutableStateOf<Evento?>(null) }
    var eventoParaReservarEquipos by remember { mutableStateOf<Evento?>(null) }

    val puedeCrearEventos = usuarioActual?.rol == RolUsuario.SUPERVISOR || usuarioActual?.rol == RolUsuario.ADMINISTRADOR_GENERAL
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (usuarioActual?.rol == RolUsuario.TECNICO) "Mis Eventos Asignados" else "Proyectos y Eventos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "StageTrack Operations",
                            style = MaterialTheme.typography.labelMedium,
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
                }
            )
        },
        floatingActionButton = {
            if (puedeCrearEventos) {
                FloatingActionButton(
                    onClick = { mostrarDialogoCrear = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo Evento")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (eventos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay eventos o proyectos registrados.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(eventos, key = { it.id }) { evento ->
                        TarjetaEventoItem(
                            evento = evento,
                            usuarioActual = usuarioActual,
                            onAdministrarClick = { onVerDetalleEventoClick(evento) },
                            onEditarClick = { eventoParaEditar = evento },
                            onCambiarEstado = { nuevoEstado ->
                                eventoViewModel.cambiarEstadoEvento(evento.id, nuevoEstado) {}
                            }
                        )
                    }
                }
            }
        }

        // 1. Diálogo Etapa 1: Creación Rápida del Proyecto
        if (mostrarDialogoCrear && usuarioActual != null) {
            DialogoCrearEventoRapido(
                usuarioActual = usuarioActual!!,
                onDismiss = { mostrarDialogoCrear = false },
                onGuardar = { nombre, cliente, categoria, inicio, fin, ubicacion, notas, onResultado ->
                    eventoViewModel.crearNuevoEvento(
                        context = context,
                        nombre = nombre,
                        cliente = cliente,
                        categoria = categoria,
                        fechaInicio = inicio,
                        fechaFin = fin,
                        ubicacion = ubicacion,
                        supervisor = usuarioActual!!,
                        tecnicosAsignados = emptyList(),
                        equiposAsignados = emptyList(),
                        notasMontaje = notas,
                        onResultado = { res ->
                            if (res is Resultado.Exito) {
                                mostrarDialogoCrear = false
                            }
                            onResultado(res)
                        }
                    )
                }
            )
        }

        // 2. Diálogo Etapa 2: Asignar Personal (Estilo Asana / Monday)
        if (eventoParaAsignarPersonal != null) {
            DialogoAsignarPersonalEvento(
                evento = eventoParaAsignarPersonal!!,
                usuariosDisponibles = listaUsuarios,
                onDismiss = { eventoParaAsignarPersonal = null },
                onGuardar = { jefeBodega, tecnicos, onResultado ->
                    eventoViewModel.asignarPersonalAEvento(
                        context = context,
                        evento = eventoParaAsignarPersonal!!,
                        jefeBodega = jefeBodega,
                        tecnicos = tecnicos,
                        onResultado = { res ->
                            if (res is Resultado.Exito) {
                                eventoParaAsignarPersonal = null
                            }
                            onResultado(res)
                        }
                    )
                }
            )
        }

        // 3. Diálogo Etapa 3: Reservar Equipos de Bodega (Rider)
        if (eventoParaReservarEquipos != null) {
            val equiposEnBodega = remember(todosEquipos) {
                todosEquipos.filter { it.estadoActual == EstadoEquipo.EN_BODEGA || eventoParaReservarEquipos!!.idsEquiposAsignados.contains(it.id) }
            }

            DialogoReservarEquiposEvento(
                evento = eventoParaReservarEquipos!!,
                equiposDisponibles = equiposEnBodega,
                onDismiss = { eventoParaReservarEquipos = null },
                onGuardar = { equiposSeleccionados, onResultado ->
                    eventoViewModel.reservarEquiposAEvento(
                        context = context,
                        evento = eventoParaReservarEquipos!!,
                        equipos = equiposSeleccionados,
                        onResultado = { res ->
                            if (res is Resultado.Exito) {
                                eventoParaReservarEquipos = null
                            }
                            onResultado(res)
                        }
                    )
                }
            )
        }

        // 4. Diálogo de Edición de Datos del Proyecto
        if (eventoParaEditar != null) {
            DialogoEditarEvento(
                evento = eventoParaEditar!!,
                onDismiss = { eventoParaEditar = null },
                onGuardar = { eventoActualizado, onResultado ->
                    eventoViewModel.actualizarEventoExistente(
                        eventoActualizado = eventoActualizado,
                        onResultado = { res ->
                            if (res is Resultado.Exito) {
                                eventoParaEditar = null
                            }
                            onResultado(res)
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun TarjetaEventoItem(
    evento: Evento,
    usuarioActual: Usuario?,
    onAdministrarClick: () -> Unit,
    onEditarClick: () -> Unit,
    onCambiarEstado: (EstadoEvento) -> Unit
) {
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val fechaTexto = remember(evento.fechaInicio, evento.fechaFin) {
        "${formatoFecha.format(Date(evento.fechaInicio))} - ${formatoFecha.format(Date(evento.fechaFin))}"
    }

    val esSupervisor = usuarioActual?.rol == RolUsuario.SUPERVISOR || usuarioActual?.rol == RolUsuario.ADMINISTRADOR_GENERAL

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = evento.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Cliente: ${evento.cliente}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(etiquetaEstado(evento.estado), fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = colorEstadoContainer(evento.estado)
                        )
                    )

                    if (esSupervisor) {
                        Spacer(modifier = Modifier.height(2.dp))
                        IconButton(
                            onClick = onEditarClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar Proyecto",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {},
                    label = { Text(etiquetaCategoria(evento.categoria), fontWeight = FontWeight.Bold) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = evento.ubicacion,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = fechaTexto,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Bodega a Cargo: ${evento.nombreJefeBodega ?: "Sin asignar"}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (evento.nombreJefeBodega != null) FontWeight.Bold else FontWeight.Normal,
                    color = if (evento.nombreJefeBodega != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Supervisor: ${evento.nombreSupervisor} | ${evento.idsTecnicosAsignados.size} técnico(s) | ${evento.idsEquiposAsignados.size} equipo(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!evento.notasMontaje.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Notas: ${evento.notasMontaje}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val esGestionador = usuarioActual?.rol == RolUsuario.SUPERVISOR ||
                        usuarioActual?.rol == RolUsuario.ADMINISTRADOR_GENERAL ||
                        usuarioActual?.rol == RolUsuario.JEFE_BODEGA

                val textoBoton = if (esGestionador) "Administrar" else "Ver más información"
                val iconoBoton = if (esGestionador) Icons.Default.Settings else Icons.Default.Info

                Button(
                    onClick = onAdministrarClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = iconoBoton,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(textoBoton, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                if (esSupervisor && evento.estado == EstadoEvento.PLANIFICADO) {
                    Button(
                        onClick = { onCambiarEstado(EstadoEvento.EN_CURSO) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Iniciar Evento", fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                } else if (esSupervisor && evento.estado == EstadoEvento.EN_CURSO) {
                    Button(
                        onClick = { onCambiarEstado(EstadoEvento.FINALIZADO) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Finalizar Show", fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
    }
}

/**
 * Diálogo Etapa 1: Creación Rápida de Proyecto/Evento (10 Segundos)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DialogoCrearEventoRapido(
    usuarioActual: Usuario,
    onDismiss: () -> Unit,
    onGuardar: (
        nombre: String,
        cliente: String,
        categoria: CategoriaEvento,
        fechaInicio: Long,
        fechaFin: Long,
        ubicacion: String,
        notasMontaje: String?,
        onResultado: (Resultado<Unit>) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    val formatoFecha = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var nombre by remember { mutableStateOf("") }
    var cliente by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf(CategoriaEvento.CONCIERTO_SHOW) }

    val horaActual = System.currentTimeMillis()
    var fechaInicio by remember { mutableStateOf(horaActual) }
    var fechaFin by remember { mutableStateOf(horaActual + (3 * 24 * 60 * 60 * 1000L)) }
    var notasMontaje by remember { mutableStateOf("") }

    var mensajeError by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    val calInicio = remember(fechaInicio) { Calendar.getInstance().apply { timeInMillis = fechaInicio } }
    val datePickerInicio = DatePickerDialog(
        context,
        { _, y, m, d ->
            calInicio.set(y, m, d)
            fechaInicio = calInicio.timeInMillis
            if (fechaFin < fechaInicio) {
                fechaFin = fechaInicio + (24 * 60 * 60 * 1000L)
            }
        },
        calInicio.get(Calendar.YEAR),
        calInicio.get(Calendar.MONTH),
        calInicio.get(Calendar.DAY_OF_MONTH)
    )

    val calFin = remember(fechaFin) { Calendar.getInstance().apply { timeInMillis = fechaFin } }
    val datePickerFin = DatePickerDialog(
        context,
        { _, y, m, d ->
            calFin.set(y, m, d)
            if (calFin.timeInMillis >= fechaInicio) {
                fechaFin = calFin.timeInMillis
            } else {
                mensajeError = "La fecha fin no puede ser anterior a la de inicio."
            }
        },
        calFin.get(Calendar.YEAR),
        calFin.get(Calendar.MONTH),
        calFin.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Crear Proyecto / Evento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Supervisor: ${usuarioActual.nombre}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
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
                    onValueChange = { nombre = it; mensajeError = null },
                    label = { Text("Nombre del Evento") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cliente,
                    onValueChange = { cliente = it; mensajeError = null },
                    label = { Text("Cliente / Productora") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it; mensajeError = null },
                    label = { Text("Ubicación / Recinto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Categoría de Producción:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CategoriaEvento.entries.forEach { cat ->
                        FilterChip(
                            selected = categoriaSeleccionada == cat,
                            onClick = { categoriaSeleccionada = cat },
                            label = { Text(etiquetaCategoria(cat), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text("Fechas del Proyecto:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { datePickerInicio.show() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Inicio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text(formatoFecha.format(Date(fechaInicio)), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { datePickerFin.show() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Fin / Desmontaje", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text(formatoFecha.format(Date(fechaFin)), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                OutlinedTextField(
                    value = notasMontaje,
                    onValueChange = { notasMontaje = it },
                    label = { Text("Notas u Observaciones de Montaje") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !cargando && nombre.isNotBlank() && cliente.isNotBlank() && ubicacion.isNotBlank(),
                onClick = {
                    cargando = true
                    mensajeError = null
                    onGuardar(
                        nombre,
                        cliente,
                        categoriaSeleccionada,
                        fechaInicio,
                        fechaFin,
                        ubicacion,
                        notasMontaje.ifBlank { null }
                    ) { res ->
                        cargando = false
                        if (res is Resultado.Error) {
                            mensajeError = res.excepcion.message
                        }
                    }
                }
            ) {
                Text("Crear Proyecto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo Etapa 2: Asignar Personal con Diseño Estilo Asana / Monday (Avatares, Filtros y Badges)
 */
@Composable
fun DialogoAsignarPersonalEvento(
    evento: Evento,
    usuariosDisponibles: List<Usuario>,
    onDismiss: () -> Unit,
    onGuardar: (
        jefeBodega: Usuario?,
        tecnicos: List<Usuario>,
        onResultado: (Resultado<Unit>) -> Unit
    ) -> Unit
) {
    var jefeBodegaSeleccionado by remember {
        mutableStateOf(usuariosDisponibles.find { it.id == evento.idJefeBodega })
    }
    val tecnicosSeleccionados = remember {
        mutableStateListOf<Usuario>().apply {
            addAll(usuariosDisponibles.filter { evento.idsTecnicosAsignados.contains(it.id) })
        }
    }

    var textoBusquedaTecnico by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    val jefesBodegaPosibles = remember(usuariosDisponibles) {
        usuariosDisponibles.filter { (it.rol == RolUsuario.JEFE_BODEGA || it.rol == RolUsuario.ADMINISTRADOR_GENERAL) && it.activo }
    }

    val tecnicosPosibles = remember(usuariosDisponibles, textoBusquedaTecnico) {
        usuariosDisponibles.filter {
            it.rol == RolUsuario.TECNICO && it.activo &&
            (textoBusquedaTecnico.isBlank() ||
             it.nombre.contains(textoBusquedaTecnico, ignoreCase = true) ||
             it.correo.contains(textoBusquedaTecnico, ignoreCase = true))
        }
    }

    val totalConvocados = (if (jefeBodegaSeleccionado != null) 1 else 0) + tecnicosSeleccionados.size

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Convocatoria de Personal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Proyecto: ${evento.nombre}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                // Barra con Avatares Apilados
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Equipo asignado ($totalConvocados):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                            if (jefeBodegaSeleccionado != null) {
                                AvatarUsuario(
                                    urlFotografia = jefeBodegaSeleccionado!!.urlFotografia,
                                    nombreUsuario = jefeBodegaSeleccionado!!.nombre,
                                    tamano = 28.dp
                                )
                            }
                            tecnicosSeleccionados.take(4).forEach { tec ->
                                AvatarUsuario(
                                    urlFotografia = tec.urlFotografia,
                                    nombreUsuario = tec.nombre,
                                    tamano = 28.dp
                                )
                            }
                        }
                    }
                }

                if (mensajeError != null) {
                    Text(
                        text = mensajeError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sección 1: Jefe de Bodega Responsable
                    item {
                        Text(
                            text = "Jefe de Bodega a Cargo:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                    }

                    if (jefesBodegaPosibles.isEmpty()) {
                        item {
                            Text("No hay Jefes de Bodega activos.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        items(jefesBodegaPosibles, key = { "jb_${it.id}" }) { jb ->
                            val esSeleccionado = jefeBodegaSeleccionado?.id == jb.id
                            ItemFilaUsuarioAsana(
                                usuario = jb,
                                etiquetaRol = "Bodega",
                                seleccionado = esSeleccionado,
                                onToggle = {
                                    jefeBodegaSeleccionado = if (esSeleccionado) null else jb
                                }
                            )
                        }
                    }

                    // Sección 2: Cuadrilla de Técnicos
                    item {
                        Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                            Text(
                                text = "Cuadrilla de Técnicos (${tecnicosSeleccionados.size}):",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = textoBusquedaTecnico,
                                onValueChange = { textoBusquedaTecnico = it },
                                placeholder = { Text("Buscar técnico por nombre o correo...") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (tecnicosPosibles.isEmpty()) {
                        item {
                            Text("No se encontraron técnicos activos.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        items(tecnicosPosibles, key = { "tec_${it.id}" }) { tec ->
                            val esSeleccionado = tecnicosSeleccionados.contains(tec)
                            ItemFilaUsuarioAsana(
                                usuario = tec,
                                etiquetaRol = "Técnico",
                                seleccionado = esSeleccionado,
                                onToggle = {
                                    if (esSeleccionado) tecnicosSeleccionados.remove(tec)
                                    else tecnicosSeleccionados.add(tec)
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !cargando,
                onClick = {
                    cargando = true
                    mensajeError = null
                    onGuardar(jefeBodegaSeleccionado, tecnicosSeleccionados.toList()) { res ->
                        cargando = false
                        if (res is Resultado.Error) {
                            mensajeError = res.excepcion.message
                        }
                    }
                }
            ) {
                Text("Guardar y Notificar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Fila de Usuario estilo Asana / Monday.com con Avatar, Correo, Badge y Botón de Estado
 */
@Composable
private fun ItemFilaUsuarioAsana(
    usuario: Usuario,
    etiquetaRol: String,
    seleccionado: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = if (seleccionado)
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarUsuario(
                    urlFotografia = usuario.urlFotografia,
                    nombreUsuario = usuario.nombre,
                    tamano = 42.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = usuario.nombre,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = usuario.correo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            FilterChip(
                selected = seleccionado,
                onClick = onToggle,
                label = {
                    Text(
                        text = if (seleccionado) "✓ $etiquetaRol" else "+ $etiquetaRol",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = if (seleccionado) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else null
            )
        }
    }
}

/**
 * Diálogo Etapa 3: Reservar Equipos de Bodega (Rider Técnico)
 */
@Composable
fun DialogoReservarEquiposEvento(
    evento: Evento,
    equiposDisponibles: List<Equipo>,
    onDismiss: () -> Unit,
    onGuardar: (
        equipos: List<Equipo>,
        onResultado: (Resultado<Unit>) -> Unit
    ) -> Unit
) {
    val equiposSeleccionados = remember {
        mutableStateListOf<Equipo>().apply {
            addAll(equiposDisponibles.filter { evento.idsEquiposAsignados.contains(it.id) })
        }
    }

    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Reservar Equipos de Bodega", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Evento: ${evento.nombre}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                if (mensajeError != null) {
                    Text(text = mensajeError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Text("Equipos Reservados (${equiposSeleccionados.size}):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (equiposDisponibles.isEmpty()) {
                    Text("No hay equipos disponibles en Bodega.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(equiposDisponibles, key = { it.id }) { eq ->
                            val sel = equiposSeleccionados.contains(eq)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(value = sel, onValueChange = {
                                        if (sel) equiposSeleccionados.remove(eq) else equiposSeleccionados.add(eq)
                                    }, role = Role.Checkbox)
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = sel, onCheckedChange = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(eq.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("${eq.categoria} | N/S: ${eq.numeroSerie}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !cargando,
                onClick = {
                    cargando = true
                    mensajeError = null
                    onGuardar(equiposSeleccionados.toList()) { res ->
                        cargando = false
                        if (res is Resultado.Error) {
                            mensajeError = res.excepcion.message
                        }
                    }
                }
            ) {
                Text("Guardar y Enviar Lista a Bodega")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo para la edición de datos del evento/proyecto.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DialogoEditarEvento(
    evento: Evento,
    onDismiss: () -> Unit,
    onGuardar: (
        eventoActualizado: Evento,
        onResultado: (Resultado<Unit>) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    val formatoFecha = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var nombre by remember { mutableStateOf(evento.nombre) }
    var cliente by remember { mutableStateOf(evento.cliente) }
    var ubicacion by remember { mutableStateOf(evento.ubicacion) }
    var categoriaSeleccionada by remember { mutableStateOf(evento.categoria) }

    var fechaInicio by remember { mutableStateOf(evento.fechaInicio) }
    var fechaFin by remember { mutableStateOf(evento.fechaFin) }
    var notasMontaje by remember { mutableStateOf(evento.notasMontaje ?: "") }

    var mensajeError by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    val calInicio = remember(fechaInicio) { Calendar.getInstance().apply { timeInMillis = fechaInicio } }
    val datePickerInicio = DatePickerDialog(
        context,
        { _, y, m, d ->
            calInicio.set(y, m, d)
            fechaInicio = calInicio.timeInMillis
            if (fechaFin < fechaInicio) {
                fechaFin = fechaInicio + (24 * 60 * 60 * 1000L)
            }
        },
        calInicio.get(Calendar.YEAR),
        calInicio.get(Calendar.MONTH),
        calInicio.get(Calendar.DAY_OF_MONTH)
    )

    val calFin = remember(fechaFin) { Calendar.getInstance().apply { timeInMillis = fechaFin } }
    val datePickerFin = DatePickerDialog(
        context,
        { _, y, m, d ->
            calFin.set(y, m, d)
            if (calFin.timeInMillis >= fechaInicio) {
                fechaFin = calFin.timeInMillis
            } else {
                mensajeError = "La fecha fin no puede ser anterior a la de inicio."
            }
        },
        calFin.get(Calendar.YEAR),
        calFin.get(Calendar.MONTH),
        calFin.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Editar Proyecto / Evento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("ID: ${evento.id}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
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
                    onValueChange = { nombre = it; mensajeError = null },
                    label = { Text("Nombre del Evento") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cliente,
                    onValueChange = { cliente = it; mensajeError = null },
                    label = { Text("Cliente / Productora") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it; mensajeError = null },
                    label = { Text("Ubicación / Recinto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Categoría de Producción:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CategoriaEvento.entries.forEach { cat ->
                        FilterChip(
                            selected = categoriaSeleccionada == cat,
                            onClick = { categoriaSeleccionada = cat },
                            label = { Text(etiquetaCategoria(cat), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text("Fechas del Proyecto:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { datePickerInicio.show() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Inicio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text(formatoFecha.format(Date(fechaInicio)), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { datePickerFin.show() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Fin", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text(formatoFecha.format(Date(fechaFin)), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                OutlinedTextField(
                    value = notasMontaje,
                    onValueChange = { notasMontaje = it },
                    label = { Text("Notas u Observaciones de Montaje") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !cargando && nombre.isNotBlank() && cliente.isNotBlank() && ubicacion.isNotBlank(),
                onClick = {
                    cargando = true
                    mensajeError = null
                    val actualizado = evento.copy(
                        nombre = nombre,
                        cliente = cliente,
                        categoria = categoriaSeleccionada,
                        fechaInicio = fechaInicio,
                        fechaFin = fechaFin,
                        ubicacion = ubicacion,
                        notasMontaje = notasMontaje.ifBlank { null }
                    )
                    onGuardar(actualizado) { res ->
                        cargando = false
                        if (res is Resultado.Error) {
                            mensajeError = res.excepcion.message
                        }
                    }
                }
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

fun etiquetaCategoria(cat: CategoriaEvento): String {
    return when (cat) {
        CategoriaEvento.CONCIERTO_SHOW -> "Concierto / Show"
        CategoriaEvento.CORPORATIVO -> "Evento Corporativo"
        CategoriaEvento.FESTIVAL -> "Festival"
        CategoriaEvento.TEATRO -> "Obra de Teatro"
        CategoriaEvento.OTRO -> "Producción"
    }
}

fun etiquetaEstado(estado: EstadoEvento): String {
    return when (estado) {
        EstadoEvento.PLANIFICADO -> "Planificado"
        EstadoEvento.EN_CURSO -> "En Curso"
        EstadoEvento.FINALIZADO -> "Finalizado"
        EstadoEvento.CANCELADO -> "Cancelado"
    }
}

@Composable
fun colorEstadoContainer(estado: EstadoEvento): androidx.compose.ui.graphics.Color {
    return when (estado) {
        EstadoEvento.PLANIFICADO -> MaterialTheme.colorScheme.primaryContainer
        EstadoEvento.EN_CURSO -> MaterialTheme.colorScheme.tertiaryContainer
        EstadoEvento.FINALIZADO -> MaterialTheme.colorScheme.surfaceVariant
        EstadoEvento.CANCELADO -> MaterialTheme.colorScheme.errorContainer
    }
}
