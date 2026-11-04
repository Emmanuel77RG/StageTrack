package com.example.stagetrack.ui.navegacion

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.EstadoEquipo
import com.example.stagetrack.dominio.modelo.ModoEscaneoQr
import com.example.stagetrack.dominio.modelo.RolUsuario
import com.example.stagetrack.ui.componentes.DialogoFichaTecnicaEquipo
import com.example.stagetrack.ui.pantallas.PantallaAceptarCustodia
import com.example.stagetrack.ui.pantallas.PantallaAuditoriaGlobal
import com.example.stagetrack.ui.pantallas.PantallaDashboardPrincipal
import com.example.stagetrack.ui.pantallas.PantallaExportacionReportes
import com.example.stagetrack.ui.pantallas.PantallaEscaneoQr
import com.example.stagetrack.ui.pantallas.PantallaGestionEventos
import com.example.stagetrack.ui.pantallas.PantallaDetalleEvento
import com.example.stagetrack.ui.pantallas.PantallaGestionUsuarios
import com.example.stagetrack.ui.pantallas.PantallaInicioSesion
import com.example.stagetrack.ui.pantallas.PantallaInventarioEvento
import com.example.stagetrack.ui.pantallas.PantallaInventarioEquipos
import com.example.stagetrack.ui.pantallas.PantallaPerfilUsuario
import com.example.stagetrack.ui.pantallas.PantallaPrimerInicioSesion
import com.example.stagetrack.ui.pantallas.PantallaReporteDano
import com.example.stagetrack.ui.pantallas.PantallaTransferenciaCustodia
import com.example.stagetrack.ui.viewmodel.AutenticacionViewModel
import com.example.stagetrack.ui.viewmodel.EquipoViewModel
import com.example.stagetrack.ui.viewmodel.EventoViewModel

/**
 * Componente principal de navegación y control de acceso por rol de usuario.
 */
