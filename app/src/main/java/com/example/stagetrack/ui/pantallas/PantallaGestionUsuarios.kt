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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.dominio.modelo.Usuario
import com.example.stagetrack.ui.componentes.AvatarUsuario
import com.example.stagetrack.ui.componentes.DialogoAjustarImagen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de Gestión de Usuarios exclusiva para el Administrador General.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionUsuarios(
    usuarios: List<Usuario>,
    usuarioActualId: String,
    onVolverClick: () -> Unit,
    onCrearUsuario: (nombre: String, correo: String, clave: String, rol: RolUsuario, onResultado: (Resultado<Usuario>) -> Unit) -> Unit,
    onEditarUsuario: (idUsuario: String, nombre: String, correo: String, rol: RolUsuario, onResultado: (Resultado<Unit>) -> Unit) -> Unit = { _, _, _, _, _ -> },
    onRestablecerContrasena: (idUsuario: String, nuevaClave: String, onResultado: (Resultado<Unit>) -> Unit) -> Unit,
    onCambiarEstadoActivo: (idUsuario: String, activo: Boolean, onResultado: (Resultado<Unit>) -> Unit) -> Unit,
    onSubirFotoPerfil: (

        idUsuario: String,
        uri: Uri,
        rotacion: Float,
        escala: Float,
        offsetX: Float,
        offsetY: Float,
        tamanoVistaPx: Float,
        onResultado: (Resultado<String>) -> Unit
    ) -> Unit,
    onSincronizarNube: ((Resultado<Int>) -> Unit) -> Unit = { _ -> }
) {
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var usuarioParaEditar by remember { mutableStateOf<Usuario?>(null) }
    var usuarioParaRestablecer by remember { mutableStateOf<Usuario?>(null) }
    var usuarioCreadoExitoso by remember { mutableStateOf<Pair<Usuario, String>?>(null) }
    var mensajeSnack by remember { mutableStateOf<String?>(null) }
    var cargandoSubidaFotoId by remember { mutableStateOf<String?>(null) }
    var uriAjustarImagen by remember { mutableStateOf<Uri?>(null) }
    var idUsuarioAjustandoFoto by remember { mutableStateOf<String?>(null) }
    var cargandoSincronizacion by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Gestión de Usuarios",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Panel del Administrador General",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolverClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            cargandoSincronizacion = true
                            onSincronizarNube { res ->
                                cargandoSincronizacion = false
                                when (res) {
                                    is Resultado.Exito -> {
                                        mensajeSnack = if (res.datos > 0)
                                            "Se sincronizaron ${res.datos} nuevo(s) usuario(s) a la nube correctamente. Los usuarios existentes se mantuvieron intactos."
                                        else
                                            "Todos los usuarios locales ya estaban sincronizados en la nube."
                                    }
                                    is Resultado.Error -> {
                                        mensajeSnack = res.mensaje ?: res.excepcion.localizedMessage
                                    }
                                    is Resultado.Cargando -> {}
                                }
                            }
                        },
                        enabled = !cargandoSincronizacion
                    ) {
                        if (cargandoSincronizacion) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Sincronizar Usuarios a la Nube"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoCrear = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Alta")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Alta de Usuario")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (usuarios.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay usuarios registrados.",
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
                    items(usuarios, key = { it.id }) { usuario ->
                        TarjetaUsuarioItem(
                            usuario = usuario,
                            esUsuarioPropio = (usuario.id == usuarioActualId),
                            cargandoFoto = cargandoSubidaFotoId == usuario.id,
                            onEditarClick = { usuarioParaEditar = usuario },
                            onRestablecerContrasenaClick = { usuarioParaRestablecer = usuario },
                            onCambiarEstadoActivo = { activo ->
                                onCambiarEstadoActivo(usuario.id, activo) { res ->
                                    if (res is Resultado.Error) {
                                        mensajeSnack = res.mensaje ?: res.excepcion.localizedMessage
                                    }
                                }
                            },
                            onFotoSeleccionada = { uri ->
                                idUsuarioAjustandoFoto = usuario.id
                                uriAjustarImagen = uri
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Espacio para el FAB
                    }
                }
            }

            // Diálogo de error temporal si aplica
            mensajeSnack?.let { error ->
                AlertDialog(
                    onDismissRequest = { mensajeSnack = null },
                    confirmButton = {
                        TextButton(onClick = { mensajeSnack = null }) {
                            Text("Aceptar")
                        }
                    },
                    title = { Text("Aviso de Administración") },
                    text = { Text(error) }
                )
            }
        }
    }

    if (mostrarDialogoCrear) {
        DialogoCrearUsuario(
            onDismiss = { mostrarDialogoCrear = false },
            onConfirmar = { nombre, correo, clave, rol, onResultado ->
                onCrearUsuario(nombre, correo, clave, rol) { resultado ->
                    if (resultado is Resultado.Exito) {
                        mostrarDialogoCrear = false
                        usuarioCreadoExitoso = Pair(resultado.datos, clave)
                    }
                    onResultado(resultado)
                }
            }
        )
    }

    usuarioCreadoExitoso?.let { (usr, claveTemp) ->
        DialogoCredencialesCreadas(
            usuario = usr,
            claveTemporal = claveTemp,
            onDismiss = { usuarioCreadoExitoso = null }
        )
    }

    usuarioParaRestablecer?.let { usuario ->
        DialogoRestablecerContrasena(
            usuario = usuario,
            onDismiss = { usuarioParaRestablecer = null },
            onConfirmar = { nuevaClave, onResultado ->
                onRestablecerContrasena(usuario.id, nuevaClave) { resultado ->
                    if (resultado is Resultado.Exito) {
                        usuarioParaRestablecer = null
                    }
                    onResultado(resultado)
                }
            }
        )
    }

    usuarioParaEditar?.let { usuario ->
        DialogoEditarUsuario(
            usuario = usuario,
            onDismiss = { usuarioParaEditar = null },
            onConfirmar = { nombre, correo, rol, onResultado ->
                onEditarUsuario(usuario.id, nombre, correo, rol) { resultado ->
                    if (resultado is Resultado.Exito) {
                        usuarioParaEditar = null
                    }
                    onResultado(resultado)
                }
            }
        )
    }


    if (uriAjustarImagen != null && idUsuarioAjustandoFoto != null) {
        DialogoAjustarImagen(
            uriImagen = uriAjustarImagen!!,
            onDismiss = {
                uriAjustarImagen = null
                idUsuarioAjustandoFoto = null
            },
            onConfirmar = { rot, esc, offX, offY, tVista ->
                val idTarget = idUsuarioAjustandoFoto!!
                val uriTarget = uriAjustarImagen!!
                uriAjustarImagen = null
                idUsuarioAjustandoFoto = null
                cargandoSubidaFotoId = idTarget
                onSubirFotoPerfil(idTarget, uriTarget, rot, esc, offX, offY, tVista) { res ->
                    cargandoSubidaFotoId = null
                    if (res is Resultado.Error) {
                        mensajeSnack = res.mensaje ?: res.excepcion.localizedMessage
                    }
                }
            }
        )
    }
}

