package com.example.stagetrack.datos.repositorio

import android.util.Log
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.core.cripto.UtilidadHash
import com.example.stagetrack.datos.local.dao.DaoUsuario
import com.example.stagetrack.datos.local.entidad.EntidadUsuario
import com.example.stagetrack.datos.mapeadores.MapeadorUsuario
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.dominio.modelo.Usuario
import com.example.stagetrack.dominio.repositorio.RepositorioAutenticacion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Implementación de producción del repositorio de autenticación y gestión de usuarios basada en Room.
 */
class RepositorioAutenticacionRoom(
    private val daoUsuario: DaoUsuario,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : RepositorioAutenticacion {

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)

    init {
        scope.launch {
            try {
                inicializarUsuarioSemillaSiEsNecesario()
            } catch (e: Exception) {
                Log.e("RepositorioAutenticacion", "Error al inicializar usuarios semilla en Room: ${e.message}", e)
            }
        }
    }

    private suspend fun inicializarUsuarioSemillaSiEsNecesario() {
        try {
            daoUsuario.eliminarUsuarioDirectorLegacy()
            daoUsuario.migrarRolesDirectoresASupervisor()

            val claveHashAdmin = UtilidadHash.calcularSha256("admin123")
            val claveHashComun = UtilidadHash.calcularSha256("123456")

            val semillas = listOf(
                EntidadUsuario(
                    id = "usr_admin_01",
                    nombre = "Administrador General",
                    correo = "admin@stagetrack.com",
                    claveHash = claveHashAdmin,
                    rol = RolUsuario.ADMINISTRADOR_GENERAL.name,
                    fechaCreacion = System.currentTimeMillis() - (86400000 * 3),
                    activo = true
                ),
                EntidadUsuario(
                    id = "usr_tecnico_01",
                    nombre = "Carlos Rodríguez",
                    correo = "tecnico@stagetrack.com",
                    claveHash = claveHashComun,
                    rol = RolUsuario.TECNICO.name,
                    fechaCreacion = System.currentTimeMillis() - (86400000 * 2),
                    activo = true
                ),
                EntidadUsuario(
                    id = "usr_bodega_01",
                    nombre = "María López",
                    correo = "bodega@stagetrack.com",
                    claveHash = claveHashComun,
                    rol = RolUsuario.JEFE_BODEGA.name,
                    fechaCreacion = System.currentTimeMillis() - 86400000,
                    activo = true
                ),
                EntidadUsuario(
                    id = "usr_supervisor_01",
                    nombre = "Roberto Gómez",
                    correo = "supervisor@stagetrack.com",
                    claveHash = claveHashComun,
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
            }
        } catch (e: Exception) {
            Log.e("RepositorioAutenticacion", "Error al verificar/insertar usuarios semilla: ${e.message}", e)
        }
    }

    override fun obtenerUsuarioActual(): Flow<Usuario?> = _usuarioActual.asStateFlow()

    override suspend fun iniciarSesion(correo: String, clave: String): Resultado<Usuario> {
        return try {
            inicializarUsuarioSemillaSiEsNecesario()

            val entidad = daoUsuario.obtenerUsuarioPorCorreo(correo.trim().lowercase())
                ?: return Resultado.Error(IllegalArgumentException("El usuario con el correo $correo no está registrado."))

            if (!entidad.activo) {
                return Resultado.Error(IllegalStateException("La cuenta de usuario está inactiva. Contacte al Administrador General."))
            }

            val claveHashIngresada = UtilidadHash.calcularSha256(clave)
            if (entidad.claveHash != claveHashIngresada) {
                return Resultado.Error(IllegalArgumentException("La contraseña ingresada es incorrecta."))
            }

            val usuarioDominio = MapeadorUsuario.aDominio(entidad)
            _usuarioActual.value = usuarioDominio
            Resultado.Exito(usuarioDominio)
        } catch (e: Exception) {
            Resultado.Error(e, "Error al iniciar sesión: ${e.localizedMessage}")
        }
    }

    override suspend fun cerrarSesion(): Resultado<Unit> {
        _usuarioActual.value = null
        return Resultado.Exito(Unit)
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
        return try {
            val correoLimpio = correo.trim().lowercase()
            val usuarioExistente = daoUsuario.obtenerUsuarioPorCorreo(correoLimpio)
            if (usuarioExistente != null) {
                return Resultado.Error(IllegalArgumentException("Ya existe un usuario registrado con el correo $correoLimpio."))
            }

            val idNuevo = "usr_${System.currentTimeMillis()}"
            val claveHash = UtilidadHash.calcularSha256(clave)
            val usuarioNuevoDominio = Usuario(
                id = idNuevo,
                nombre = nombre.trim(),
                correo = correoLimpio,
                rol = rol,
                esPrimerInicio = true,
                fechaCreacion = System.currentTimeMillis(),
                activo = true
            )

            val entidad = MapeadorUsuario.aEntidad(usuarioNuevoDominio, claveHash)
            daoUsuario.insertarOActualizarUsuario(entidad)

            Resultado.Exito(usuarioNuevoDominio)
        } catch (e: Exception) {
            Resultado.Error(e, "Error al dar de alta al usuario: ${e.localizedMessage}")
        }
    }

    override suspend fun restablecerContrasena(
        idUsuario: String,
        nuevaClave: String
    ): Resultado<Unit> {
        return try {
            val claveHash = UtilidadHash.calcularSha256(nuevaClave)
            daoUsuario.actualizarContrasena(idUsuario, claveHash)
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e, "Error al restablecer contraseña: ${e.localizedMessage}")
        }
    }

    override suspend fun cambiarEstadoActivo(
        idUsuario: String,
        activo: Boolean
    ): Resultado<Unit> {
        return try {
            val usuarioEntidad = daoUsuario.obtenerUsuarioPorId(idUsuario)
                ?: return Resultado.Error(IllegalArgumentException("Usuario no encontrado."))

            // Protección de Super-Admin: evitar desactivar al único admin activo
            if (!activo && usuarioEntidad.rol == RolUsuario.ADMINISTRADOR_GENERAL.name) {
                val adminsActivos = daoUsuario.contarAdministradoresActivos()
                if (adminsActivos <= 1) {
                    return Resultado.Error(IllegalStateException("No se puede desactivar al único Administrador General activo."))
                }
            }

            daoUsuario.actualizarEstadoActivo(idUsuario, activo)

            // Si el usuario desactivado es el usuario actual, cerrar su sesión
            if (!activo && _usuarioActual.value?.id == idUsuario) {
                _usuarioActual.value = null
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
            val actual = _usuarioActual.value
            if (actual != null && actual.id == idUsuario) {
                _usuarioActual.value = actual.copy(nombre = nombre, correo = correo, rol = rol)
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
            // Si el usuario modificado es el usuario actual en sesión, actualizar la variable de estado
            if (_usuarioActual.value?.id == idUsuario) {
                _usuarioActual.value = _usuarioActual.value?.copy(urlFotografia = urlFotografia)
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
            if (_usuarioActual.value?.id == idUsuario) {
                _usuarioActual.value = _usuarioActual.value?.copy(correoRecuperacion = correoLimpio)
            }
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
            val usuarioEntidad = daoUsuario.obtenerUsuarioPorId(idUsuario)
                ?: return Resultado.Error(IllegalArgumentException("Usuario no encontrado."))

            val claveHashActual = UtilidadHash.calcularSha256(claveActual)
            if (usuarioEntidad.claveHash.isNotBlank() && usuarioEntidad.claveHash != claveHashActual) {
                return Resultado.Error(IllegalArgumentException("La contraseña actual ingresada es incorrecta."))
            }

            val nuevaClaveHash = UtilidadHash.calcularSha256(nuevaClave)
            daoUsuario.actualizarContrasena(idUsuario, nuevaClaveHash)
            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e, "Error al cambiar la contraseña: ${e.localizedMessage}")
        }
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
        return try {
            val usuarioEntidad = daoUsuario.obtenerUsuarioPorId(idUsuario)
                ?: return Resultado.Error(IllegalArgumentException("Usuario no encontrado."))

            val claveHashTemporal = UtilidadHash.calcularSha256(claveTemporal)
            if (usuarioEntidad.claveHash.isNotBlank() && usuarioEntidad.claveHash != claveHashTemporal) {
                return Resultado.Error(IllegalArgumentException("La contraseña temporal ingresada no coincide."))
            }

            val nuevaClaveHash = UtilidadHash.calcularSha256(nuevaClave)
            val correoLimpio = correoRecuperacion.trim().lowercase()

            daoUsuario.actualizarContrasena(idUsuario, nuevaClaveHash)
            daoUsuario.actualizarCorreoRecuperacion(idUsuario, correoLimpio)
            if (!urlFotografia.isNullOrBlank()) {
                daoUsuario.actualizarFotoPerfil(idUsuario, urlFotografia)
            }
            daoUsuario.actualizarEstadoPrimerInicio(idUsuario, false)
            daoUsuario.actualizarCorreoVerificado(idUsuario, true)

            val actualizado = daoUsuario.obtenerUsuarioPorId(idUsuario)
            if (actualizado != null) {
                _usuarioActual.value = MapeadorUsuario.aDominio(actualizado)
            }

            Resultado.Exito(Unit)
        } catch (e: Exception) {
            Resultado.Error(e, "Error al completar la configuración inicial: ${e.localizedMessage}")
        }
    }

    override suspend fun sincronizarUsuariosLocalesAFirestore(): Resultado<Int> {
        return Resultado.Exito(0)
    }
}
