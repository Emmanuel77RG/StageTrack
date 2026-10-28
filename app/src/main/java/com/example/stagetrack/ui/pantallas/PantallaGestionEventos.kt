package com.example.stagetrack.ui.pantallas

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.EstadoEvento
import com.example.stagetrack.dominio.modelo.Evento
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.dominio.modelo.Usuario
import com.example.stagetrack.ui.viewmodel.AutenticacionViewModel
import com.example.stagetrack.ui.viewmodel.EventoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionEventos(
    eventoViewModel: EventoViewModel,
    autenticacionViewModel: AutenticacionViewModel,
    onVolverClick: () -> Unit,
    onVerInventarioEventoClick: (Evento) -> Unit
) {
    val usuarioActual by autenticacionViewModel.usuarioActual.collectAsState()
    val listaUsuarios by autenticacionViewModel.listaUsuarios.collectAsState()

    // Sincronizar el usuario actual en el ViewModel para filtrado de Flow
    LaunchedEffect(usuarioActual) {
        eventoViewModel.establecerUsuarioActual(usuarioActual)
    }

    val eventos by eventoViewModel.listaEventos.collectAsState()
    var mostrarDialogoCrear by remember { mutableStateOf(false) }

    val puedeCrearEventos = usuarioActual?.rol == RolUsuario.SUPERVISOR || usuarioActual?.rol == RolUsuario.ADMINISTRADOR_GENERAL

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
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo Evento")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nuevo Evento")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (eventos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (usuarioActual?.rol == RolUsuario.TECNICO)
                            "No tienes eventos asignados en este momento."
                        else
                            "No hay eventos registrados en el sistema.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(eventos, key = { it.id }) { evento ->
                        TarjetaEventoItem(
                            evento = evento,
                            usuarioActual = usuarioActual,
                            onVerInventarioClick = { onVerInventarioEventoClick(evento) },
                            onCambiarEstado = { nuevoEstado ->
                                eventoViewModel.cambiarEstadoEvento(evento.id, nuevoEstado) {}
                            }
                        )
                    }
                }
            }
        }

        if (mostrarDialogoCrear && usuarioActual != null) {
            DialogoCrearEvento(
                usuarioActual = usuarioActual!!,
                usuariosDisponibles = listaUsuarios,
                onDismiss = { mostrarDialogoCrear = false },
                onGuardar = { nombre, cliente, ubicacion, tecnicos, onResultado ->
                    val inicio = System.currentTimeMillis()
                    val fin = inicio + (3 * 24 * 60 * 60 * 1000L) // 3 días por defecto
                    eventoViewModel.crearNuevoEvento(
                        nombre = nombre,
                        cliente = cliente,
                        fechaInicio = inicio,
                        fechaFin = fin,
                        ubicacion = ubicacion,
                        supervisor = usuarioActual!!,
                        tecnicosAsignados = tecnicos,
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
    }
}

@Composable
fun TarjetaEventoItem(
    evento: Evento,
    usuarioActual: Usuario?,
    onVerInventarioClick: () -> Unit,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = evento.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cliente: ${evento.cliente}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AssistChip(
                    onClick = {},
                    label = { Text(etiquetaEstado(evento.estado)) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = colorEstadoContainer(evento.estado)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Supervisor: ${evento.nombreSupervisor} | ${evento.idsTecnicosAsignados.size} técnicos asignados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onVerInventarioClick) {
                    Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver Inventario")
                }

                if (esSupervisor && evento.estado == EstadoEvento.PLANIFICADO) {
                    TextButton(onClick = { onCambiarEstado(EstadoEvento.EN_CURSO) }) {
                        Text("Iniciar Evento")
                    }
                } else if (esSupervisor && evento.estado == EstadoEvento.EN_CURSO) {
                    TextButton(onClick = { onCambiarEstado(EstadoEvento.FINALIZADO) }) {
                        Text("Finalizar")
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoCrearEvento(
    usuarioActual: Usuario,
    usuariosDisponibles: List<Usuario>,
    onDismiss: () -> Unit,
    onGuardar: (
        nombre: String,
        cliente: String,
        ubicacion: String,
        tecnicos: List<Usuario>,
        onResultado: (Resultado<Unit>) -> Unit
    ) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var cliente by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    val tecnicosSeleccionados = remember { mutableStateListOf<Usuario>() }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    val listaTecnicosPosibles = remember(usuariosDisponibles) {
        usuariosDisponibles.filter { it.rol == RolUsuario.TECNICO && it.activo }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Nuevo Evento / Proyecto") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Seleccionar Técnicos Convocados (${tecnicosSeleccionados.size}):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                if (listaTecnicosPosibles.isEmpty()) {
                    Text(
                        text = "No hay técnicos registrados en el sistema.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        items(listaTecnicosPosibles, key = { it.id }) { tecnico ->
                            val seleccionado = tecnicosSeleccionados.contains(tecnico)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        value = seleccionado,
                                        onValueChange = {
                                            if (seleccionado) tecnicosSeleccionados.remove(tecnico)
                                            else tecnicosSeleccionados.add(tecnico)
                                        },
                                        role = Role.Checkbox
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = seleccionado,
                                    onCheckedChange = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = tecnico.nombre,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = tecnico.correo,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !cargando && nombre.isNotBlank() && cliente.isNotBlank() && ubicacion.isNotBlank(),
                onClick = {
                    cargando = true
                    mensajeError = null
                    onGuardar(nombre, cliente, ubicacion, tecnicosSeleccionados.toList()) { res ->
                        cargando = false
                        if (res is Resultado.Error) {
                            mensajeError = res.excepcion.message
                        }
                    }
                }
            ) {
                Text("Crear Evento")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun etiquetaEstado(estado: EstadoEvento): String {
    return when (estado) {
        EstadoEvento.PLANIFICADO -> "Planificado"
        EstadoEvento.EN_CURSO -> "En Curso"
        EstadoEvento.FINALIZADO -> "Finalizado"
        EstadoEvento.CANCELADO -> "Cancelado"
    }
}

@Composable
private fun colorEstadoContainer(estado: EstadoEvento) = when (estado) {
    EstadoEvento.PLANIFICADO -> MaterialTheme.colorScheme.secondaryContainer
    EstadoEvento.EN_CURSO -> MaterialTheme.colorScheme.primaryContainer
    EstadoEvento.FINALIZADO -> MaterialTheme.colorScheme.surfaceVariant
    EstadoEvento.CANCELADO -> MaterialTheme.colorScheme.errorContainer
}