@Composable
private fun TarjetaUsuarioItem(
    usuario: Usuario,
    esUsuarioPropio: Boolean,
    cargandoFoto: Boolean,
    onEditarClick: () -> Unit,
    onRestablecerContrasenaClick: () -> Unit,
    onCambiarEstadoActivo: (Boolean) -> Unit,
    onFotoSeleccionada: (Uri) -> Unit
) {
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val fechaTexto = remember(usuario.fechaCreacion) {
        formatoFecha.format(Date(usuario.fechaCreacion))
    }

    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onFotoSeleccionada(it) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (usuario.activo) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.then(
                        if (esUsuarioPropio) {
                            Modifier.clickable { launcherGaleria.launch("image/*") }
                        } else {
                            Modifier
                        }
                    )
                ) {
                    AvatarUsuario(
                        urlFotografia = usuario.urlFotografia,
                        nombreUsuario = usuario.nombre,
                        tamano = 48.dp
                    )
                    if (esUsuarioPropio) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Cambiar mi foto",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = usuario.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = usuario.correo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = usuario.activo,
                    onCheckedChange = onCambiarEstadoActivo
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(etiquetaRol(usuario.rol)) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = colorRolContainer(usuario.rol)
                    )
                )

                Text(
                    text = "Alta: $fechaTexto",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEditarClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Editar", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onRestablecerContrasenaClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clave", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoEditarUsuario(
    usuario: Usuario,
    onDismiss: () -> Unit,
    onConfirmar: (nombre: String, correo: String, rol: RolUsuario, onResultado: (Resultado<Unit>) -> Unit) -> Unit
) {
    var nombre by remember { mutableStateOf(usuario.nombre) }
    var correo by remember { mutableStateOf(usuario.correo) }
    var rolSeleccionado by remember { mutableStateOf(usuario.rol) }
    var menuRolExpandido by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Usuario") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; errorMensaje = null },
                    label = { Text("Nombre Completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it; errorMensaje = null },
                    label = { Text("Correo Electrónico") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = menuRolExpandido,
                    onExpandedChange = { menuRolExpandido = !menuRolExpandido }
                ) {
                    OutlinedTextField(
                        value = etiquetaRol(rolSeleccionado),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol Asignado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuRolExpandido) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = menuRolExpandido,
                        onDismissRequest = { menuRolExpandido = false }
                    ) {
                        RolUsuario.entries.forEach { rol ->
                            DropdownMenuItem(
                                text = { Text(etiquetaRol(rol)) },
                                onClick = {
                                    rolSeleccionado = rol
                                    menuRolExpandido = false
                                }
                            )
                        }
                    }
                }

                errorMensaje?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isBlank() || correo.isBlank()) {
                        errorMensaje = "El nombre y el correo no pueden estar vacíos"
                        return@Button
                    }
                    cargando = true
                    onConfirmar(nombre, correo, rolSeleccionado) { res ->
                        cargando = false
                        if (res is Resultado.Error) {
                            errorMensaje = res.mensaje ?: res.excepcion.localizedMessage
                        }
                    }
                },
                enabled = !cargando
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoCrearUsuario(
    onDismiss: () -> Unit,
    onConfirmar: (nombre: String, correo: String, clave: String, rol: RolUsuario, onResultado: (Resultado<Usuario>) -> Unit) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.TECNICO) }
    var menuRolExpandido by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dar de Alta Nuevo Usuario") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; errorMensaje = null },
                    label = { Text("Nombre Completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it; errorMensaje = null },
                    label = { Text("Correo Electrónico") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = clave,
                    onValueChange = { clave = it; errorMensaje = null },
                    label = { Text("Contraseña Inicial") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = menuRolExpandido,
                    onExpandedChange = { menuRolExpandido = !menuRolExpandido }
                ) {
                    OutlinedTextField(
                        value = etiquetaRol(rolSeleccionado),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol Asignado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuRolExpandido) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = menuRolExpandido,
                        onDismissRequest = { menuRolExpandido = false }
                    ) {
                        RolUsuario.entries.forEach { rol ->
                            DropdownMenuItem(
                                text = { Text(etiquetaRol(rol)) },
                                onClick = {
                                    rolSeleccionado = rol
                                    menuRolExpandido = false
                                }
                            )
                        }
                    }
                }

                errorMensaje?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isBlank() || correo.isBlank() || clave.isBlank()) {
                        errorMensaje = "Todos los campos son obligatorios"
                        return@Button
                    }
                    cargando = true
                    onConfirmar(nombre, correo, clave, rolSeleccionado) { res ->
                        cargando = false
                        if (res is Resultado.Error) {
                            errorMensaje = res.mensaje ?: res.excepcion.localizedMessage
                        }
                    }
                },
                enabled = !cargando
            ) {
                Text("Guardar y Dar de Alta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun DialogoRestablecerContrasena(
    usuario: Usuario,
    onDismiss: () -> Unit,
    onConfirmar: (nuevaClave: String, onResultado: (Resultado<Unit>) -> Unit) -> Unit
) {
    var nuevaClave by remember { mutableStateOf("") }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restablecer Contraseña") },
        text = {
            Column {
                Text(
                    text = "Usuario: ${usuario.nombre} (${usuario.correo})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = nuevaClave,
                    onValueChange = { nuevaClave = it; errorMensaje = null },
                    label = { Text("Nueva Contraseña") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMensaje?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nuevaClave.isBlank()) {
                        errorMensaje = "Ingrese la nueva contraseña"
                        return@Button
                    }
                    cargando = true
                    onConfirmar(nuevaClave) { res ->
                        cargando = false
                        if (res is Resultado.Error) {
                            errorMensaje = res.mensaje ?: res.excepcion.localizedMessage
                        }
                    }
                },
                enabled = !cargando
            ) {
                Text("Cambiar Contraseña")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun etiquetaRol(rol: RolUsuario): String {
    return when (rol) {
        RolUsuario.ADMINISTRADOR_GENERAL -> "Administrador General"
        RolUsuario.JEFE_BODEGA -> "Jefe de Bodega"
        RolUsuario.TECNICO -> "Técnico de Producción"
        RolUsuario.SUPERVISOR -> "Supervisor"
    }
}

@Composable
private fun colorRolContainer(rol: RolUsuario) = when (rol) {
    RolUsuario.ADMINISTRADOR_GENERAL -> MaterialTheme.colorScheme.primaryContainer
    RolUsuario.JEFE_BODEGA -> MaterialTheme.colorScheme.secondaryContainer
    RolUsuario.TECNICO -> MaterialTheme.colorScheme.tertiaryContainer
    RolUsuario.SUPERVISOR -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
private fun DialogoCredencialesCreadas(
    usuario: Usuario,
    claveTemporal: String,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copiado by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Usuario Dado de Alta") },
        text = {
            Column {
                Text(
                    text = "El usuario se ha creado exitosamente. Comparte estas credenciales temporales para su primer inicio de sesión:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Nombre: ${usuario.nombre}", fontWeight = FontWeight.Bold)
                        Text(text = "Correo: ${usuario.correo}")
                        Text(text = "Clave Temporal: $claveTemporal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val textoCopiar = "Hola ${usuario.nombre}, tus credenciales de acceso para StageTrack son:\nCorreo: ${usuario.correo}\nContraseña Temporal: $claveTemporal\nPor favor inicia sesión para configurar tu perfil."
                    clipboardManager.setText(AnnotatedString(textoCopiar))
                    copiado = true
                }
            ) {
                Icon(
                    imageVector = if (copiado) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (copiado) "¡Copiado!" else "Copiar Credenciales")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
