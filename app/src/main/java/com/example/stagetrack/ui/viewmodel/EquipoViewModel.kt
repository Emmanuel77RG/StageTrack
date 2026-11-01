package com.example.stagetrack.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.core.cripto.UtilidadHash
import com.example.stagetrack.core.imagen.UtilidadImagen
import com.example.stagetrack.datos.repositorio.RepositorioAlmacenamientoSupabase
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.EstadoEquipo
import com.example.stagetrack.dominio.modelo.NivelSeveridadDano
import com.example.stagetrack.dominio.modelo.ReporteDano
import com.example.stagetrack.dominio.modelo.TransferenciaCustodia
import com.example.stagetrack.dominio.repositorio.RepositorioAlmacenamiento
import com.example.stagetrack.dominio.repositorio.RepositorioCustodia
import com.example.stagetrack.dominio.repositorio.RepositorioEquipo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de la gestión del inventario, escaneo y transferencias en la arquitectura MVVM.
 */
class EquipoViewModel(
    private val repositorioEquipo: RepositorioEquipo,
    private val repositorioCustodia: RepositorioCustodia,
    private val repositorioAlmacenamiento: RepositorioAlmacenamiento = RepositorioAlmacenamientoSupabase()
) : ViewModel() {

    val listaEquipos: StateFlow<List<Equipo>> = repositorioEquipo.obtenerTodosLosEquipos()
        .catch { e ->
            Log.e("EquipoViewModel", "Error al obtener lista de equipos: ${e.message}", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val historialGlobalCustodia: StateFlow<List<TransferenciaCustodia>> = repositorioCustodia.obtenerHistorialGlobalCustodia()
        .catch { e ->
            Log.e("EquipoViewModel", "Error al obtener historial global de custodia: ${e.message}", e)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    fun registrarNuevoEquipo(
        context: Context? = null,
        nombre: String,
        categoria: String,
        numeroSerie: String,
        codigoQr: String,
        urlImagen: String? = null,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val idGenerado = "eq_${System.currentTimeMillis()}"
            var urlFinal: String? = urlImagen

            if (context != null && !urlImagen.isNullOrBlank()) {
                val bytes = UtilidadImagen.obtenerBytesImagen(context, urlImagen)
                if (bytes != null && bytes.isNotEmpty()) {
                    val resSubida = repositorioAlmacenamiento.subirFotoEquipo(idGenerado, bytes)
                    if (resSubida is Resultado.Exito) {
                        urlFinal = resSubida.datos
                        Log.d("EquipoViewModel", "Subida a Supabase exitosa: $urlFinal")
                    } else if (resSubida is Resultado.Error) {
                        Log.e("EquipoViewModel", "Subida a Supabase falló: ${resSubida.mensaje}. Usando respaldo para sincronización.", resSubida.excepcion)
                    }
                }
            }

            val nuevoEquipo = Equipo(
                id = idGenerado,
                codigoQR = codigoQr,
                nombre = nombre,
                categoria = categoria,
                numeroSerie = numeroSerie,
                estadoActual = EstadoEquipo.EN_BODEGA,
                urlImagen = urlFinal
            )
            val res = repositorioEquipo.registrarNuevoEquipo(nuevoEquipo)
            onResultado(res)
        }
    }

    fun actualizarEquipoExistente(
        context: Context? = null,
        equipoActualizado: Equipo,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            var equipoFinal = equipoActualizado
            val urlOriginal = equipoActualizado.urlImagen

            if (context != null && !urlOriginal.isNullOrBlank() && !urlOriginal.startsWith("http")) {
                val bytes = UtilidadImagen.obtenerBytesImagen(context, urlOriginal)
                if (bytes != null && bytes.isNotEmpty()) {
                    val resSubida = repositorioAlmacenamiento.subirFotoEquipo(equipoActualizado.id, bytes)
                    if (resSubida is Resultado.Exito) {
                        equipoFinal = equipoActualizado.copy(urlImagen = resSubida.datos)
                        Log.d("EquipoViewModel", "Foto de equipo actualizada en Supabase: ${resSubida.datos}")
                    } else if (resSubida is Resultado.Error) {
                        Log.e("EquipoViewModel", "Fallo al actualizar foto en Supabase: ${resSubida.mensaje}. Manteniendo respaldo.", resSubida.excepcion)
                    }
                }
            }

            val res = repositorioEquipo.actualizarEquipo(equipoFinal)
            onResultado(res)
        }
    }

    fun realizarTransferenciaCustodia(
        idEquipo: String,
        idEmisor: String,
        idReceptor: String,
        nombreReceptor: String,
        bytesFotografia: ByteArray,
        notas: String? = null,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val idTrans = "trans_${System.currentTimeMillis()}"
            repositorioAlmacenamiento.subirEvidenciaCustodia(idTrans, bytesFotografia)
            val base64Evidencia = android.util.Base64.encodeToString(bytesFotografia, android.util.Base64.NO_WRAP)
            val hashSha256 = UtilidadHash.calcularSha256(bytesFotografia)

            val transferencia = TransferenciaCustodia(
                id = idTrans,
                idEquipo = idEquipo,
                idUsuarioEmisor = idEmisor,
                idUsuarioReceptor = idReceptor,
                uriFotografiaEvidencia = base64Evidencia,
                hashSha256Evidencia = hashSha256,
                notas = notas
            )
            val resCustodia = repositorioCustodia.registrarTransferenciaCustodia(transferencia)
            if (resCustodia is Resultado.Exito) {
                repositorioEquipo.actualizarEstadoCustodia(
                    idEquipo = idEquipo,
                    nuevoEstado = EstadoEquipo.EN_TRANSITO,
                    idCustodio = idReceptor,
                    nombreCustodio = nombreReceptor
                )
            }
            onResultado(resCustodia)
        }
    }

    fun confirmarRecepcionCustodia(
        idEquipo: String,
        idUsuarioReceptor: String,
        nombreUsuarioReceptor: String,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioEquipo.actualizarEstadoCustodia(
                idEquipo = idEquipo,
                nuevoEstado = EstadoEquipo.EN_CUSTODIA,
                idCustodio = idUsuarioReceptor,
                nombreCustodio = nombreUsuarioReceptor
            )
            onResultado(res)
        }
    }

    fun registrarReporteDano(
        idEquipo: String,
        idUsuarioReportador: String,
        descripcionDano: String,
        severidad: NivelSeveridadDano,
        bytesFotografia: ByteArray,
        hashSha256: String,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val idReporte = "rep_${System.currentTimeMillis()}"
            val resSubida = repositorioAlmacenamiento.subirEvidenciaDano(idReporte, bytesFotografia)
            val urlEvidencia = if (resSubida is Resultado.Exito) resSubida.datos else ""

            val reporte = ReporteDano(
                id = idReporte,
                idEquipo = idEquipo,
                idUsuarioReportador = idUsuarioReportador,
                descripcionDano = descripcionDano,
                severidad = severidad,
                uriFotografiaEvidencia = urlEvidencia,
                hashSha256Evidencia = hashSha256
            )

            val resReporte = repositorioCustodia.registrarReporteDano(reporte)
            if (resReporte is Resultado.Exito) {
                val equipoActual = listaEquipos.value.find { it.id == idEquipo }
                repositorioEquipo.actualizarEstadoCustodia(
                    idEquipo = idEquipo,
                    nuevoEstado = EstadoEquipo.REPORTADO_DANADO,
                    idCustodio = equipoActual?.idCustodioActual ?: idUsuarioReportador,
                    nombreCustodio = equipoActual?.nombreCustodioActual
                )
            }
            onResultado(resReporte)
        }
    }

    fun obtenerReportesDanoPorEquipo(idEquipo: String): Flow<List<ReporteDano>> {
        return repositorioCustodia.obtenerReportesDanoPorEquipo(idEquipo)
    }

    fun iniciarDevolucionABodega(
        idEquipo: String,
        idTecnicoEmisor: String,
        nombreTecnicoEmisor: String,
        bytesFotografia: ByteArray,
        latitudGps: Double? = null,
        longitudGps: Double? = null,
        notas: String? = null,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val idTrans = "trans_${System.currentTimeMillis()}"
            repositorioAlmacenamiento.subirEvidenciaCustodia(idTrans, bytesFotografia)
            val base64Evidencia = android.util.Base64.encodeToString(bytesFotografia, android.util.Base64.NO_WRAP)
            val hashSha256 = UtilidadHash.calcularSha256(bytesFotografia)

            val transferencia = TransferenciaCustodia(
                id = idTrans,
                idEquipo = idEquipo,
                idUsuarioEmisor = idTecnicoEmisor,
                idUsuarioReceptor = "bodega",
                fechaHoraTimestamp = System.currentTimeMillis(),
                uriFotografiaEvidencia = base64Evidencia,
                hashSha256Evidencia = hashSha256,
                latitudGps = latitudGps,
                longitudGps = longitudGps,
                notas = notas
            )

            val resCustodia = repositorioCustodia.registrarTransferenciaCustodia(transferencia)
            if (resCustodia is Resultado.Exito) {
                repositorioEquipo.actualizarEstadoCustodia(
                    idEquipo = idEquipo,
                    nuevoEstado = EstadoEquipo.EN_PROCESO_DEVOLUCION,
                    idCustodio = idTecnicoEmisor,
                    nombreCustodio = nombreTecnicoEmisor
                )
            }
            onResultado(resCustodia)
        }
    }

    fun confirmarDevolucionEnBodega(
        idEquipo: String,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioEquipo.actualizarEstadoCustodia(
                idEquipo = idEquipo,
                nuevoEstado = EstadoEquipo.EN_BODEGA,
                idCustodio = null,
                nombreCustodio = null
            )
            onResultado(res)
        }
    }

    fun enviarAEnMantenimiento(
        idEquipo: String,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioEquipo.actualizarEstadoEquipo(
                idEquipo = idEquipo,
                nuevoEstado = EstadoEquipo.EN_MANTENIMIENTO
            )
            onResultado(res)
        }
    }

    fun finalizarMantenimientoYDevolverABodega(
        idEquipo: String,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioEquipo.actualizarEstadoCustodia(
                idEquipo = idEquipo,
                nuevoEstado = EstadoEquipo.EN_BODEGA,
                idCustodio = null,
                nombreCustodio = null
            )
            onResultado(res)
        }
    }

    fun devolverEquiposDeEventoABodega(
        idsEquipos: List<String>,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            idsEquipos.forEach { idEq ->
                repositorioEquipo.actualizarEstadoCustodia(
                    idEquipo = idEq,
                    nuevoEstado = EstadoEquipo.EN_BODEGA,
                    idCustodio = null,
                    nombreCustodio = null
                )
            }
            onResultado(Resultado.Exito(Unit))
        }
    }

    fun restablecerTodosLosEquiposABodega(
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val equiposActuales = listaEquipos.value
            equiposActuales.forEach { eq ->
                repositorioEquipo.actualizarEquipo(
                    eq.copy(
                        estadoActual = EstadoEquipo.EN_BODEGA,
                        idCustodioActual = null,
                        nombreCustodioActual = null,
                        ubicacionActual = "Bodega Principal"
                    )
                )
            }
            onResultado(Resultado.Exito(Unit))
        }
    }

    fun darDeBajaEquipo(
        idEquipo: String,
        motivoBaja: String,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioEquipo.actualizarEstadoEquipo(
                idEquipo = idEquipo,
                nuevoEstado = EstadoEquipo.DADO_DE_BAJA
            )
            onResultado(res)
        }
    }

    fun actualizarEstadoEquipo(
        idEquipo: String,
        nuevoEstado: EstadoEquipo,
        onResultado: (Resultado<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repositorioEquipo.actualizarEstadoEquipo(idEquipo, nuevoEstado)
            onResultado(res)
        }
    }
}
