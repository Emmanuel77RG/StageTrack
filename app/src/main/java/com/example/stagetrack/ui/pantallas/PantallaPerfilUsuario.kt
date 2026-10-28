package com.example.stagetrack.ui.pantallas

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.dominio.modelo.Usuario
import com.example.stagetrack.ui.componentes.AvatarUsuario
import com.example.stagetrack.ui.componentes.DialogoAjustarImagen
import kotlinx.coroutines.launch

/**
 * Pantalla de Perfil de Usuario para que el usuario en sesión pueda ajustar su fotografía,
 * registrar su correo de recuperación secundario y cambiar su contraseña de manera segura.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfilUsuario(
    usuario: Usuario,
    onVolverClick: () -> Unit,
    onSubirFotoPerfil: (
        uri: Uri,
        rotacion: Float,
        escala: Float,
        offsetX: Float,
        offsetY: Float,
        tamanoVistaPx: Float,
        onResultado: (Resultado<String>) -> Unit
    ) -> Unit,
    onGuardarCorreoRecuperacion: (correoRecuperacion: String?, onResultado: (Resultado<Unit>) -> Unit) -> Unit,
    onCambiarContrasena: (claveActual: String, nuevaClave: String, onResultado: (Resultado<Unit>) -> Unit) -> Unit
) {
    var uriImagenSeleccionada by remember { mutableStateOf<Uri?>(null) }
    var mostrandoAjusteImagen by remember { mutableStateOf(false) }

    var correoRecuperacion by remember { mutableStateOf(usuario.correoRecuperacion ?: "") }
    var guardandoCorreo by remember { mutableStateOf(false) }

    var claveActual by remember { mutableStateOf("") }
    var nuevaClave by remember { mutableStateOf("") }
    var confirmarClave by remember { mutableStateOf("") }
    var cambiandoClave by remember { mutableStateOf(false) }

    var cargandoFoto by remember { mutableStateOf(false) }
    var mensajeAlerta by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uriImagenSeleccionada = uri
            mostrandoAjusteImagen = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolverClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado de Avatar interactivo
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .size(120.dp)
                    .clickable { launcherGaleria.launch("image/*") }
            ) {
                AvatarUsuario(
                    urlFotografia = usuario.urlFotografia,
                    nombreUsuario = usuario.nombre,
                    tamano = 120.dp
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    if (cargandoFoto) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Cambiar Foto",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = usuario.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = usuario.correo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            AssistChip(
                onClick = {},
                label = { Text(etiquetaRol(usuario.rol)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta 1: Correo de Recuperación Personal
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Correo de Recuperación Personal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Registra una dirección de correo real alternativa para recuperar el acceso a tu cuenta de forma segura.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = correoRecuperacion,
                        onValueChange = { correoRecuperacion = it },
                        label = { Text("Correo Personal de Recuperación") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            guardandoCorreo = true
                            onGuardarCorreoRecuperacion(correoRecuperacion.ifBlank { null }) { res ->
                                guardandoCorreo = false
                                when (res) {
                                    is Resultado.Exito -> {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Correo de recuperación guardado correctamente.")
                                        }
                                    }
                                    is Resultado.Error -> {
                                        mensajeAlerta = res.mensaje ?: res.excepcion.localizedMessage
                                    }
                                    else -> {}
                                }
                            }
                        },
                        enabled = !guardandoCorreo,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar Correo")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta 2: Cambio Seguro de Contraseña
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cambiar Contraseña",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = claveActual,
                        onValueChange = { claveActual = it },
                        label = { Text("Contraseña Actual") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nuevaClave,
                        onValueChange = { nuevaClave = it },
                        label = { Text("Nueva Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmarClave,
                        onValueChange = { confirmarClave = it },
                        label = { Text("Confirmar Nueva Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (claveActual.isBlank()) {
                                mensajeAlerta = "Debe ingresar su contraseña actual por seguridad."
                                return@Button
                            }
                            if (nuevaClave.length < 6) {
                                mensajeAlerta = "La nueva contraseña debe tener al menos 6 caracteres."
                                return@Button
                            }
                            if (nuevaClave != confirmarClave) {
                                mensajeAlerta = "La nueva contraseña y su confirmación no coinciden."
                                return@Button
                            }

                            cambiandoClave = true
                            onCambiarContrasena(claveActual, nuevaClave) { res ->
                                cambiandoClave = false
                                when (res) {
                                    is Resultado.Exito -> {
                                        claveActual = ""
                                        nuevaClave = ""
                                        confirmarClave = ""
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Contraseña actualizada exitosamente.")
                                        }
                                    }
                                    is Resultado.Error -> {
                                        mensajeAlerta = res.mensaje ?: res.excepcion.localizedMessage
                                    }
                                    else -> {}
                                }
                            }
                        },
                        enabled = !cambiandoClave,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Actualizar Contraseña")
                    }
                }
            }
        }
    }

    // Diálogo interactivo con gestos multitáctiles para ajustar la foto de perfil
    if (mostrandoAjusteImagen && uriImagenSeleccionada != null) {
        DialogoAjustarImagen(
            uriImagen = uriImagenSeleccionada!!,
            onDismiss = { mostrandoAjusteImagen = false },
            onConfirmar = { rot, esc, offX, offY, tVista ->
                mostrandoAjusteImagen = false
                cargandoFoto = true
                uriImagenSeleccionada?.let { uri ->
                    onSubirFotoPerfil(uri, rot, esc, offX, offY, tVista) { res ->
                        cargandoFoto = false
                        if (res is Resultado.Error) {
                            mensajeAlerta = res.mensaje ?: res.excepcion.localizedMessage
                        } else if (res is Resultado.Exito) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Foto de perfil actualizada en Supabase.")
                            }
                        }
                    }
                }
            }
        )
    }

    // Diálogo emergente para alertas de validación o error
    mensajeAlerta?.let { error ->
        AlertDialog(
            onDismissRequest = { mensajeAlerta = null },
            confirmButton = {
                TextButton(onClick = { mensajeAlerta = null }) {
                    Text("Aceptar")
                }
            },
            title = { Text("Aviso de Perfil") },
            text = { Text(error) }
        )
    }
}

private fun etiquetaRol(rol: RolUsuario): String {
    return when (rol) {
        RolUsuario.ADMINISTRADOR_GENERAL -> "Administrador General"
        RolUsuario.JEFE_BODEGA -> "Jefe de Bodega"
        RolUsuario.TECNICO -> "Técnico de Producción"
        RolUsuario.SUPERVISOR -> "Supervisor"
    }
}
