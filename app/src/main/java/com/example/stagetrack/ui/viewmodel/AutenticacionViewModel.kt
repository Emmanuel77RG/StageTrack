package com.example.stagetrack.ui.viewmodel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Resultado individual de la generación de un usuario en lote.
 */
data class CredencialUsuarioGenerada(
    val nombre: String,
    val correo: String,
    val claveTemporal: String,
    val rol: RolUsuario,
    val exito: Boolean,
    val mensajeError: String? = null
)

/**
 * Resumen consolidado del proceso de creación masiva de usuarios en lote.
 */
data class LoteResultadoCreacion(
    val totalProcesados: Int,
    val exitoCount: Int,
    val errorCount: Int,
    val rutaArchivoCredenciales: String,
    val listaCredenciales: List<CredencialUsuarioGenerada>
)

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

    fun registrarFcmTokenDispositivo(idUsuario: String, token: String) {
        if (repositorioAutenticacion is com.example.stagetrack.datos.repositorio.RepositorioAutenticacionFirebase) {
            repositorioAutenticacion.sincronizarFcmToken(idUsuario, token)
        }
    }

    val listaUsuarios: StateFlow<List<Usuario>> = repositorioAutenticacion.obtenerTodosLosUsuarios()
        .map { lista ->
            lista.sortedBy { it.nombre.lowercase() }
        }
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

    /**
     * Procesa la creación masiva de usuarios a partir de un contenido en formato CSV,
     * genera las cuentas correspondientes con contraseñas temporales y exporta
     * un archivo de credenciales consolidado en Descargas/StageTrack_Reports.
     */
    fun crearUsuariosEnLoteCsv(
        context: Context,
        contenidoCsv: String,
        onProgreso: (actual: Int, total: Int, correoActual: String) -> Unit,
        onResultado: (Resultado<LoteResultadoCreacion>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val lineas = contenidoCsv.lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                if (lineas.isEmpty()) {
                    onResultado(Resultado.Error(IllegalArgumentException("El contenido CSV está vacío o no contiene líneas válidas.")))
                    return@launch
                }

                val primeraLinea = lineas.first().lowercase()
                val lineasDatos = if (primeraLinea.contains("nombre") || primeraLinea.contains("correo") || primeraLinea.contains("email") || primeraLinea.contains("rol")) {
                    lineas.drop(1)
                } else {
                    lineas
                }

                if (lineasDatos.isEmpty()) {
                    onResultado(Resultado.Error(IllegalArgumentException("No se encontraron registros de usuarios para procesar.")))
                    return@launch
                }

                val listaCredenciales = mutableListOf<CredencialUsuarioGenerada>()
                var exitoCount = 0
                var errorCount = 0

                val total = lineasDatos.size

                lineasDatos.forEachIndexed { index, linea ->
                    val columnas = linea.split(",").map { it.trim().removeSurrounding("\"") }
                    val indexCorreo = columnas.indexOfFirst { it.contains("@") }

                    if (indexCorreo >= 1 && columnas[0].isNotBlank()) {
                        val nombre = columnas.take(indexCorreo).filter { it.isNotBlank() }.joinToString(" ")
                        val correo = columnas[indexCorreo]
                        val strRol = if (columnas.size > indexCorreo + 1) columnas[indexCorreo + 1] else ""
                        val strClave = if (columnas.size > indexCorreo + 2) columnas[indexCorreo + 2] else ""

                        val rol = when {
                            strRol.contains("ADMIN", ignoreCase = true) || strRol.contains("DIRECTOR", ignoreCase = true) -> RolUsuario.ADMINISTRADOR_GENERAL
                            strRol.contains("BODEGA", ignoreCase = true) || strRol.contains("JEFE", ignoreCase = true) -> RolUsuario.JEFE_BODEGA
                            strRol.contains("SUPERVISOR", ignoreCase = true) -> RolUsuario.SUPERVISOR
                            else -> RolUsuario.TECNICO
                        }

                        val claveTemp = if (strClave.length >= 6) {
                            strClave
                        } else {
                            "ST2026!" + (1000..9999).random()
                        }

                        onProgreso(index + 1, total, correo)

                        val res = repositorioAutenticacion.crearUsuario(nombre, correo, claveTemp, rol)
                        if (res is Resultado.Exito) {
                            exitoCount++
                            listaCredenciales.add(
                                CredencialUsuarioGenerada(
                                    nombre = nombre,
                                    correo = correo,
                                    claveTemporal = claveTemp,
                                    rol = rol,
                                    exito = true
                                )
                            )
                        } else {
                            errorCount++
                            val msg = if (res is Resultado.Error) res.mensaje ?: res.excepcion.localizedMessage ?: "Error al registrar" else "Error desconocido"
                            listaCredenciales.add(
                                CredencialUsuarioGenerada(
                                    nombre = nombre,
                                    correo = correo,
                                    claveTemporal = claveTemp,
                                    rol = rol,
                                    exito = false,
                                    mensajeError = msg
                                )
                            )
                        }
                    } else {
                        errorCount++
                        listaCredenciales.add(
                            CredencialUsuarioGenerada(
                                nombre = if (columnas.isNotEmpty()) columnas[0] else "Fila inválida",
                                correo = if (indexCorreo >= 0) columnas[indexCorreo] else "Sin correo",
                                claveTemporal = "N/A",
                                rol = RolUsuario.TECNICO,
                                exito = false,
                                mensajeError = "Formato de fila inválido o sin correo electrónico"
                            )
                        )
                    }
                }

                // Exportación de credenciales a archivo CSV
                val nombreArchivo = "credenciales_usuarios_${System.currentTimeMillis()}.csv"
                val sbCsv = StringBuilder()
                sbCsv.append("Nombre,Correo,Contraseña Temporal,Rol,Estado,Detalle\n")
                listaCredenciales.forEach { c ->
                    val estado = if (c.exito) "Creado Exitosamente" else "Error"
                    val detalle = c.mensajeError ?: "OK"
                    sbCsv.append("\"${c.nombre}\",\"${c.correo}\",\"${c.claveTemporal}\",\"${c.rol.name}\",\"$estado\",\"$detalle\"\n")
                }

                val rutaArchivo = guardarYAlmacenarCsvCredenciales(context, nombreArchivo, sbCsv.toString())

                onResultado(
                    Resultado.Exito(
                        LoteResultadoCreacion(
                            totalProcesados = total,
                            exitoCount = exitoCount,
                            errorCount = errorCount,
                            rutaArchivoCredenciales = rutaArchivo,
                            listaCredenciales = listaCredenciales
                        )
                    )
                )
            } catch (e: Exception) {
                Log.e("AutenticacionViewModel", "Error en proceso de creación masiva: ${e.message}", e)
                onResultado(Resultado.Error(e, "Error al procesar lote CSV: ${e.message}"))
            }
        }
    }

    /**
     * Exporta una plantilla de ejemplo en CSV para la creación masiva de usuarios.
     */
    fun descargarPlantillaCsv(context: Context): String {
        val plantilla = "Nombre,Apellido,Correo,Rol,ContrasenaOpcional\n" +
                "Enrique,Rebollar,EnriqueR@stagetrack.com,Técnico,123456\n" +
                "Brandon,Benitez,BrandonB@stagetrack.com,Técnico,123456\n" +
                "David,Valdin,DavidV@stagetrack.com,Supervisor,123456\n" +
                "Alexis,Martinez,AlexisM@stagetrack.com,Jefe de bodega,123456\n" +
                "Axel,Rebollo,AxelR@stagetrack.com,,123456\n"

        return guardarYAlmacenarCsvCredenciales(context, "plantilla_importacion_usuarios.csv", plantilla)
    }

    private fun guardarYAlmacenarCsvCredenciales(context: Context, nombreArchivo: String, contenido: String): String {
        return try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/StageTrack_Reports")
            }

            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(contenido.toByteArray())
                }
                "Descargas/StageTrack_Reports/$nombreArchivo"
            } else {
                val archivo = File(context.cacheDir, nombreArchivo)
                FileOutputStream(archivo).use { it.write(contenido.toByteArray()) }
                archivo.absolutePath
            }
        } catch (e: Exception) {
            "cacheDir/$nombreArchivo"
        }
    }
}

