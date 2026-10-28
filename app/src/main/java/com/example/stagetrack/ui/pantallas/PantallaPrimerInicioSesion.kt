package com.example.stagetrack.ui.pantallas

import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.stagetrack.dominio.modelo.Usuario
import com.example.stagetrack.ui.componentes.AvatarUsuario
import com.example.stagetrack.ui.componentes.DialogoAjustarImagen
import kotlinx.coroutines.launch

/**
 * Pantalla obligatoria de bienvenida y configuración de Primer Inicio de Sesión.
 * Impide la navegación al Dashboard hasta que el usuario personalice su contraseña,
 * verifique su correo personal de recuperación y ajuste su foto de perfil.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrimerInicioSesion(
    usuario: Usuario,
    onEnviarVerificacionCorreo: (correoRecuperacion: String, onResultado: (Resultado<Unit>) -> Unit) -> Unit,
    onCompletarPrimerInicio: (
        claveTemporal: String,
        nuevaClave: String,
        correoRecuperacion: String,
        uriFotoLocal: Uri?,
        rotacionFoto: Float,
        escalaFoto: Float,
        offsetXFoto: Float,
        offsetYFoto: Float,
        tamanoVistaPxFoto: Float,
        onResultado: (Resultado<Unit>) -> Unit
    ) -> Unit,
    onCerrarSesion: () -> Unit
) {
    var claveTemporal by remember { mutableStateOf("") }
    var nuevaClave by remember { mutableStateOf("") }
    var confirmarClave by remember { mutableStateOf("") }

    var correoRecuperacion by remember { mutableStateOf("") }
    var enviandoVerificacion by remember { mutableStateOf(false) }
    var correoVerificacionEnviado by remember { mutableStateOf(false) }

    var uriFotoLocal by remember { mutableStateOf<Uri?>(null) }
    var rotacionFoto by remember { mutableFloatStateOf(0f) }
    var escalaFoto by remember { mutableFloatStateOf(1.0f) }
    var offsetXFoto by remember { mutableFloatStateOf(0f) }
    var offsetYFoto by remember { mutableFloatStateOf(0f) }
    var tamanoVistaPxFoto by remember { mutableFloatStateOf(0f) }
    var mostrandoAjusteImagen by remember { mutableStateOf(false) }

    var cargandoProceso by remember { mutableStateOf(false) }
    var mensajeAlerta by remember { mutableStateOf<String?>(null) }
    var mostrandoConfirmarSalida by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Interceptación de retroceso para proteger la sesión
    BackHandler {
        mostrandoConfirmarSalida = true
    }

    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uriFotoLocal = uri
            mostrandoAjusteImagen = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("¡Bienvenido a StageTrack!", fontWeight = FontWeight.Bold)
                        Text(
                            "Configuración de Primer Inicio de Sesión",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
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
            // Sección de Foto de Perfil
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .size(110.dp)
                    .clickable { launcherGaleria.launch("image/*") }
            ) {
                AvatarUsuario(
                    urlFotografia = uriFotoLocal?.toString() ?: usuario.urlFotografia,
                    nombreUsuario = usuario.nombre,
                    tamano = 110.dp
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Ajustar Foto",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Hola, ${usuario.nombre}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Cuenta: ${usuario.correo}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta 1: Actualización de Contraseña
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LockReset, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1. Definir Nueva Contraseña", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = claveTemporal,
                        onValueChange = { claveTemporal = it },
                        label = { Text("Contraseña Temporal Asignada") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nuevaClave,
                        onValueChange = { nuevaClave = it },
                        label = { Text("Nueva Contraseña Definitiva") },
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
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta 2: Correo Personal de Recuperación y Verificación Firebase
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("2. Correo de Recuperación Real", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Ingresa tu correo personal para verificarlo mediante Firebase Auth y respaldar tu cuenta.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = correoRecuperacion,
                        onValueChange = { correoRecuperacion = it },
                        label = { Text("Correo Personal de Recuperación") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            if (correoRecuperacion.isBlank() || !correoRecuperacion.contains("@")) {
                                mensajeAlerta = "Ingresa una dirección de correo válida para verificar."
                                return@OutlinedButton
                            }
                            enviandoVerificacion = true
                            onEnviarVerificacionCorreo(correoRecuperacion) { res ->
                                enviandoVerificacion = false
                                when (res) {
                                    is Resultado.Exito -> {
                                        correoVerificacionEnviado = true
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Mensaje/PIN de verificación enviado mediante Firebase Auth.")
                                        }
                                    }
                                    is Resultado.Error -> {
                                        mensajeAlerta = res.mensaje ?: res.excepcion.localizedMessage
                                    }
                                    else -> {}
                                }
                            }
                        },
                        enabled = !enviandoVerificacion,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        if (enviandoVerificacion) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                        } else if (correoVerificacionEnviado) {
                            Icon(imageVector = Icons.Default.MarkEmailRead, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(if (correoVerificacionEnviado) "Verificación Enviada" else "Enviar Verificación Firebase")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de acción principal
            Button(
                onClick = {
                    if (claveTemporal.isBlank()) {
                        mensajeAlerta = "Por favor ingresa la contraseña temporal asignada por el administrador."
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
                    if (nuevaClave == claveTemporal) {
                        mensajeAlerta = "La nueva contraseña debe ser diferente a la clave temporal asignada."
                        return@Button
                    }
                    if (correoRecuperacion.isBlank() || !correoRecuperacion.contains("@")) {
                        mensajeAlerta = "Ingresa tu correo personal de recuperación."
                        return@Button
                    }

                    cargandoProceso = true
                    onCompletarPrimerInicio(
                        claveTemporal,
                        nuevaClave,
                        correoRecuperacion,
                        uriFotoLocal,
                        rotacionFoto,
                        escalaFoto,
                        offsetXFoto,
                        offsetYFoto,
                        tamanoVistaPxFoto
                    ) { res ->
                        cargandoProceso = false
                        if (res is Resultado.Error) {
                            mensajeAlerta = res.mensaje ?: res.excepcion.localizedMessage
                        }
                    }
                },
                enabled = !cargandoProceso,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (cargandoProceso) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Completar Perfil e Ingresar", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Diálogo interactivo con gestos para ajustar la foto de perfil
    if (mostrandoAjusteImagen && uriFotoLocal != null) {
        DialogoAjustarImagen(
            uriImagen = uriFotoLocal!!,
            onDismiss = { mostrandoAjusteImagen = false },
            onConfirmar = { rot, esc, offX, offY, tVista ->
                rotacionFoto = rot
                escalaFoto = esc
                offsetXFoto = offX
                offsetYFoto = offY
                tamanoVistaPxFoto = tVista
                mostrandoAjusteImagen = false
            }
        )
    }

    // Diálogo de confirmación de salida al intentar presionar "Atrás"
    if (mostrandoConfirmarSalida) {
        AlertDialog(
            onDismissRequest = { mostrandoConfirmarSalida = false },
            title = { Text("Configuración de Perfil Pendiente") },
            text = { Text("Debes completar la configuración inicial para usar StageTrack. Si sales ahora se cerrará tu sesión. ¿Deseas salir?") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrandoConfirmarSalida = false
                        onCerrarSesion()
                    }
                ) {
                    Text("Cerrar Sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrandoConfirmarSalida = false }) {
                    Text("Continuar Configuración")
                }
            }
        )
    }

    // Alertas de validación o error
    mensajeAlerta?.let { err ->
        AlertDialog(
            onDismissRequest = { mensajeAlerta = null },
            confirmButton = {
                TextButton(onClick = { mensajeAlerta = null }) {
                    Text("Aceptar")
                }
            },
            title = { Text("Configuración Requerida") },
            text = { Text(err) }
        )
    }
}
