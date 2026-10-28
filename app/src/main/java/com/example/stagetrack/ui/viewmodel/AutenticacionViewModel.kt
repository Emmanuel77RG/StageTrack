package com.example.stagetrack.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.core.imagen.UtilidadImagen
import com.example.stagetrack.datos.repositorio.RepositorioAlmacenamientoSupabase
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.dominio.modelo.Usuario
import com.example.stagetrack.dominio.repositorio.RepositorioAlmacenamiento
import com.example.stagetrack.dominio.repositorio.RepositorioAutenticacion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel encargado del estado de autenticación, sesión activa y gestión de usuarios.
 */
class AutenticacionViewModel(
    private val repositorioAutenticacion: RepositorioAutenticacion,
    private val repositorioAlmacenamiento: RepositorioAlmacenamiento = RepositorioAlmacenamientoSupabase()
) : ViewModel() {

    val usuarioActual: StateFlow<Usuario?> = repositorioAutenticacion.obtenerUsuarioActual()
        .catch { e ->
            Log.e("AutenticacionViewModel", "Error al obtener usuario actual: ${e.message}", e)
            emit(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val listaUsuarios: StateFlow<List<Usuario>> = repositorioAutenticacion.obtenerTodosLosUsuarios()
        .catch { e ->
            Log.e("AutenticacionViewModel", "Error al obtener lista de usuarios: ${e.message}", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun iniciarSesion(
        correo: String,
        clave: String,
        onResultado: (Resultado<Usuario>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioAutenticacion.iniciarSesion(correo, clave)
            onResultado(res)
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            repositorioAutenticacion.cerrarSesion()
        }
    }

    fun crearUsuario(
        nombre: String,
        correo: String,
        clave: String,
        rol: RolUsuario,
        onResultado: (Resultado<Usuario>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioAutenticacion.crearUsuario(nombre, correo, clave, rol)
            onResultado(res)
        }
    }

    fun restablecerContrasena(
        idUsuario: String,
        nuevaClave: String,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioAutenticacion.restablecerContrasena(idUsuario, nuevaClave)
            onResultado(res)
        }
    }

    fun cambiarEstadoActivo(
        idUsuario: String,
        activo: Boolean,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioAutenticacion.cambiarEstadoActivo(idUsuario, activo)
            onResultado(res)
        }
    }

    fun actualizarDatosUsuario(
        idUsuario: String,
        nombre: String,
        correo: String,
        rol: RolUsuario,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioAutenticacion.actualizarDatosUsuario(idUsuario, nombre, correo, rol)
            onResultado(res)
        }
    }


    fun subirYActualizarFotoPerfil(
        context: Context,
        idUsuario: String,
        uriImagen: Uri,
        rotacionGrados: Float = 0f,
        escala: Float = 1.0f,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        tamanoVistaPx: Float = 0f,
        onResultado: (Resultado<String>) -> Unit
    ) {
        viewModelScope.launch {
            val bytes = UtilidadImagen.procesarYComprimirImagen(
                context = context,
                uri = uriImagen,
                rotacionAdicionalGrados = rotacionGrados,
                escala = escala,
                offsetX = offsetX,
                offsetY = offsetY,
                tamanoVistaPx = tamanoVistaPx
            )
            if (bytes == null) {
                onResultado(Resultado.Error(IllegalArgumentException("No se pudo procesar la imagen seleccionada.")))
                return@launch
            }

            val resSubida = repositorioAlmacenamiento.subirFotoPerfil(idUsuario, bytes)
            if (resSubida is Resultado.Exito) {
                val urlBase = resSubida.datos
                val urlAnticache = if (urlBase.contains("?")) "$urlBase&t=${System.currentTimeMillis()}" else "$urlBase?t=${System.currentTimeMillis()}"
                val resActualizacion = repositorioAutenticacion.actualizarFotoPerfil(idUsuario, urlAnticache)
                if (resActualizacion is Resultado.Exito) {
                    onResultado(Resultado.Exito(urlAnticache))
                } else if (resActualizacion is Resultado.Error) {
                    onResultado(Resultado.Error(resActualizacion.excepcion, resActualizacion.mensaje))
                }
            } else if (resSubida is Resultado.Error) {
                onResultado(Resultado.Error(resSubida.excepcion, resSubida.mensaje))
            }
        }
    }

    fun actualizarCorreoRecuperacion(
        idUsuario: String,
        correoRecuperacion: String?,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioAutenticacion.actualizarCorreoRecuperacion(idUsuario, correoRecuperacion)
            onResultado(res)
        }
    }

    fun cambiarContrasenaConVerificacion(
        idUsuario: String,
        claveActual: String,
        nuevaClave: String,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioAutenticacion.cambiarContrasenaConVerificacion(idUsuario, claveActual, nuevaClave)
            onResultado(res)
        }
    }

    fun enviarVerificacionCorreoRecuperacion(
        correoRecuperacion: String,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioAutenticacion.enviarVerificacionCorreoRecuperacion(correoRecuperacion)
            onResultado(res)
        }
    }

    fun completarPrimerInicio(
        context: Context,
        idUsuario: String,
        claveTemporal: String,
        nuevaClave: String,
        correoRecuperacion: String,
        uriFotoLocal: Uri?,
        rotacionFoto: Float = 0f,
        escalaFoto: Float = 1.0f,
        offsetXFoto: Float = 0f,
        offsetYFoto: Float = 0f,
        tamanoVistaPxFoto: Float = 0f,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            var urlFotoSubida: String? = null
            if (uriFotoLocal != null) {
                val bytes = UtilidadImagen.procesarYComprimirImagen(
                    context = context,
                    uri = uriFotoLocal,
                    rotacionAdicionalGrados = rotacionFoto,
                    escala = escalaFoto,
                    offsetX = offsetXFoto,
                    offsetY = offsetYFoto,
                    tamanoVistaPx = tamanoVistaPxFoto
                )
                if (bytes != null) {
                    val resSubida = repositorioAlmacenamiento.subirFotoPerfil(idUsuario, bytes)
                    if (resSubida is Resultado.Exito) {
                        val urlBase = resSubida.datos
                        urlFotoSubida = if (urlBase.contains("?")) "$urlBase&t=${System.currentTimeMillis()}" else "$urlBase?t=${System.currentTimeMillis()}"
                    }
                }
            }

            val res = repositorioAutenticacion.completarPrimerInicio(
                idUsuario = idUsuario,
                claveTemporal = claveTemporal,
                nuevaClave = nuevaClave,
                correoRecuperacion = correoRecuperacion,
                urlFotografia = urlFotoSubida
            )
            onResultado(res)
        }
    }

    fun sincronizarUsuariosConNube(
        onResultado: (Resultado<Int>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioAutenticacion.sincronizarUsuariosLocalesAFirestore()
            onResultado(res)
        }
    }
}
