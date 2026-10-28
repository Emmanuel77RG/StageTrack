package com.example.stagetrack.datos.repositorio

import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.dominio.modelo.Usuario
import com.example.stagetrack.dominio.repositorio.RepositorioAutenticacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementación falsa (mock) del repositorio de autenticación para pruebas y prototipo inicial.
 */
class RepositorioAutenticacionFalso : RepositorioAutenticacion {

    private val listaUsuariosInicial = mutableListOf(
        Usuario(
            id = "usr_admin",
            nombre = "Administrador General",
            correo = "admin@stagetrack.com",
            rol = RolUsuario.ADMINISTRADOR_GENERAL,
            fechaCreacion = System.currentTimeMillis(),
            activo = true
        ),
        Usuario(
            id = "usr_01",
            nombre = "Carlos Rodríguez",
            correo = "tecnico@stagetrack.com",
            rol = RolUsuario.TECNICO,
            fechaCreacion = System.currentTimeMillis() - 86400000,
            activo = true
        ),
        Usuario(
            id = "usr_02",
            nombre = "María López",
            correo = "bodega@stagetrack.com",
            rol = RolUsuario.JEFE_BODEGA,
            fechaCreacion = System.currentTimeMillis() - 172800000,
            activo = true
        )
    )

    private val _usuariosFlow = MutableStateFlow<List<Usuario>>(listaUsuariosInicial.toList())
    private val _usuarioActual = MutableStateFlow<Usuario?>(listaUsuariosInicial[0])

    override fun obtenerUsuarioActual(): Flow<Usuario?> = _usuarioActual.asStateFlow()

    override suspend fun iniciarSesion(correo: String, clave: String): Resultado<Usuario> {
        val usuarioEncontrado = listaUsuariosInicial.find { it.correo.lowercase() == correo.lowercase() }
        if (usuarioEncontrado != null) {
            if (!usuarioEncontrado.activo) {
                return Resultado.Error(IllegalStateException("El usuario se encuentra inactivo. Contacte al administrador."))
            }
            _usuarioActual.value = usuarioEncontrado
            return Resultado.Exito(usuarioEncontrado)
        }

        val rolDetectado = when {
            correo.contains("admin") -> RolUsuario.ADMINISTRADOR_GENERAL
            correo.contains("bodega") -> RolUsuario.JEFE_BODEGA
            correo.contains("supervisor") || correo.contains("director") -> RolUsuario.SUPERVISOR
            else -> RolUsuario.TECNICO
        }
        val usuarioNuevo = Usuario(
            id = "usr_${System.currentTimeMillis()}",
            nombre = correo.substringBefore("@").replaceFirstChar { it.uppercase() },
            correo = correo,
            rol = rolDetectado
        )
        listaUsuariosInicial.add(usuarioNuevo)
        _usuariosFlow.value = listaUsuariosInicial.toList()
        _usuarioActual.value = usuarioNuevo
        return Resultado.Exito(usuarioNuevo)
    }

    override suspend fun cerrarSesion(): Resultado<Unit> {
        _usuarioActual.value = null
        return Resultado.Exito(Unit)
    }

    override fun obtenerTodosLosUsuarios(): Flow<List<Usuario>> = _usuariosFlow.asStateFlow()

    override suspend fun crearUsuario(
        nombre: String,
        correo: String,
        clave: String,
        rol: RolUsuario
    ): Resultado<Usuario> {
        if (listaUsuariosInicial.any { it.correo.lowercase() == correo.lowercase() }) {
            return Resultado.Error(IllegalArgumentException("Ya existe un usuario registrado con el correo: $correo"))
        }
        val nuevoUsuario = Usuario(
            id = "usr_${System.currentTimeMillis()}",
            nombre = nombre,
            correo = correo,
            rol = rol,
            fechaCreacion = System.currentTimeMillis(),
            activo = true
        )
        listaUsuariosInicial.add(nuevoUsuario)
        _usuariosFlow.value = listaUsuariosInicial.toList()
        return Resultado.Exito(nuevoUsuario)
    }

    override suspend fun restablecerContrasena(
        idUsuario: String,
        nuevaClave: String
    ): Resultado<Unit> {
        return Resultado.Exito(Unit)
    }

