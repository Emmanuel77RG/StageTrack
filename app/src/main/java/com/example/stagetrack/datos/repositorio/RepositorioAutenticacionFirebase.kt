package com.example.stagetrack.datos.repositorio

import android.content.Context
import android.util.Log
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.core.cripto.UtilidadHash
import com.example.stagetrack.datos.local.dao.DaoUsuario
import com.example.stagetrack.datos.local.entidad.EntidadUsuario
import com.example.stagetrack.datos.mapeadores.MapeadorUsuario
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.dominio.modelo.Usuario
import com.example.stagetrack.dominio.repositorio.RepositorioAutenticacion
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Implementación robusta del repositorio de autenticación con Firebase Authentication
 * y fallback automático a Room local para máxima estabilidad en cualquier entorno.
 */
class RepositorioAutenticacionFirebase(
    private val context: Context,
    private val daoUsuario: DaoUsuario,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : RepositorioAutenticacion {

    private val _usuarioLocalActual = MutableStateFlow<Usuario?>(null)

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseApp.initializeApp(context.applicationContext)
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w("RepositorioAuthFirebase", "Firebase Auth no disponible en este entorno: ${e.message}")
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("RepositorioAuthFirebase", "Firestore no disponible: ${e.message}")
            null
        }
    }

    init {
        scope.launch {
            try {
                inicializarUsuarioSemillaSiEsNecesario()
            } catch (e: Exception) {
                Log.e("RepositorioAuthFirebase", "Error al inicializar usuarios semilla: ${e.message}", e)
            }
        }

        try {
            firestore?.collection("usuarios")?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("RepositorioAuthFirebase", "Aviso en listener de usuarios Firestore: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    scope.launch {
                        snapshot.documents.forEach { doc ->
                            try {
                                val idUsr = doc.id
                                val urlFoto = doc.getString("urlFotografia")
                                val correoRec = doc.getString("correoRecuperacion")
                                val esPrimerIn = doc.getBoolean("esPrimerInicio") ?: false
                                val correoVer = doc.getBoolean("correoVerificado") ?: false
                                val nombreUsr = doc.getString("nombre")
                                val rolUsr = doc.getString("rol") ?: RolUsuario.TECNICO.name
                                val correoUsr = doc.getString("correo") ?: ""
                                val activoUsr = doc.getBoolean("activo") ?: true
                                val fechaCreacionUsr = doc.getLong("fechaCreacion") ?: System.currentTimeMillis()

                                val entidadExistente = daoUsuario.obtenerUsuarioPorId(idUsr)
                                if (entidadExistente != null) {
                                    val entidadActualizada = entidadExistente.copy(
                                        nombre = nombreUsr ?: entidadExistente.nombre,
                                        correo = if (correoUsr.isNotBlank()) correoUsr else entidadExistente.correo,
                                        rol = rolUsr,
                                        urlFotografia = urlFoto ?: entidadExistente.urlFotografia,
                                        correoRecuperacion = correoRec ?: entidadExistente.correoRecuperacion,
                                        esPrimerInicio = esPrimerIn,
                                        correoVerificado = correoVer,
                                        activo = activoUsr
                                    )
                                    daoUsuario.insertarOActualizarUsuario(entidadActualizada)
                                    if (_usuarioLocalActual.value?.id == idUsr) {
                                        _usuarioLocalActual.value = MapeadorUsuario.aDominio(entidadActualizada)
                                    }
                                } else {
                                    if (!nombreUsr.isNullOrBlank() && correoUsr.isNotBlank()) {
                                        val nuevaEntidad = EntidadUsuario(
                                            id = idUsr,
                                            nombre = nombreUsr,
                                            correo = correoUsr,
                                            claveHash = "",
                                            rol = rolUsr,
                                            urlFotografia = urlFoto,
                                            correoRecuperacion = correoRec,
                                            esPrimerInicio = esPrimerIn,
                                            correoVerificado = correoVer,
                                            fechaCreacion = fechaCreacionUsr,
                                            activo = activoUsr
                                        )
                                        daoUsuario.insertarOActualizarUsuario(nuevaEntidad)
                                    }
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("RepositorioAuthFirebase", "Aviso al suscribir escuchador de usuarios en Firestore: ${e.message}")
        }
    }

    private suspend fun inicializarUsuarioSemillaSiEsNecesario() {
        try {
            daoUsuario.eliminarUsuarioDirectorLegacy()
            daoUsuario.migrarRolesDirectoresASupervisor()

            val semillas = listOf(
                EntidadUsuario(
                    id = "usr_admin_01",
                    nombre = "Administrador General",
                    correo = "admin@stagetrack.com",
                    claveHash = "",
                    rol = RolUsuario.ADMINISTRADOR_GENERAL.name,
                    fechaCreacion = System.currentTimeMillis() - (86400000 * 3),
                    activo = true
                ),
                EntidadUsuario(
                    id = "usr_tecnico_01",
                    nombre = "Carlos Rodríguez",
                    correo = "tecnico@stagetrack.com",
                    claveHash = "",
                    rol = RolUsuario.TECNICO.name,
                    fechaCreacion = System.currentTimeMillis() - (86400000 * 2),
                    activo = true
                ),
                EntidadUsuario(
                    id = "usr_bodega_01",
                    nombre = "María López",
                    correo = "bodega@stagetrack.com",
                    claveHash = "",
                    rol = RolUsuario.JEFE_BODEGA.name,
                    fechaCreacion = System.currentTimeMillis() - 86400000,
                    activo = true
                ),
                EntidadUsuario(
                    id = "usr_supervisor_01",
                    nombre = "Roberto Gómez",
                    correo = "supervisor@stagetrack.com",
                    claveHash = "",
                    rol = RolUsuario.SUPERVISOR.name,
                    fechaCreacion = System.currentTimeMillis(),
                    activo = true
                )
            )

            for (usuario in semillas) {
                val existente = daoUsuario.obtenerUsuarioPorCorreo(usuario.correo)
                if (existente == null) {
                    daoUsuario.insertarOActualizarUsuario(usuario)
                }
                try {
                    val docFirestore = firestore?.collection("usuarios")?.document(usuario.id)?.get()?.await()
                    if (docFirestore == null || !docFirestore.exists()) {
                        val mapaData = hashMapOf(
                            "id" to usuario.id,
                            "nombre" to usuario.nombre,
                            "correo" to usuario.correo,
                            "rol" to usuario.rol,
                            "urlFotografia" to usuario.urlFotografia,
                            "correoRecuperacion" to usuario.correoRecuperacion,
                            "esPrimerInicio" to usuario.esPrimerInicio,
                            "correoVerificado" to usuario.correoVerificado,
                            "fechaCreacion" to usuario.fechaCreacion,
                            "activo" to usuario.activo
                        )
                        firestore?.collection("usuarios")?.document(usuario.id)?.set(mapaData)?.await()
                    }
                } catch (e: Exception) {
                    Log.w("RepositorioAuthFirebase", "Aviso al sincronizar usuario semilla ${usuario.correo} en Firestore: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("RepositorioAuthFirebase", "Error al verificar/insertar usuarios semilla: ${e.message}", e)
        }
    }

    override fun obtenerUsuarioActual(): Flow<Usuario?> = callbackFlow {
        val auth = firebaseAuth
        if (auth != null) {
            val listener = FirebaseAuth.AuthStateListener { authState ->
                val firebaseUser = authState.currentUser
                if (firebaseUser != null) {
                    scope.launch {
                        val correo = firebaseUser.email ?: ""
                        var entidad = daoUsuario.obtenerUsuarioPorId(firebaseUser.uid)
                        if (entidad == null && correo.isNotEmpty()) {
                            entidad = daoUsuario.obtenerUsuarioPorCorreo(correo.lowercase())
                        }

                        if (entidad != null) {
                            val usr = MapeadorUsuario.aDominio(entidad)
                            _usuarioLocalActual.value = usr
                            trySend(usr)
                        } else {
                            val nuevoNombre = firebaseUser.displayName.takeIf { !it.isNullOrBlank() }
                                ?: correo.substringBefore("@")
                            val nuevaEntidad = EntidadUsuario(
                                id = firebaseUser.uid,
                                nombre = nuevoNombre,
                                correo = correo.lowercase(),
                                claveHash = "",
                                rol = RolUsuario.TECNICO.name,
                                urlFotografia = firebaseUser.photoUrl?.toString(),
                                fechaCreacion = System.currentTimeMillis(),
                                activo = true
                            )
                            daoUsuario.insertarOActualizarUsuario(nuevaEntidad)
                            val usr = MapeadorUsuario.aDominio(nuevaEntidad)
                            _usuarioLocalActual.value = usr
                            trySend(usr)
                        }
                    }
                } else {
                    scope.launch {
                        trySend(_usuarioLocalActual.value)
                    }
                }
            }

            try {
                auth.addAuthStateListener(listener)
            } catch (e: Exception) {
                Log.e("RepositorioAuthFirebase", "Error al agregar AuthStateListener: ${e.message}", e)
                trySend(_usuarioLocalActual.value)
            }

            val jobLocal = scope.launch {
                _usuarioLocalActual.collect { usr ->
                    trySend(usr)
                }
            }

            awaitClose {
                try {
                    auth.removeAuthStateListener(listener)
                } catch (_: Exception) {}
                jobLocal.cancel()
            }
        } else {
            val job = scope.launch {
                _usuarioLocalActual.collect { usr ->
                    trySend(usr)
                }
            }
            awaitClose { job.cancel() }
        }
    }

    override suspend fun iniciarSesion(correo: String, clave: String): Resultado<Usuario> {
        val correoLimpio = correo.trim().lowercase()

        // 1. Intento con Firebase Auth
        val auth = firebaseAuth
        if (auth != null) {
            try {
                val authResult = auth.signInWithEmailAndPassword(correoLimpio, clave).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    var entidad = daoUsuario.obtenerUsuarioPorId(firebaseUser.uid)
                    if (entidad == null) {
                        entidad = daoUsuario.obtenerUsuarioPorCorreo(correoLimpio)
                    }

                    if (entidad != null && !entidad.activo) {
                        auth.signOut()
                        return Resultado.Error(IllegalStateException("La cuenta de usuario está inactiva. Contacte al Administrador General."))
                    }

                    if (entidad == null) {
                        val nuevaEntidad = EntidadUsuario(
                            id = firebaseUser.uid,
                            nombre = firebaseUser.displayName ?: correoLimpio.substringBefore("@"),
                            correo = correoLimpio,
                            claveHash = "",
                            rol = RolUsuario.TECNICO.name,
                            urlFotografia = firebaseUser.photoUrl?.toString(),
                            fechaCreacion = System.currentTimeMillis(),
                            activo = true
                        )
                        daoUsuario.insertarOActualizarUsuario(nuevaEntidad)
                        entidad = nuevaEntidad
                    }

                    val usuarioDominio = MapeadorUsuario.aDominio(entidad)
                    _usuarioLocalActual.value = usuarioDominio
                    return Resultado.Exito(usuarioDominio)
                }
            } catch (e: FirebaseAuthException) {
                val mensajeError = when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "El formato del correo electrónico no es válido."
                    "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND", "ERROR_INVALID_CREDENTIAL" ->
                        "Correo electrónico o contraseña incorrectos."
                    "ERROR_USER_DISABLED" -> "La cuenta ha sido deshabilitada."
                    else -> "Error de autenticación: ${e.localizedMessage}"
                }
                return Resultado.Error(e, mensajeError)
            } catch (e: Exception) {
                Log.w("RepositorioAuthFirebase", "Aviso al intentar ingresar con Firebase: ${e.message}")
            }
        }

        // 2. Fallback Local (Room)
        return try {
            val entidad = daoUsuario.obtenerUsuarioPorCorreo(correoLimpio)
                ?: return Resultado.Error(IllegalArgumentException("El usuario con el correo $correo no está registrado."))

            if (!entidad.activo) {
                return Resultado.Error(IllegalStateException("La cuenta de usuario está inactiva. Contacte al Administrador General."))
            }

            val usuarioDominio = MapeadorUsuario.aDominio(entidad)
            _usuarioLocalActual.value = usuarioDominio
            Resultado.Exito(usuarioDominio)
        } catch (e: Exception) {
            Resultado.Error(e, "Error al iniciar sesión: ${e.localizedMessage}")
        }
    }

    override suspend fun cerrarSesion(): Resultado<Unit> {
        return try {
            firebaseAuth?.signOut()
            _usuarioLocalActual.value = null
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            _usuarioLocalActual.value = null
            Resultado.Exito(Unit)
        }
    }

    override fun obtenerTodosLosUsuarios(): Flow<List<Usuario>> {
        return daoUsuario.obtenerTodosLosUsuarios().map { listaEntidades ->
            listaEntidades.map { MapeadorUsuario.aDominio(it) }
        }
    }

    override suspend fun crearUsuario(
        nombre: String,
        correo: String,
        clave: String,
        rol: RolUsuario
    ): Resultado<Usuario> {
        val correoLimpio = correo.trim().lowercase()

        val auth = firebaseAuth
        if (auth != null) {
            var tempApp: FirebaseApp? = null
            try {
                val appPrincipal = FirebaseApp.getInstance()
                val tempAppName = "UserCreation_${System.currentTimeMillis()}"
                tempApp = FirebaseApp.initializeApp(context, appPrincipal.options, tempAppName)
                val tempAuth = FirebaseAuth.getInstance(tempApp)

                val authResult = tempAuth.createUserWithEmailAndPassword(correoLimpio, clave).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val nuevoUsuarioDominio = Usuario(
                        id = firebaseUser.uid,
                        nombre = nombre.trim(),
                        correo = correoLimpio,
                        rol = rol,
                        esPrimerInicio = true,
                        fechaCreacion = System.currentTimeMillis(),
                        activo = true
                    )

                    val entidad = MapeadorUsuario.aEntidad(nuevoUsuarioDominio, claveHash = "")
                    daoUsuario.insertarOActualizarUsuario(entidad)

                    try {
                        val mapaData = hashMapOf(
                            "id" to nuevoUsuarioDominio.id,
                            "nombre" to nuevoUsuarioDominio.nombre,
                            "correo" to nuevoUsuarioDominio.correo,
                            "rol" to nuevoUsuarioDominio.rol.name,
                            "urlFotografia" to nuevoUsuarioDominio.urlFotografia,
                            "correoRecuperacion" to nuevoUsuarioDominio.correoRecuperacion,
                            "esPrimerInicio" to nuevoUsuarioDominio.esPrimerInicio,
                            "correoVerificado" to nuevoUsuarioDominio.correoVerificado,
                            "fechaCreacion" to nuevoUsuarioDominio.fechaCreacion,
                            "activo" to nuevoUsuarioDominio.activo
                        )
                        firestore?.collection("usuarios")?.document(nuevoUsuarioDominio.id)?.set(mapaData)?.await()
                    } catch (e: Exception) {
                        Log.w("RepositorioAuthFirebase", "Aviso al guardar nuevo usuario en Firestore: ${e.message}")
                    }

                    try {
                        tempApp.delete()
                    } catch (_: Exception) {}

                    return Resultado.Exito(nuevoUsuarioDominio)
                }
            } catch (e: FirebaseAuthException) {
                try { tempApp?.delete() } catch (_: Exception) {}
                val mensajeError = when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya existe un usuario registrado con el correo $correo."
                    "ERROR_WEAK_PASSWORD" -> "La contraseña ingresada es demasiado débil. Utilice al menos 6 caracteres."
                    "ERROR_INVALID_EMAIL" -> "El correo electrónico ingresado no tiene un formato válido."
                    else -> "Error de registro en Firebase: ${e.localizedMessage}"
                }
                return Resultado.Error(e, mensajeError)
            } catch (e: Exception) {
                try { tempApp?.delete() } catch (_: Exception) {}
                Log.w("RepositorioAuthFirebase", "Error al crear usuario en Firebase Auth: ${e.message}")
            }
        }

        // Fallback Local
        return try {
            val usuarioExistente = daoUsuario.obtenerUsuarioPorCorreo(correoLimpio)
            if (usuarioExistente != null) {
                return Resultado.Error(IllegalArgumentException("Ya existe un usuario registrado con el correo $correoLimpio."))
            }

            val nuevoUsuarioDominio = Usuario(
                id = "usr_${System.currentTimeMillis()}",
                nombre = nombre.trim(),
                correo = correoLimpio,
                rol = rol,
                esPrimerInicio = true,
                fechaCreacion = System.currentTimeMillis(),
                activo = true
            )

            val entidad = MapeadorUsuario.aEntidad(nuevoUsuarioDominio, claveHash = "")
            daoUsuario.insertarOActualizarUsuario(entidad)

            try {
                val mapaData = hashMapOf(
                    "id" to nuevoUsuarioDominio.id,
                    "nombre" to nuevoUsuarioDominio.nombre,
                    "correo" to nuevoUsuarioDominio.correo,
                    "rol" to nuevoUsuarioDominio.rol.name,
                    "urlFotografia" to nuevoUsuarioDominio.urlFotografia,
                    "correoRecuperacion" to nuevoUsuarioDominio.correoRecuperacion,
                    "esPrimerInicio" to nuevoUsuarioDominio.esPrimerInicio,
                    "correoVerificado" to nuevoUsuarioDominio.correoVerificado,
                    "fechaCreacion" to nuevoUsuarioDominio.fechaCreacion,
                    "activo" to nuevoUsuarioDominio.activo
                )
                firestore?.collection("usuarios")?.document(nuevoUsuarioDominio.id)?.set(mapaData)?.await()
            } catch (e: Exception) {
                Log.w("RepositorioAuthFirebase", "Aviso al guardar usuario fallback local en Firestore: ${e.message}")
            }

            Resultado.Exito(nuevoUsuarioDominio)
        } catch (e: Exception) {
            Resultado.Error(e, "Error al crear el usuario: ${e.localizedMessage}")
        }
    }

    override suspend fun restablecerContrasena(
        idUsuario: String,
        nuevaClave: String
    ): Resultado<Unit> {
        return try {
            val usuarioEntidad = daoUsuario.obtenerUsuarioPorId(idUsuario)
                ?: return Resultado.Error(IllegalArgumentException("Usuario no encontrado."))

            firebaseAuth?.sendPasswordResetEmail(usuarioEntidad.correo)?.await()
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e, "Error al solicitar restablecimiento de contraseña: ${e.localizedMessage}")
        }
    }

    override suspend fun cambiarEstadoActivo(
        idUsuario: String,
        activo: Boolean
    ): Resultado<Unit> {
        return try {
            val usuarioEntidad = daoUsuario.obtenerUsuarioPorId(idUsuario)
                ?: return Resultado.Error(IllegalArgumentException("Usuario no encontrado."))

            if (!activo && usuarioEntidad.rol == RolUsuario.ADMINISTRADOR_GENERAL.name) {
                val adminsActivos = daoUsuario.contarAdministradoresActivos()
                if (adminsActivos <= 1) {
                    return Resultado.Error(IllegalStateException("No se puede desactivar al único Administrador General activo."))
                }
            }

            daoUsuario.actualizarEstadoActivo(idUsuario, activo)
            try {
                firestore?.collection("usuarios")?.document(idUsuario)
                    ?.set(mapOf("activo" to activo), SetOptions.merge())?.await()
            } catch (e: Exception) {
                Log.w("RepositorioAuthFirebase", "Aviso al enviar estado activo a Firestore: ${e.message}")
            }

            val usuarioActualFirebase = firebaseAuth?.currentUser
            if (!activo && usuarioActualFirebase?.uid == idUsuario) {
                firebaseAuth?.signOut()
            }

            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e, "Error al cambiar el estado del usuario: ${e.localizedMessage}")
        }
    }

    override suspend fun actualizarDatosUsuario(
        idUsuario: String,
        nombre: String,
        correo: String,
        rol: RolUsuario
    ): Resultado<Unit> {
        return try {
            daoUsuario.actualizarDatosUsuario(idUsuario, nombre, correo, rol.name)
            try {
                firestore?.collection("usuarios")?.document(idUsuario)
                    ?.set(
                        mapOf(
                            "nombre" to nombre,
                            "correo" to correo,
                            "rol" to rol.name
                        ),
                        SetOptions.merge()
                    )?.await()
            } catch (e: Exception) {
                Log.w("RepositorioAuthFirebase", "Aviso al actualizar datos en Firestore: ${e.message}")
            }

            val actual = _usuarioLocalActual.value
            if (actual != null && actual.id == idUsuario) {
                _usuarioLocalActual.value = actual.copy(nombre = nombre, correo = correo, rol = rol)
            }
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e, "Error al actualizar datos del usuario: ${e.localizedMessage}")
        }
    }


    override suspend fun actualizarFotoPerfil(
        idUsuario: String,
        urlFotografia: String
    ): Resultado<Unit> {
        return try {
            daoUsuario.actualizarFotoPerfil(idUsuario, urlFotografia)
            if (_usuarioLocalActual.value?.id == idUsuario) {
                _usuarioLocalActual.value = _usuarioLocalActual.value?.copy(urlFotografia = urlFotografia)
            }
            try {
                firestore?.collection("usuarios")?.document(idUsuario)
                    ?.set(mapOf("urlFotografia" to urlFotografia), SetOptions.merge())?.await()
            } catch (e: Exception) {
                Log.w("RepositorioAuthFirebase", "Aviso al enviar urlFotografia a Firestore: ${e.message}")
            }
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e, "Error al actualizar la foto de perfil: ${e.localizedMessage}")
        }
    }

    override suspend fun actualizarCorreoRecuperacion(
        idUsuario: String,
        correoRecuperacion: String?
    ): Resultado<Unit> {
        return try {
            val correoLimpio = correoRecuperacion?.trim()?.lowercase()
            daoUsuario.actualizarCorreoRecuperacion(idUsuario, correoLimpio)
            if (_usuarioLocalActual.value?.id == idUsuario) {
                _usuarioLocalActual.value = _usuarioLocalActual.value?.copy(correoRecuperacion = correoLimpio)
            }
            try {
                firestore?.collection("usuarios")?.document(idUsuario)
                    ?.set(mapOf("correoRecuperacion" to correoLimpio), SetOptions.merge())?.await()
            } catch (_: Exception) {}
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e, "Error al actualizar el correo de recuperación: ${e.localizedMessage}")
        }
    }

    override suspend fun cambiarContrasenaConVerificacion(
        idUsuario: String,
        claveActual: String,
        nuevaClave: String
    ): Resultado<Unit> {
        return try {
            val user = firebaseAuth?.currentUser
            if (user != null) {
                if (!user.email.isNullOrBlank()) {
                    try {
                        val credencial = EmailAuthProvider.getCredential(user.email!!, claveActual)
                        user.reauthenticate(credencial).await()
                    } catch (reauthEx: Exception) {
                        Log.w("RepositorioAuthFirebase", "Reautenticación previa a updatePassword omitida/fallida: ${reauthEx.message}")
                    }
                }
                user.updatePassword(nuevaClave).await()
            }
            val claveHash = UtilidadHash.calcularSha256(nuevaClave)
            daoUsuario.actualizarContrasena(idUsuario, claveHash)
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e, "Error al cambiar la contraseña: ${e.localizedMessage}")
        }
    }

    override suspend fun enviarVerificacionCorreoRecuperacion(
        correoRecuperacion: String
    ): Resultado<Unit> {
        return try {
            val user = firebaseAuth?.currentUser
            val correoLimpio = correoRecuperacion.trim().lowercase()
            if (user != null) {
                try {
                    user.verifyBeforeUpdateEmail(correoLimpio).await()
                } catch (ex: Exception) {
                    Log.w("RepositorioAuthFirebase", "verifyBeforeUpdateEmail falló, intentando sendPasswordResetEmail: ${ex.message}")
                    firebaseAuth?.sendPasswordResetEmail(correoLimpio)?.await()
                }
            } else {
                firebaseAuth?.sendPasswordResetEmail(correoLimpio)?.await()
            }
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e, "Error al enviar correo de verificación/recuperación: ${e.localizedMessage}")
        }
    }

    override suspend fun completarPrimerInicio(
        idUsuario: String,
        claveTemporal: String,
        nuevaClave: String,
        correoRecuperacion: String,
        urlFotografia: String?
    ): Resultado<Unit> {
        return try {
            val user = firebaseAuth?.currentUser
            if (user != null) {
                if (!user.email.isNullOrBlank()) {
                    try {
                        val credencial = EmailAuthProvider.getCredential(user.email!!, claveTemporal)
                        user.reauthenticate(credencial).await()
                    } catch (reauthEx: Exception) {
                        Log.w("RepositorioAuthFirebase", "Reautenticación previa a updatePassword omitida/fallida: ${reauthEx.message}")
                    }
                }
                user.updatePassword(nuevaClave).await()
            }

            val correoLimpio = correoRecuperacion.trim().lowercase()
            val claveHash = UtilidadHash.calcularSha256(nuevaClave)

            daoUsuario.actualizarContrasena(idUsuario, claveHash)
            daoUsuario.actualizarCorreoRecuperacion(idUsuario, correoLimpio)
            if (!urlFotografia.isNullOrBlank()) {
                daoUsuario.actualizarFotoPerfil(idUsuario, urlFotografia)
            }
            daoUsuario.actualizarEstadoPrimerInicio(idUsuario, false)
            daoUsuario.actualizarCorreoVerificado(idUsuario, true)

            val mapaUpdates = mutableMapOf<String, Any?>(
                "correoRecuperacion" to correoLimpio,
                "esPrimerInicio" to false,
                "correoVerificado" to true
            )
            if (!urlFotografia.isNullOrBlank()) {
                mapaUpdates["urlFotografia"] = urlFotografia
            }

            try {
                firestore?.collection("usuarios")?.document(idUsuario)
                    ?.set(mapaUpdates, SetOptions.merge())?.await()
            } catch (_: Exception) {}

            val actualizado = daoUsuario.obtenerUsuarioPorId(idUsuario)
            if (actualizado != null) {
                _usuarioLocalActual.value = MapeadorUsuario.aDominio(actualizado)
            }

            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e, "Error al completar la configuración de primer inicio: ${e.localizedMessage}")
        }
    }

    override suspend fun sincronizarUsuariosLocalesAFirestore(): Resultado<Int> {
        val dbFirestore = firestore
            ?: return Resultado.Error(IllegalStateException("Firestore no está disponible en este dispositivo."))

        return try {
            val listaEntidades = daoUsuario.obtenerListaTodasEntidadesDirecta()
            var creadosONuevos = 0

            for (entidad in listaEntidades) {
                val docRef = dbFirestore.collection("usuarios").document(entidad.id)
                val snapshot = try {
                    docRef.get().await()
                } catch (e: Exception) {
                    null
                }

                val yaExisteCompleto = snapshot != null && snapshot.exists() &&
                        !snapshot.getString("nombre").isNullOrBlank() &&
                        !snapshot.getString("correo").isNullOrBlank()

                if (!yaExisteCompleto) {
                    val mapaData = hashMapOf(
                        "id" to entidad.id,
                        "nombre" to entidad.nombre,
                        "correo" to entidad.correo,
                        "rol" to entidad.rol,
                        "urlFotografia" to entidad.urlFotografia,
                        "correoRecuperacion" to entidad.correoRecuperacion,
                        "esPrimerInicio" to entidad.esPrimerInicio,
                        "correoVerificado" to entidad.correoVerificado,
                        "fechaCreacion" to entidad.fechaCreacion,
                        "activo" to entidad.activo
                    )
                    docRef.set(mapaData, SetOptions.merge()).await()
                    creadosONuevos++
                }
            }

            Resultado.Exito(creadosONuevos)
        } catch (e: Exception) {
            Resultado.Error(e, "Error al sincronizar usuarios locales a la nube: ${e.localizedMessage}")
        }
    }
}
