package com.example.stagetrack.ui.pantallas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.core.imagen.recordarBitmapEquipo
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
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleEvento(
    idEvento: String,
    eventoViewModel: EventoViewModel,
    autenticacionViewModel: AutenticacionViewModel,
    equipoViewModel: EquipoViewModel? = null,
    onVolverClick: () -> Unit,
    onVerInventarioEventoClick: (Evento) -> Unit
) {
    val context = LocalContext.current
    val usuarioActual by autenticacionViewModel.usuarioActual.collectAsState()
    val listaUsuarios by autenticacionViewModel.listaUsuarios.collectAsState()
    val listaEventos by eventoViewModel.listaEventos.collectAsState()
    val todosEquipos by equipoViewModel?.listaEquipos?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    val evento = remember(listaEventos, idEvento) {
        listaEventos.find { it.id == idEvento }
    }

    if (evento == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cargando detalles del proyecto...")
        }
        return
    }

    var pestañaSeleccionada by remember { mutableStateOf(0) } // 0: Personal, 1: Equipos, 2: Resumen
    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    val esSupervisor = usuarioActual?.rol == RolUsuario.SUPERVISOR || usuarioActual?.rol == RolUsuario.ADMINISTRADOR_GENERAL
    val esJefeBodegaAsignado = usuarioActual?.id == evento.idJefeBodega || usuarioActual?.rol == RolUsuario.JEFE_BODEGA || usuarioActual?.rol == RolUsuario.ADMINISTRADOR_GENERAL
    val puedeGestionarEquipos = esSupervisor || esJefeBodegaAsignado

    // Estados para asignación de personal estilo Asana
    var jefeBodegaSeleccionado by remember(evento, listaUsuarios) {
        mutableStateOf(listaUsuarios.find { it.id == evento.idJefeBodega })
    }
    val tecnicosSeleccionados = remember(evento, listaUsuarios) {
        mutableStateListOf<Usuario>().apply {
            addAll(listaUsuarios.filter { evento.idsTecnicosAsignados.contains(it.id) })
        }
    }
    var textoBusquedaPersonal by remember { mutableStateOf("") }
    var guardandoPersonal by remember { mutableStateOf(false) }

    // Estados para reserva de equipos (Rider)
    val equiposSeleccionados = remember(evento, todosEquipos) {
        mutableStateListOf<Equipo>().apply {
            addAll(todosEquipos.filter { evento.idsEquiposAsignados.contains(it.id) })
        }
    }
    var textoBusquedaEquipo by remember { mutableStateOf("") }
    var guardandoEquipos by remember { mutableStateOf(false) }

    val jefesBodegaPosibles = remember(listaUsuarios) {
        listaUsuarios.filter { (it.rol == RolUsuario.JEFE_BODEGA || it.rol == RolUsuario.ADMINISTRADOR_GENERAL) && it.activo }
    }

    val tecnicosPosibles = remember(listaUsuarios, textoBusquedaPersonal) {
        listaUsuarios.filter {
            it.rol == RolUsuario.TECNICO && it.activo &&
            (textoBusquedaPersonal.isBlank() ||
             it.nombre.contains(textoBusquedaPersonal, ignoreCase = true) ||
             it.correo.contains(textoBusquedaPersonal, ignoreCase = true))
        }
    }

    val equiposDisponiblesBodega = remember(todosEquipos, textoBusquedaEquipo) {
        todosEquipos.filter {
            (it.estadoActual == EstadoEquipo.EN_BODEGA || evento.idsEquiposAsignados.contains(it.id)) &&
            (textoBusquedaEquipo.isBlank() ||
             it.nombre.contains(textoBusquedaEquipo, ignoreCase = true) ||
             it.categoria.contains(textoBusquedaEquipo, ignoreCase = true) ||
             it.numeroSerie.contains(textoBusquedaEquipo, ignoreCase = true))
        }
    }

    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = evento.nombre,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Cliente: ${evento.cliente} | ${etiquetaCategoria(evento.categoria)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolverClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tarjeta Superior Resumen del Proyecto
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = evento.ubicacion, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }

                        AssistChip(
                            onClick = {},
                            label = { Text(etiquetaEstado(evento.estado)) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = colorEstadoContainer(evento.estado))
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${formatoFecha.format(Date(evento.fechaInicio))} - ${formatoFecha.format(Date(evento.fechaFin))}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (!evento.notasMontaje.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Notas: ${evento.notasMontaje}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            val mostrarTabChecklist = usuarioActual?.rol != RolUsuario.TECNICO

            if (!mostrarTabChecklist && pestañaSeleccionada == 2) {
                pestañaSeleccionada = 0
            }

            // Pestañas Principales
            TabRow(selectedTabIndex = pestañaSeleccionada) {
                Tab(
                    selected = pestañaSeleccionada == 0,
                    onClick = { pestañaSeleccionada = 0 },
                    icon = { Icon(imageVector = Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text("Personal (${(if (jefeBodegaSeleccionado != null) 1 else 0) + tecnicosSeleccionados.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = pestañaSeleccionada == 1,
                    onClick = { pestañaSeleccionada = 1 },
                    icon = { Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = { Text("Equipos (${equiposSeleccionados.size})", fontWeight = FontWeight.Bold) }
                )
                if (mostrarTabChecklist) {
                    Tab(
                        selected = pestañaSeleccionada == 2,
                        onClick = { pestañaSeleccionada = 2 },
                        icon = { Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        text = { Text("Checklist", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (pestañaSeleccionada) {
                    0 -> {
                        // PESTAÑA 1: Personal Convocado
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Barra Superior con Avatares Apilados
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Personal Convocado:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                                        if (jefeBodegaSeleccionado != null) {
                                            AvatarUsuario(urlFotografia = jefeBodegaSeleccionado!!.urlFotografia, nombreUsuario = jefeBodegaSeleccionado!!.nombre, tamano = 32.dp)
                                        }
                                        tecnicosSeleccionados.take(5).forEach { tec ->
                                            AvatarUsuario(urlFotografia = tec.urlFotografia, nombreUsuario = tec.nombre, tamano = 32.dp)
                                        }
                                    }
                                }
                            }

                            if (esSupervisor) {
                                OutlinedTextField(
                                    value = textoBusquedaPersonal,
                                    onValueChange = { textoBusquedaPersonal = it },
                                    placeholder = { Text("Buscar por nombre o correo...") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp)
                                )

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    item {
                                        Text("Jefe de Bodega a Cargo:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                                    }

                                    items(jefesBodegaPosibles, key = { "jb_${it.id}" }) { jb ->
                                        val esSel = jefeBodegaSeleccionado?.id == jb.id
                                        FilaUsuarioDetalleAsana(
                                            usuario = jb,
                                            rolEtiqueta = "Jefe de Bodega",
                                            seleccionado = esSel,
                                            habilitado = true,
                                            onToggle = {
                                                jefeBodegaSeleccionado = if (esSel) null else jb
                                            }
                                        )
                                    }

                                    item {
                                        Text("Cuadrilla de Técnicos (${tecnicosSeleccionados.size}):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                                    }

                                    items(tecnicosPosibles, key = { "tec_${it.id}" }) { tec ->
                                        val esSel = tecnicosSeleccionados.contains(tec)
                                        FilaUsuarioDetalleAsana(
                                            usuario = tec,
                                            rolEtiqueta = "Técnico",
                                            seleccionado = esSel,
                                            habilitado = true,
                                            onToggle = {
                                                if (esSel) tecnicosSeleccionados.remove(tec) else tecnicosSeleccionados.add(tec)
                                            }
                                        )
                                    }
                                }

                                Button(
                                    enabled = !guardandoPersonal,
                                    onClick = {
                                        guardandoPersonal = true
                                        eventoViewModel.asignarPersonalAEvento(
                                            context = context,
                                            evento = evento,
                                            jefeBodega = jefeBodegaSeleccionado,
                                            tecnicos = tecnicosSeleccionados.toList(),
                                            onResultado = {
                                                guardandoPersonal = false
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Guardar y Notificar Personal", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // Vista de Solo Consulta para Técnicos / Personal Asignado
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    item {
                                        Text("Jefe de Bodega a Cargo:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                                    }

                                    if (jefeBodegaSeleccionado != null) {
                                        item {
                                            FilaUsuarioDetalleAsana(
                                                usuario = jefeBodegaSeleccionado!!,
                                                rolEtiqueta = "Jefe de Bodega",
                                                seleccionado = true,
                                                habilitado = false,
                                                onToggle = {}
                                            )
                                        }
                                    } else {
                                        item {
                                            Text("Sin Jefe de Bodega asignado.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    item {
                                        Text("Cuadrilla de Técnicos Asignada (${tecnicosSeleccionados.size}):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                                    }

                                    if (tecnicosSeleccionados.isEmpty()) {
                                        item {
                                            Text("Sin técnicos asignados a este evento.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    } else {
                                        items(tecnicosSeleccionados, key = { "tec_${it.id}" }) { tec ->
                                            FilaUsuarioDetalleAsana(
                                                usuario = tec,
                                                rolEtiqueta = "Técnico Asignado",
                                                seleccionado = true,
                                                habilitado = false,
                                                onToggle = {}
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // PESTAÑA 2: Equipos del Evento (Rider Técnico Estilo Asana)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Barra Superior de Resumen de Equipos Estilo Asana
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Rider de Equipos Reservados:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${equiposSeleccionados.size} activos",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            if (puedeGestionarEquipos) {
                                OutlinedTextField(
                                    value = textoBusquedaEquipo,
                                    onValueChange = { textoBusquedaEquipo = it },
                                    placeholder = { Text("Buscar equipos por nombre, N/S...") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp)
                                )

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(equiposDisponiblesBodega, key = { it.id }) { eq ->
                                        val esSel = equiposSeleccionados.contains(eq)
                                        FilaEquipoDetalleAsana(
                                            equipo = eq,
                                            seleccionado = esSel,
                                            habilitado = true,
                                            onToggle = {
                                                if (esSel) equiposSeleccionados.remove(eq) else equiposSeleccionados.add(eq)
                                            }
                                        )
                                    }
                                }

                                Button(
                                    enabled = !guardandoEquipos,
                                    onClick = {
                                        guardandoEquipos = true
                                        eventoViewModel.reservarEquiposAEvento(
                                            context = context,
                                            evento = evento,
                                            equipos = equiposSeleccionados.toList(),
                                            onResultado = {
                                                guardandoEquipos = false
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Guardar Lista de Empaque y Notificar Bodega", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // Vista de Solo Consulta para Técnicos: Muestra únicamente el Rider de Equipos asignados al evento
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    item {
                                        Text("Equipos Asignados al Evento (${equiposSeleccionados.size}):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                                    }

                                    if (equiposSeleccionados.isEmpty()) {
                                        item {
                                            Text("No hay equipos asignados a este proyecto actualmente.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    } else {
                                        items(equiposSeleccionados, key = { it.id }) { eq ->
                                            FilaEquipoDetalleAsana(
                                                equipo = eq,
                                                seleccionado = true,
                                                habilitado = false,
                                                onToggle = {}
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // PESTAÑA 3: Checklist (Oculta para rol Técnico)
                        if (mostrarTabChecklist && equipoViewModel != null) {
                            ContenidoInventarioEventoEmbedded(
                                evento = evento,
                                equipoViewModel = equipoViewModel,
                                usuarioActual = usuarioActual,
                                modifier = Modifier.fillMaxSize(),
                                mostrarEncabezado = false
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Acceso no disponible.")
                            }
                        }
                    }
                }
            }
        }

        if (mostrarDialogoEditar) {
            DialogoEditarEvento(
                evento = evento,
                onDismiss = { mostrarDialogoEditar = false },
                onGuardar = { eventoActualizado, onResultado ->
                    eventoViewModel.actualizarEventoExistente(
                        eventoActualizado = eventoActualizado,
                        onResultado = { res ->
                            if (res is Resultado.Exito) {
                                mostrarDialogoEditar = false
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
private fun FilaUsuarioDetalleAsana(
    usuario: Usuario,
    rolEtiqueta: String,
    seleccionado: Boolean,
    habilitado: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = habilitado) { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = if (seleccionado) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                    tamano = 44.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = usuario.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = usuario.correo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (habilitado) {
                FilterChip(
                    selected = seleccionado,
                    onClick = onToggle,
                    label = {
                        Text(
                            text = rolEtiqueta,
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
            } else {
                AssistChip(
                    onClick = {},
                    label = { Text(if (seleccionado) "Asignado" else "Disponible") }
                )
            }
        }
    }
}

/**
 * Fila de selección de equipo estilo Asana / Monday con miniatura, categoría, número de serie
 * y badge interactivo de estado de reserva.
 */
@Composable
private fun FilaEquipoDetalleAsana(
    equipo: Equipo,
    seleccionado: Boolean,
    habilitado: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    val bitmapEquipo = recordarBitmapEquipo(context, equipo.urlImagen)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = habilitado) { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = if (seleccionado) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmapEquipo != null) {
                        Image(
                            bitmap = bitmapEquipo.asImageBitmap(),
                            contentDescription = "Foto de ${equipo.nombre}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
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

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = equipo.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${equipo.categoria} | N/S: ${equipo.numeroSerie}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Ubicación: ${equipo.ubicacionActual}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (habilitado) {
                FilterChip(
                    selected = seleccionado,
                    onClick = onToggle,
                    label = {
                        Text(
                            text = if (seleccionado) "Reservado" else "Disponible",
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
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Rider Asignado",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
