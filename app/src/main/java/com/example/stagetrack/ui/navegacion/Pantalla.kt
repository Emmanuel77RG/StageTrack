package com.example.stagetrack.ui.navegacion

import com.example.stagetrack.dominio.modelo.EstadoEquipo
import com.example.stagetrack.dominio.modelo.ModoEscaneoQr

/**
 * Destinos y rutas de navegación en la aplicación StageTrack.
 */
sealed class Pantalla(val ruta: String) {
    data object InicioSesion : Pantalla("inicio_sesion")
    data object GestionUsuarios : Pantalla("gestion_usuarios")
    data class InventarioEquipos(val filtroEstadoInicial: EstadoEquipo? = null) : Pantalla("inventario_equipos")
    data object TransferenciaCustodia : Pantalla("transferencia_custodia")
    data object InicioBodega : Pantalla("inicio_bodega")
    data object InicioTecnico : Pantalla("inicio_tecnico")
    data object InicioSupervisor : Pantalla("inicio_supervisor")
    data class EscaneoQr(val modo: ModoEscaneoQr = ModoEscaneoQr.CONSULTAR_FICHA) : Pantalla("escaneo_qr")
    data object ReporteDano : Pantalla("reporte_dano")
    data object GestionEventos : Pantalla("gestion_eventos")
    data object PerfilUsuario : Pantalla("perfil_usuario")
    data object PrimerInicioSesion : Pantalla("primer_inicio_sesion")
    data object AceptarCustodia : Pantalla("aceptar_custodia")
    data class InventarioEvento(val idEvento: String) : Pantalla("inventario_evento")
    data object AuditoriaGlobal : Pantalla("auditoria_global")
    data object ExportacionReportes : Pantalla("exportacion_reportes")
}
