package com.example.stagetrack.dominio.repositorio

import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.dominio.modelo.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para la autenticación, gestión de sesión y administración de usuarios.
 */
interface RepositorioAutenticacion {
    fun obtenerUsuarioActual(): Flow<Usuario?>
    suspend fun iniciarSesion(correo: String, clave: String): Resultado<Usuario>
    suspend fun cerrarSesion(): Resultado<Unit>

    fun obtenerTodosLosUsuarios(): Flow<List<Usuario>>
    suspend fun crearUsuario(
        nombre: String,
        correo: String,
        clave: String,
        rol: RolUsuario
    ): Resultado<Usuario>

    suspend fun restablecerContrasena(
        idUsuario: String,
        nuevaClave: String
    ): Resultado<Unit>

    suspend fun cambiarEstadoActivo(
        idUsuario: String,
        activo: Boolean
    ): Resultado<Unit>

    suspend fun actualizarDatosUsuario(
        idUsuario: String,
        nombre: String,
        correo: String,
        rol: RolUsuario
    ): Resultado<Unit>

    suspend fun actualizarFotoPerfil(
        idUsuario: String,
        urlFotografia: String
    ): Resultado<Unit>

    suspend fun actualizarCorreoRecuperacion(
        idUsuario: String,
        correoRecuperacion: String?
    ): Resultado<Unit>

    suspend fun cambiarContrasenaConVerificacion(
        idUsuario: String,
        claveActual: String,
        nuevaClave: String
    ): Resultado<Unit>

    suspend fun enviarVerificacionCorreoRecuperacion(
        correoRecuperacion: String
    ): Resultado<Unit>

    suspend fun completarPrimerInicio(
        idUsuario: String,
        claveTemporal: String,
        nuevaClave: String,
        correoRecuperacion: String,
        urlFotografia: String?
    ): Resultado<Unit>

    suspend fun sincronizarUsuariosLocalesAFirestore(): Resultado<Int>
}