@Composable
fun NavegacionApp(
    autenticacionViewModel: AutenticacionViewModel,
    equipoViewModel: EquipoViewModel,
    eventoViewModel: EventoViewModel
) {
    val usuarioActual by autenticacionViewModel.usuarioActual.collectAsState()
    val listaUsuarios by autenticacionViewModel.listaUsuarios.collectAsState()
    val equipos by equipoViewModel.listaEquipos.collectAsState()
    val eventos by eventoViewModel.listaEventos.collectAsState()

    LaunchedEffect(usuarioActual) {
        eventoViewModel.establecerUsuarioActual(usuarioActual)
    }

    var pantallaActual by remember { mutableStateOf<Pantalla>(Pantalla.InicioSesion) }
    var equipoSeleccionadoParaAccion by remember { mutableStateOf<Equipo?>(null) }
    var equipoParaFichaTecnica by remember { mutableStateOf<Equipo?>(null) }
    var qrVerificadoEnEscaneo by remember { mutableStateOf(false) }

    // Interceptar el botón o gesto de regresar del dispositivo
    BackHandler(enabled = usuarioActual != null && pantallaActual !is Pantalla.InicioBodega) {
        when (pantallaActual) {
            is Pantalla.TransferenciaCustodia, is Pantalla.ReporteDano -> {
                pantallaActual = Pantalla.InventarioEquipos()
            }
            is Pantalla.InventarioEvento -> {
                pantallaActual = Pantalla.GestionEventos
            }
            else -> {
                pantallaActual = Pantalla.InicioBodega
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (usuarioActual == null) {
            PantallaInicioSesion(
                onIniciarSesionClick = { correo, clave, onResultado ->
                    autenticacionViewModel.iniciarSesion(correo, clave, onResultado)
                }
            )
        } else if (usuarioActual!!.esPrimerInicio) {
            val context = LocalContext.current
            PantallaPrimerInicioSesion(
                usuario = usuarioActual!!,
                onEnviarVerificacionCorreo = { correo, onResultado ->
                    autenticacionViewModel.enviarVerificacionCorreoRecuperacion(correo, onResultado)
                },
                onCompletarPrimerInicio = { claveTemp, nuevaClave, correoRec, uriFoto, rot, esc, offX, offY, tVista, onResultado ->
                    autenticacionViewModel.completarPrimerInicio(
                        context = context,
                        idUsuario = usuarioActual!!.id,
                        claveTemporal = claveTemp,
                        nuevaClave = nuevaClave,
                        correoRecuperacion = correoRec,
                        uriFotoLocal = uriFoto,
                        rotacionFoto = rot,
                        escalaFoto = esc,
                        offsetXFoto = offX,
                        offsetYFoto = offY,
                        tamanoVistaPxFoto = tVista,
                        onResultado = onResultado
                    )
                },
                onCerrarSesion = {
                    autenticacionViewModel.cerrarSesion()
                    pantallaActual = Pantalla.InicioSesion
                }
            )
        } else {
            when (val actual = pantallaActual) {
                is Pantalla.GestionUsuarios -> {
                    if (usuarioActual?.rol == RolUsuario.ADMINISTRADOR_GENERAL) {
                        val context = LocalContext.current
                        PantallaGestionUsuarios(
                            usuarios = listaUsuarios,
                            usuarioActualId = usuarioActual?.id ?: "",
                            onVolverClick = { pantallaActual = Pantalla.InicioBodega },
                            onCrearUsuario = { nombre, correo, clave, rol, onResultado ->
                                autenticacionViewModel.crearUsuario(nombre, correo, clave, rol, onResultado)
                            },
                            onEditarUsuario = { idUsuario, nombre, correo, rol, onResultado ->
                                autenticacionViewModel.actualizarDatosUsuario(idUsuario, nombre, correo, rol, onResultado)
                            },
                            onRestablecerContrasena = { idUsuario, nuevaClave, onResultado ->
                                autenticacionViewModel.restablecerContrasena(idUsuario, nuevaClave, onResultado)
                            },
                            onCambiarEstadoActivo = { idUsuario, activo, onResultado ->
                                autenticacionViewModel.cambiarEstadoActivo(idUsuario, activo, onResultado)
                            },
                            onSubirFotoPerfil = { idUsuario, uri, rot, esc, offX, offY, tVista, onResultado ->
                                autenticacionViewModel.subirYActualizarFotoPerfil(
                                    context = context,
                                    idUsuario = idUsuario,
                                    uriImagen = uri,
                                    rotacionGrados = rot,
                                    escala = esc,
                                    offsetX = offX,
                                    offsetY = offY,
                                    tamanoVistaPx = tVista,
                                    onResultado = onResultado
                                )
                            },
                            onSincronizarNube = { onResultado ->
                                autenticacionViewModel.sincronizarUsuariosConNube(onResultado)
                            },
                            onCrearUsuariosEnLoteCsv = { ctx, contenido, onProgreso, onResultado ->
                                autenticacionViewModel.crearUsuariosEnLoteCsv(ctx, contenido, onProgreso, onResultado)
                            },
                            onDescargarPlantillaCsv = { ctx ->
                                autenticacionViewModel.descargarPlantillaCsv(ctx)
                            }
                        )
                    } else {
                        pantallaActual = Pantalla.InicioBodega
                    }
                }

                is Pantalla.AuditoriaGlobal -> {
                    if (usuarioActual?.rol == RolUsuario.ADMINISTRADOR_GENERAL) {
                        PantallaAuditoriaGlobal(
                            equipoViewModel = equipoViewModel,
                            listaUsuarios = listaUsuarios,
                            onVolverClick = { pantallaActual = Pantalla.InicioBodega }
                        )
                    } else {
                        pantallaActual = Pantalla.InicioBodega
                    }
                }

                is Pantalla.ExportacionReportes -> {
                    if (usuarioActual?.rol == RolUsuario.ADMINISTRADOR_GENERAL) {
                        PantallaExportacionReportes(
                            equipoViewModel = equipoViewModel,
                            onVolverClick = { pantallaActual = Pantalla.InicioBodega }
                        )
                    } else {
                        pantallaActual = Pantalla.InicioBodega
                    }
                }


                is Pantalla.InventarioEquipos -> {
                    PantallaInventarioEquipos(
                        equipoViewModel = equipoViewModel,
                        eventoViewModel = eventoViewModel,
                        usuarioActual = usuarioActual,
                        filtroEstadoInicial = actual.filtroEstadoInicial,
                        onVolverClick = { pantallaActual = Pantalla.InicioBodega },
                        onTransferirCustodiaClick = { eq ->
                            equipoSeleccionadoParaAccion = eq
                            val esAceptacionPendiente = (eq.estadoActual == EstadoEquipo.EN_TRANSITO) && usuarioActual?.rol == RolUsuario.TECNICO
                            qrVerificadoEnEscaneo = false
                            pantallaActual = if (esAceptacionPendiente) Pantalla.AceptarCustodia else Pantalla.TransferenciaCustodia
                        },
                        onReportarDanoClick = { eq ->
                            equipoSeleccionadoParaAccion = eq
                            pantallaActual = Pantalla.ReporteDano
                        }
                    )
                }

                is Pantalla.InventarioEvento -> {
                    val eventoObjetivo = eventos.find { it.id == actual.idEvento }
                    if (eventoObjetivo != null) {
                        PantallaInventarioEvento(
                            evento = eventoObjetivo,
                            equipoViewModel = equipoViewModel,
                            usuarioActual = usuarioActual,
                            onVolverClick = { pantallaActual = Pantalla.GestionEventos },
                            onTransferirCustodiaClick = { eq ->
                                equipoSeleccionadoParaAccion = eq
                                pantallaActual = Pantalla.TransferenciaCustodia
                            },
                            onReportarDanoClick = { eq ->
                                equipoSeleccionadoParaAccion = eq
                                pantallaActual = Pantalla.ReporteDano
                            }
                        )
                    } else {
                        pantallaActual = Pantalla.GestionEventos
                    }
                }

                is Pantalla.TransferenciaCustodia -> {
                    val equipoObjetivo = equipoSeleccionadoParaAccion ?: equipos.firstOrNull()
                    if (equipoObjetivo != null) {
                        PantallaTransferenciaCustodia(
                            equipo = equipoObjetivo,
                            autenticacionViewModel = autenticacionViewModel,
                            equipoViewModel = equipoViewModel,
                            eventoViewModel = eventoViewModel,
                            onVolverClick = { pantallaActual = Pantalla.InventarioEquipos() },
                            onTransferenciaExitosa = { pantallaActual = Pantalla.InventarioEquipos() }
                        )
                    } else {
                        pantallaActual = Pantalla.InventarioEquipos()
                    }
                }

                is Pantalla.ReporteDano -> {
                    val equipoObjetivo = equipoSeleccionadoParaAccion ?: equipos.firstOrNull()
                    if (equipoObjetivo != null) {
                        PantallaReporteDano(
                            equipo = equipoObjetivo,
                            autenticacionViewModel = autenticacionViewModel,
                            equipoViewModel = equipoViewModel,
                            onVolverClick = { pantallaActual = Pantalla.InventarioEquipos() },
                            onReporteExitoso = { pantallaActual = Pantalla.InventarioEquipos() }
                        )
                    } else {
                        pantallaActual = Pantalla.InventarioEquipos()
                    }
                }

                is Pantalla.EscaneoQr -> {
                    PantallaEscaneoQr(
                        modo = actual.modo,
                        equipos = equipos,
                        onVolverClick = { pantallaActual = Pantalla.InicioBodega },
                        onQrEscaneado = { codigoQr ->
                            val equipoEncontrado = equipos.find {
                                it.codigoQR.equals(codigoQr, ignoreCase = true) ||
                                it.numeroSerie.equals(codigoQr, ignoreCase = true) ||
                                it.id.equals(codigoQr, ignoreCase = true)
                            }
                            if (equipoEncontrado != null) {
                                equipoSeleccionadoParaAccion = equipoEncontrado

                                if (usuarioActual?.rol == RolUsuario.TECNICO) {
                                    val esDestinatarioTransito = equipoEncontrado.estadoActual == EstadoEquipo.EN_TRANSITO &&
                                            equipoEncontrado.idCustodioActual == usuarioActual?.id

                                    val esCustodioActivo = equipoEncontrado.estadoActual == EstadoEquipo.EN_CUSTODIA &&
                                            equipoEncontrado.idCustodioActual == usuarioActual?.id

                                    when {
                                        esDestinatarioTransito -> {
                                            qrVerificadoEnEscaneo = true
                                            pantallaActual = Pantalla.AceptarCustodia
                                        }
                                        esCustodioActivo -> {
                                            pantallaActual = Pantalla.TransferenciaCustodia
                                        }
                                        else -> {
                                            // Si no es el destinatario en tránsito ni el custodio activo, solo muestra la Ficha Técnica
                                            equipoParaFichaTecnica = equipoEncontrado
                                            pantallaActual = Pantalla.InicioBodega
                                        }
                                    }
                                } else {
                                    // Para roles de Gestión (Supervisor, Jefe de Bodega, Administrador)
                                    when (actual.modo) {
                                        ModoEscaneoQr.CONSULTAR_FICHA -> {
                                            equipoParaFichaTecnica = equipoEncontrado
                                            pantallaActual = Pantalla.InicioBodega
                                        }
                                        ModoEscaneoQr.RECEPCION_CUSTODIA -> {
                                            val esAceptacionPendiente = (equipoEncontrado.estadoActual == EstadoEquipo.EN_TRANSITO)
                                            qrVerificadoEnEscaneo = true
                                            pantallaActual = if (esAceptacionPendiente) Pantalla.AceptarCustodia else Pantalla.TransferenciaCustodia
                                        }
                                        ModoEscaneoQr.DESPACHO_TRASPASO -> {
                                            pantallaActual = Pantalla.TransferenciaCustodia
                                        }
                                    }
                                }
                            } else {
                                pantallaActual = Pantalla.InventarioEquipos()
                            }
                        }
                    )
                }

                is Pantalla.AceptarCustodia -> {
                    val equipoObjetivo = equipoSeleccionadoParaAccion ?: equipos.firstOrNull()
                    if (equipoObjetivo != null && usuarioActual != null) {
                        PantallaAceptarCustodia(
                            equipo = equipoObjetivo,
                            usuarioActual = usuarioActual!!,
                            equipoViewModel = equipoViewModel,
                            qrVerificadoInicial = qrVerificadoEnEscaneo,
                            onVolverClick = { pantallaActual = Pantalla.InventarioEquipos() },
                            onRecepcionExitosa = {
                                qrVerificadoEnEscaneo = false
                                pantallaActual = Pantalla.InventarioEquipos()
                            }
                        )
                    } else {
                        pantallaActual = Pantalla.InventarioEquipos()
                    }
                }

                is Pantalla.GestionEventos -> {
                    PantallaGestionEventos(
                        eventoViewModel = eventoViewModel,
                        autenticacionViewModel = autenticacionViewModel,
                        equipoViewModel = equipoViewModel,
                        onVolverClick = { pantallaActual = Pantalla.InicioBodega },
                        onVerInventarioEventoClick = { evento ->
                            pantallaActual = Pantalla.InventarioEvento(evento.id)
                        },
                        onVerDetalleEventoClick = { evento ->
                            pantallaActual = Pantalla.DetalleEvento(evento.id)
                        }
                    )
                }

                is Pantalla.DetalleEvento -> {
                    PantallaDetalleEvento(
                        idEvento = actual.idEvento,
                        eventoViewModel = eventoViewModel,
                        autenticacionViewModel = autenticacionViewModel,
                        equipoViewModel = equipoViewModel,
                        onVolverClick = { pantallaActual = Pantalla.GestionEventos },
                        onVerInventarioEventoClick = { evento ->
                            pantallaActual = Pantalla.InventarioEvento(evento.id)
                        }
                    )
                }

                is Pantalla.PerfilUsuario -> {
                    if (usuarioActual != null) {
                        val context = LocalContext.current
                        PantallaPerfilUsuario(
                            usuario = usuarioActual!!,
                            onVolverClick = { pantallaActual = Pantalla.InicioBodega },
                            onSubirFotoPerfil = { uri, rot, esc, offX, offY, tVista, onResultado ->
                                autenticacionViewModel.subirYActualizarFotoPerfil(
                                    context = context,
                                    idUsuario = usuarioActual!!.id,
                                    uriImagen = uri,
                                    rotacionGrados = rot,
                                    escala = esc,
                                    offsetX = offX,
                                    offsetY = offY,
                                    tamanoVistaPx = tVista,
                                    onResultado = onResultado
                                )
                            },
                            onGuardarCorreoRecuperacion = { correo, onResultado ->
                                autenticacionViewModel.actualizarCorreoRecuperacion(
                                    idUsuario = usuarioActual!!.id,
                                    correoRecuperacion = correo,
                                    onResultado = onResultado
                                )
                            },
                            onCambiarContrasena = { claveActual, nuevaClave, onResultado ->
                                autenticacionViewModel.cambiarContrasenaConVerificacion(
                                    idUsuario = usuarioActual!!.id,
                                    claveActual = claveActual,
                                    nuevaClave = nuevaClave,
                                    onResultado = onResultado
                                )
                            }
                        )
                    } else {
                        pantallaActual = Pantalla.InicioSesion
                    }
                }

                else -> {
                    usuarioActual?.let { uActual ->
                        PantallaDashboardPrincipal(
                            usuarioActual = uActual,
                            equipos = equipos,
                            eventos = eventos,
                            listaUsuarios = listaUsuarios,
                            equipoViewModel = equipoViewModel,
                            onNavegarInventario = { filtro -> pantallaActual = Pantalla.InventarioEquipos(filtro) },
                            onNavegarEventos = { pantallaActual = Pantalla.GestionEventos },
                            onNavegarEscaneoQr = { modo -> pantallaActual = Pantalla.EscaneoQr(modo) },
                            onNavegarGestionUsuarios = { pantallaActual = Pantalla.GestionUsuarios },
                            onNavegarAuditoriaGlobal = { pantallaActual = Pantalla.AuditoriaGlobal },
                            onNavegarExportacionReportes = { pantallaActual = Pantalla.ExportacionReportes },
                            onNavegarPerfil = { pantallaActual = Pantalla.PerfilUsuario },

                            onNavegarTransferencia = { eq ->
                                equipoSeleccionadoParaAccion = eq
                                val esAceptacionPendiente = (eq.estadoActual == EstadoEquipo.EN_TRANSITO) && usuarioActual?.rol == RolUsuario.TECNICO
                                qrVerificadoEnEscaneo = false
                                pantallaActual = if (esAceptacionPendiente) Pantalla.AceptarCustodia else Pantalla.TransferenciaCustodia
                            },
                            onNavegarReporteDano = { eq ->
                                equipoSeleccionadoParaAccion = eq
                                pantallaActual = Pantalla.ReporteDano
                            },
                            onNavegarFichaTecnica = { eq ->
                                equipoParaFichaTecnica = eq
                            },
                            onCerrarSesion = {
                                autenticacionViewModel.cerrarSesion()
                                pantallaActual = Pantalla.InicioSesion
                            }
                        )
                    }
                }
            }
        }

        equipoParaFichaTecnica?.let { eq ->
            DialogoFichaTecnicaEquipo(
                equipo = eq,
                usuarioActual = usuarioActual,
                onDescartar = { equipoParaFichaTecnica = null },
                onTransferirCustodiaClick = {
                    equipoSeleccionadoParaAccion = eq
                    equipoParaFichaTecnica = null
                    pantallaActual = Pantalla.TransferenciaCustodia
                },
                onReportarDanoClick = {
                    equipoSeleccionadoParaAccion = eq
                    equipoParaFichaTecnica = null
                    pantallaActual = Pantalla.ReporteDano
                },
                onDarDeBajaClick = {
                    val eqTarget = eq
                    equipoParaFichaTecnica = null
                    equipoViewModel.darDeBajaEquipo(eqTarget.id, "Baja desde Ficha Técnica") { _ -> }
                }
            )
        }
    }
}