    override suspend fun cambiarEstadoActivo(
        idUsuario: String,
        activo: Boolean
    ): Resultado<Unit> {
        val index = listaUsuariosInicial.indexOfFirst { it.id == idUsuario }
        if (index != -1) {
            val usuarioModificado = listaUsuariosInicial[index].copy(activo = activo)
            listaUsuariosInicial[index] = usuarioModificado
            _usuariosFlow.value = listaUsuariosInicial.toList()
            return Resultado.Exito(Unit)
        }
        return Resultado.Error(IllegalArgumentException("Usuario no encontrado"))
    }

    override suspend fun actualizarDatosUsuario(
        idUsuario: String,
        nombre: String,
        correo: String,
        rol: RolUsuario
    ): Resultado<Unit> {
        val index = listaUsuariosInicial.indexOfFirst { it.id == idUsuario }
        if (index != -1) {
            val usuarioModificado = listaUsuariosInicial[index].copy(
                nombre = nombre,
                correo = correo,
                rol = rol
            )
            listaUsuariosInicial[index] = usuarioModificado
            _usuariosFlow.value = listaUsuariosInicial.toList()
            if (_usuarioActual.value?.id == idUsuario) {
                _usuarioActual.value = usuarioModificado
            }
            return Resultado.Exito(Unit)
        }
        return Resultado.Error(IllegalArgumentException("Usuario no encontrado"))
    }


    override suspend fun actualizarFotoPerfil(
        idUsuario: String,
        urlFotografia: String
    ): Resultado<Unit> {
        val index = listaUsuariosInicial.indexOfFirst { it.id == idUsuario }
        if (index != -1) {
            val usuarioModificado = listaUsuariosInicial[index].copy(urlFotografia = urlFotografia)
            listaUsuariosInicial[index] = usuarioModificado
            _usuariosFlow.value = listaUsuariosInicial.toList()
            if (_usuarioActual.value?.id == idUsuario) {
                _usuarioActual.value = usuarioModificado
            }
            return Resultado.Exito(Unit)
        }
        return Resultado.Error(IllegalArgumentException("Usuario no encontrado"))
    }

    override suspend fun actualizarCorreoRecuperacion(
        idUsuario: String,
        correoRecuperacion: String?
    ): Resultado<Unit> {
        val index = listaUsuariosInicial.indexOfFirst { it.id == idUsuario }
        if (index != -1) {
            val usuarioModificado = listaUsuariosInicial[index].copy(correoRecuperacion = correoRecuperacion)
            listaUsuariosInicial[index] = usuarioModificado
            _usuariosFlow.value = listaUsuariosInicial.toList()
            if (_usuarioActual.value?.id == idUsuario) {
                _usuarioActual.value = usuarioModificado
            }
            return Resultado.Exito(Unit)
        }
        return Resultado.Error(IllegalArgumentException("Usuario no encontrado"))
    }

    override suspend fun cambiarContrasenaConVerificacion(
        idUsuario: String,
        claveActual: String,
        nuevaClave: String
    ): Resultado<Unit> {
        return Resultado.Exito(Unit)
    }

    override suspend fun enviarVerificacionCorreoRecuperacion(
        correoRecuperacion: String
    ): Resultado<Unit> {
        return Resultado.Exito(Unit)
    }

    override suspend fun completarPrimerInicio(
        idUsuario: String,
        claveTemporal: String,
        nuevaClave: String,
        correoRecuperacion: String,
        urlFotografia: String?
    ): Resultado<Unit> {
        val index = listaUsuariosInicial.indexOfFirst { it.id == idUsuario }
        if (index != -1) {
            val usuarioModificado = listaUsuariosInicial[index].copy(
                correoRecuperacion = correoRecuperacion,
                urlFotografia = urlFotografia ?: listaUsuariosInicial[index].urlFotografia,
                esPrimerInicio = false,
                correoVerificado = true
            )
            listaUsuariosInicial[index] = usuarioModificado
            _usuariosFlow.value = listaUsuariosInicial.toList()
            _usuarioActual.value = usuarioModificado
            return Resultado.Exito(Unit)
        }
        return Resultado.Error(IllegalArgumentException("Usuario no encontrado"))
    }

    override suspend fun sincronizarUsuariosLocalesAFirestore(): Resultado<Int> {
        return Resultado.Exito(0)
    }
}
