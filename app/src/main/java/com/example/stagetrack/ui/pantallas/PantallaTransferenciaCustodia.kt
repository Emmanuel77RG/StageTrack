package com.example.stagetrack.ui.pantallas

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import java.io.File
import com.example.stagetrack.core.imagen.UtilidadImagen
import com.example.stagetrack.core.imagen.recordarBitmapEquipo
import com.example.stagetrack.core.ubicacion.UtilidadUbicacionGps
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.ui.componentes.InsigniaSha256
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.ui.componentes.CameraPreview
import com.example.stagetrack.ui.viewmodel.AutenticacionViewModel
import com.example.stagetrack.ui.viewmodel.EquipoViewModel
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTransferenciaCustodia(
    equipo: Equipo,
    autenticacionViewModel: AutenticacionViewModel,
    equipoViewModel: EquipoViewModel,
    onVolverClick: () -> Unit,
    onTransferenciaExitosa: () -> Unit
) {
    val usuarioEmisor by autenticacionViewModel.usuarioActual.collectAsState()
    val listaUsuarios by autenticacionViewModel.listaUsuarios.collectAsState()
    val context = LocalContext.current

    var idReceptorSeleccionado by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
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
                    title = { Text("Transferencia de Custodia") },
                    navigationIcon = {
                        IconButton(onClick = onVolverClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Equipo a Transferir:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val bitmapEquipo = recordarBitmapEquipo(context, equipo.urlImagen)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
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
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = equipo.nombre,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "N/S: ${equipo.numeroSerie} | QR: ${equipo.codigoQR}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Emisor Actual: ${usuarioEmisor?.nombre ?: "Sin sesión"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Seleccionar Receptor (Nuevo Custodio):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Opción simplificada de Devolución a Bodega
                    val esBodegaSeleccionada = idReceptorSeleccionado == "bodega"
                    Card(
                        onClick = { idReceptorSeleccionado = "bodega" },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (esBodegaSeleccionada)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Devolver a Bodega Principal",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (esBodegaSeleccionada) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Iniciar proceso de retorno de equipo a inventario",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (esBodegaSeleccionada) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (esBodegaSeleccionada) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Receptores individuales (excluyendo encargados de bodega, administradores generales y supervisores)
                    val receptoresValidos = listaUsuarios.filter {
                        it.id != usuarioEmisor?.id &&
                        it.activo &&
                        it.rol != RolUsuario.JEFE_BODEGA &&
                        it.rol != RolUsuario.ADMINISTRADOR_GENERAL &&
                        it.rol != RolUsuario.SUPERVISOR
                    }
                    receptoresValidos.forEach { receptor ->
                        val seleccionado = idReceptorSeleccionado == receptor.id
                        Card(
                            onClick = { idReceptorSeleccionado = receptor.id },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (seleccionado)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = receptor.nombre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = receptor.correo,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (seleccionado) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas u observaciones de entrega (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Text(
                    text = "Evidencia Fotográfica Obligatoria (CameraX + SHA-256):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (fotoBitmap != null && hashSha256Foto != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Image(
                                bitmap = fotoBitmap!!.asImageBitmap(),
                                contentDescription = "Foto evidencia",
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
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (fotoBitmap == null) "Tomar Foto de Evidencia" else "Cambiar Foto")
                }

                if (mensajeError != null) {
                    Text(
                        text = mensajeError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    enabled = !cargando && idReceptorSeleccionado.isNotBlank() && bytesFoto != null && usuarioEmisor != null,
                    onClick = {
                        cargando = true
                        mensajeError = null
                        val coords = UtilidadUbicacionGps.obtenerCoordenadasGpsActuales(context)
                        val lat = coords?.latitud
                        val lng = coords?.longitud

                        if (idReceptorSeleccionado == "bodega") {
                            equipoViewModel.iniciarDevolucionABodega(
                                idEquipo = equipo.id,
                                idTecnicoEmisor = usuarioEmisor!!.id,
                                nombreTecnicoEmisor = usuarioEmisor!!.nombre,
                                bytesFotografia = bytesFoto!!,
                                latitudGps = lat,
                                longitudGps = lng,
                                notas = notas.ifBlank { null }
                            ) { resultado ->
                                cargando = false
                                when (resultado) {
                                    is Resultado.Exito -> onTransferenciaExitosa()
                                    is Resultado.Error -> mensajeError = resultado.excepcion.message
                                    is Resultado.Cargando -> {}
                                }
                            }
                        } else {
                            val receptorObj = listaUsuarios.find { it.id == idReceptorSeleccionado }
                            val nombreReceptor = receptorObj?.nombre ?: "Usuario"
                            equipoViewModel.realizarTransferenciaCustodia(
                                idEquipo = equipo.id,
                                idEmisor = usuarioEmisor!!.id,
                                idReceptor = idReceptorSeleccionado,
                                nombreReceptor = nombreReceptor,
                                bytesFotografia = bytesFoto!!,
                                notas = notas.ifBlank { null }
                            ) { resultado ->
                                cargando = false
                                when (resultado) {
                                    is Resultado.Exito -> onTransferenciaExitosa()
                                    is Resultado.Error -> mensajeError = resultado.excepcion.message
                                    is Resultado.Cargando -> {}
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (cargando) "Procesando Custodia..."
                        else if (idReceptorSeleccionado == "bodega") "Iniciar Devolución a Bodega"
                        else "Confirmar Transferencia de Custodia"
                    )
                }
            }
        }
    }
}
