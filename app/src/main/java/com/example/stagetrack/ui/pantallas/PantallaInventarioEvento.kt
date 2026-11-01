package com.example.stagetrack.ui.pantallas

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.EstadoEquipo
import com.example.stagetrack.dominio.modelo.Evento
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.dominio.modelo.Usuario
import com.example.stagetrack.ui.componentes.DialogoVerReporteDano
import com.example.stagetrack.ui.viewmodel.EquipoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ModoVistaEvento(val titulo: String) {
    INVENTARIO("Catálogo Evento"),
    CHECKLIST_SALIDA("Checklist Salida (Despacho)"),
    CHECKLIST_ENTRADA("Checklist Entrada (Devolución)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInventarioEvento(
    evento: Evento,
    equipoViewModel: EquipoViewModel,
    usuarioActual: Usuario? = null,
    onVolverClick: () -> Unit,
    onTransferirCustodiaClick: (Equipo) -> Unit,
    onReportarDanoClick: (Equipo) -> Unit
) {
    val context = LocalContext.current
    val todosLosEquipos by equipoViewModel.listaEquipos.collectAsState()

    var modoVistaActual by remember { mutableStateOf(ModoVistaEvento.INVENTARIO) }
    var busquedaTexto by remember { mutableStateOf("") }
    var mostrarDialogoAsignar by remember { mutableStateOf(false) }
    var mostrarDialogoConfirmarCierre by remember { mutableStateOf(false) }
    var equipoParaVerQr by remember { mutableStateOf<Equipo?>(null) }
    var equipoParaVerReporteDano by remember { mutableStateOf<Equipo?>(null) }

    // Mapas de estado de verificación del Checklist
    val verificacionSalidaMap = remember { mutableStateMapOf<String, Boolean>() }
    val verificacionEntradaMap = remember { mutableStateMapOf<String, Boolean>() }

    val reportesDanoParaEquipo by (
        equipoParaVerReporteDano?.let { eq ->
            equipoViewModel.obtenerReportesDanoPorEquipo(eq.id).collectAsState(initial = emptyList())
        } ?: remember { mutableStateOf(emptyList()) }
    )

    val esSupervisor = usuarioActual?.rol == RolUsuario.SUPERVISOR || usuarioActual?.rol == RolUsuario.ADMINISTRADOR_GENERAL
    val esBodega = usuarioActual?.rol == RolUsuario.JEFE_BODEGA
    val puedeAsignarEquipos = esSupervisor || esBodega

    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val fechaTexto = remember(evento.fechaInicio, evento.fechaFin) {
        "${formatoFecha.format(Date(evento.fechaInicio))} - ${formatoFecha.format(Date(evento.fechaFin))}"
    }

    // Filtrar los equipos asignados al evento
    val equiposDelEvento = todosLosEquipos.filter { equipo ->
        val asignadoAUbicacion = equipo.ubicacionActual.equals(evento.ubicacion, ignoreCase = true) ||
                equipo.ubicacionActual.contains(evento.nombre, ignoreCase = true)
        val asignadoATecnico = equipo.idCustodioActual in evento.idsTecnicosAsignados

        val esDeEsteEvento = asignadoAUbicacion || asignadoATecnico ||
                equipo.idCustodioActual == evento.idSupervisor

        val coincideBusqueda = equipo.nombre.contains(busquedaTexto, ignoreCase = true) ||
                equipo.numeroSerie.contains(busquedaTexto, ignoreCase = true) ||
                equipo.categoria.contains(busquedaTexto, ignoreCase = true)

        esDeEsteEvento && coincideBusqueda
    }

    // Métricas de Checklist
    val totalEquipos = equiposDelEvento.size
    val totalSalidaVerificados = equiposDelEvento.count { verificacionSalidaMap[it.id] == true }
    val totalEntradaVerificados = equiposDelEvento.count { verificacionEntradaMap[it.id] == true }
    val progresoSalida = if (totalEquipos > 0) totalSalidaVerificados.toFloat() / totalEquipos.toFloat() else 0f
    val progresoEntrada = if (totalEquipos > 0) totalEntradaVerificados.toFloat() / totalEquipos.toFloat() else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Inventario y Checklist de Evento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = evento.nombre,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
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
            if (puedeAsignarEquipos && modoVistaActual == ModoVistaEvento.INVENTARIO) {
                FloatingActionButton(
                    onClick = { mostrarDialogoAsignar = true },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Asignar Equipo")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Asignar Equipo")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ficha del evento
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = evento.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(evento.estado.name) }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Recinto: ${evento.ubicacion}", style = MaterialTheme.typography.bodySmall)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = fechaTexto, style = MaterialTheme.typography.bodySmall)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Supervisor: ${evento.nombreSupervisor} | Cliente: ${evento.cliente}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Pestañas / Selector de Modo de Vista
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ModoVistaEvento.entries) { modo ->
                    FilterChip(
                        selected = modoVistaActual == modo,
                        onClick = { modoVistaActual = modo },
                        label = { Text(modo.titulo, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            val ic = when (modo) {
                                ModoVistaEvento.INVENTARIO -> Icons.Default.Event
                                ModoVistaEvento.CHECKLIST_SALIDA -> Icons.Default.LocalShipping
                                ModoVistaEvento.CHECKLIST_ENTRADA -> Icons.Default.FactCheck
                            }
                            Icon(imageVector = ic, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }

            // Sección de Progreso según modo de checklist
            when (modoVistaActual) {
                ModoVistaEvento.CHECKLIST_SALIDA -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Carga Despachada:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text("$totalSalidaVerificados / $totalEquipos (${(progresoSalida * 100).toInt()}%)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progresoSalida },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    equiposDelEvento.forEach { verificacionSalidaMap[it.id] = true }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Marcar Todos Verificados en Salida", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                ModoVistaEvento.CHECKLIST_ENTRADA -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Devolución Recepcionada:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text("$totalEntradaVerificados / $totalEquipos (${(progresoEntrada * 100).toInt()}%)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progresoEntrada },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        equiposDelEvento.forEach { verificacionEntradaMap[it.id] = true }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Marcar Todos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                }

                                if (esBodega || esSupervisor) {
                                    Button(
                                        onClick = { mostrarDialogoConfirmarCierre = true },
                                        enabled = totalEntradaVerificados > 0,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Devolver a Bodega", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {}
            }

            OutlinedTextField(
                value = busquedaTexto,
                onValueChange = { busquedaTexto = it },
                label = { Text("Buscar equipos por nombre/serie/categoría") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (equiposDelEvento.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hay equipos asignados a este evento.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(equiposDelEvento, key = { it.id }) { equipo ->
                        when (modoVistaActual) {
                            ModoVistaEvento.INVENTARIO -> {
                                TarjetaEquipoItem(
                                    equipo = equipo,
                                    puedeGenerarQr = esBodega,
                                    onVerQrClick = if (esBodega) { { equipoParaVerQr = equipo } } else null,
                                    onTransferirCustodiaClick = { onTransferirCustodiaClick(equipo) },
                                    onReportarDanoClick = { onReportarDanoClick(equipo) },
                                    onVerReporteDanoClick = { equipoParaVerReporteDano = equipo }
                                )
                            }

                            ModoVistaEvento.CHECKLIST_SALIDA -> {
                                val estaVerificado = verificacionSalidaMap[equipo.id] == true
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (estaVerificado) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = estaVerificado,
                                            onCheckedChange = { chk -> verificacionSalidaMap[equipo.id] = chk }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(equipo.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            Text("${equipo.categoria} | N/S: ${equipo.numeroSerie}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        AssistChip(
                                            onClick = { verificacionSalidaMap[equipo.id] = !estaVerificado },
                                            label = { Text(if (estaVerificado) "Despachado ✅" else "Pendiente ⏳") }
                                        )
                                    }
                                }
                            }

                            ModoVistaEvento.CHECKLIST_ENTRADA -> {
                                val estaDevuelto = verificacionEntradaMap[equipo.id] == true
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (estaDevuelto) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = estaDevuelto,
                                                onCheckedChange = { chk -> verificacionEntradaMap[equipo.id] = chk }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(equipo.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                Text("${equipo.categoria} | N/S: ${equipo.numeroSerie}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            AssistChip(
                                                onClick = { verificacionEntradaMap[equipo.id] = !estaDevuelto },
                                                label = { Text(if (estaDevuelto) "Devuelto OK ✅" else "Por Recepcionar ⏳") }
                                            )
                                        }

                                        if (equipo.estadoActual == EstadoEquipo.REPORTADO_DANADO) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedButton(
                                                onClick = { equipoParaVerReporteDano = equipo },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Ver Reporte de Daño Registrado", style = MaterialTheme.typography.labelSmall)
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedButton(
                                                onClick = { onReportarDanoClick(equipo) },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Reportar Daño al Recepcionar", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        equipoParaVerReporteDano?.let { equipoDanado ->
            DialogoVerReporteDano(
                equipo = equipoDanado,
                reportes = reportesDanoParaEquipo,
                onDismiss = { equipoParaVerReporteDano = null }
            )
        }

        equipoParaVerQr?.let { equipo ->
            DialogoVerQrEquipo(
                equipo = equipo,
                onDescartar = { equipoParaVerQr = null }
            )
        }

        if (mostrarDialogoConfirmarCierre) {
            val idsParaDevolver = equiposDelEvento.filter { verificacionEntradaMap[it.id] == true }.map { it.id }
            AlertDialog(
                onDismissRequest = { mostrarDialogoConfirmarCierre = false },
                title = { Text("Confirmar Cierre y Devolución a Bodega") },
                text = {
                    Text(
                        "¿Confirmas que deseas restablecer los ${idsParaDevolver.size} equipos verificados al stock disponible en 'Bodega Principal'?"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            equipoViewModel.devolverEquiposDeEventoABodega(idsParaDevolver) { res ->
                                if (res is Resultado.Exito) {
                                    Toast.makeText(context, "${idsParaDevolver.size} equipos devueltos a Bodega Principal", Toast.LENGTH_LONG).show()
                                }
                            }
                            mostrarDialogoConfirmarCierre = false
                        }
                    ) {
                        Text("Sí, Devolver a Bodega")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoConfirmarCierre = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (mostrarDialogoAsignar) {
            DialogoAsignarEquiposAEvento(
                evento = evento,
                equiposDisponibles = todosLosEquipos.filter {
                    !equiposDelEvento.contains(it)
                },
                onDismiss = { mostrarDialogoAsignar = false },
                onAsignar = { equiposSeleccionados ->
                    equiposSeleccionados.forEach { equipo ->
                        equipoViewModel.actualizarEquipoExistente(
                            equipoActualizado = equipo.copy(
                                ubicacionActual = evento.ubicacion
                            ),
                            onResultado = {}
                        )
                    }
                    mostrarDialogoAsignar = false
                }
            )
        }
    }
}

@Composable
fun DialogoAsignarEquiposAEvento(
    evento: Evento,
    equiposDisponibles: List<Equipo>,
    onDismiss: () -> Unit,
    onAsignar: (List<Equipo>) -> Unit
) {
    val seleccionados = remember { mutableStateListOf<Equipo>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asignar Equipos al Evento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Selecciona de la bodega general los equipos a desplegar en '${evento.nombre}':",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (equiposDisponibles.isEmpty()) {
                    Text(
                        text = "Todos los equipos del inventario ya están asignados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        items(equiposDisponibles, key = { it.id }) { equipo ->
                            val estaSeleccionado = seleccionados.contains(equipo)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = estaSeleccionado,
                                    onCheckedChange = { check ->
                                        if (check == true) seleccionados.add(equipo)
                                        else seleccionados.remove(equipo)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = equipo.nombre,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${equipo.categoria} | N/S: ${equipo.numeroSerie} | Ubicación: ${equipo.ubicacionActual}",
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
                enabled = seleccionados.isNotEmpty(),
                onClick = { onAsignar(seleccionados.toList()) }
            ) {
                Text("Asignar (${seleccionados.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
