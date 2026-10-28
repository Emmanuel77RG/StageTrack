package com.example.stagetrack

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.stagetrack.datos.local.basedatos.BaseDatosStageTrack
import com.example.stagetrack.datos.repositorio.RepositorioAlmacenamientoSupabase
import com.example.stagetrack.datos.repositorio.RepositorioAutenticacionFirebase
import com.example.stagetrack.datos.repositorio.RepositorioCustodiaFirestore
import com.example.stagetrack.datos.repositorio.RepositorioEquipoFirestore
import com.example.stagetrack.datos.repositorio.RepositorioEventoFirestore
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.EstadoEquipo
import com.example.stagetrack.dominio.modelo.EstadoEvento
import com.example.stagetrack.dominio.modelo.Evento
import com.example.stagetrack.ui.navegacion.NavegacionApp
import com.example.stagetrack.ui.theme.StageTrackTheme
import com.example.stagetrack.ui.viewmodel.AutenticacionViewModel
import com.example.stagetrack.ui.viewmodel.EquipoViewModel
import com.example.stagetrack.ui.viewmodel.EventoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val baseDatos by lazy {
        BaseDatosStageTrack.obtenerInstancia(applicationContext)
    }

    private val repositorioAutenticacion by lazy {
        RepositorioAutenticacionFirebase(
            context = applicationContext,
            daoUsuario = baseDatos.daoUsuario()
        )
    }

    private val repositorioAlmacenamiento by lazy {
        RepositorioAlmacenamientoSupabase()
    }

    private val repositorioEquipo by lazy {
        RepositorioEquipoFirestore(baseDatos.daoEquipo())
    }

    private val repositorioCustodia by lazy {
        RepositorioCustodiaFirestore(baseDatos.daoCustodia(), baseDatos.daoReporteDano())
    }

    private val repositorioEvento by lazy {
        RepositorioEventoFirestore(baseDatos.daoEvento())
    }

    private val autenticacionViewModel: AutenticacionViewModel by viewModels {
        crearViewModelFactory()
    }

    private val equipoViewModel: EquipoViewModel by viewModels {
        crearViewModelFactory()
    }

    private val eventoViewModel: EventoViewModel by viewModels {
        crearViewModelFactory()
    }

    private fun crearViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(AutenticacionViewModel::class.java) -> {
                        AutenticacionViewModel(repositorioAutenticacion, repositorioAlmacenamiento) as T
                    }
                    modelClass.isAssignableFrom(EquipoViewModel::class.java) -> {
                        EquipoViewModel(repositorioEquipo, repositorioCustodia) as T
                    }
                    modelClass.isAssignableFrom(EventoViewModel::class.java) -> {
                        EventoViewModel(repositorioEvento) as T
                    }
                    else -> throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Poblado inicial de datos en la base de datos local Room si está vacía
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val equiposExistentes = repositorioEquipo.obtenerTodosLosEquipos().firstOrNull() ?: emptyList()
                if (equiposExistentes.isEmpty()) {
                    repositorioEquipo.registrarNuevoEquipo(
                        Equipo(
                            id = "eq_01",
                            codigoQR = "QR-AUDIO-CONSOLE-X32",
                            nombre = "Consola Behringer X32",
                            categoria = "Audio",
                            numeroSerie = "SN-982134",
                            estadoActual = EstadoEquipo.EN_BODEGA,
                            ubicacionActual = "Bodega Principal"
                        )
                    )
                    repositorioEquipo.registrarNuevoEquipo(
                        Equipo(
                            id = "eq_02",
                            codigoQR = "QR-LIGHT-ROBOTIC-ROBE",
                            nombre = "Luminaria Robótica Robe Pointe",
                            categoria = "Iluminación",
                            numeroSerie = "SN-443120",
                            estadoActual = EstadoEquipo.EN_CUSTODIA,
                            idCustodioActual = "usr_tecnico_01",
                            nombreCustodioActual = "Carlos Rodríguez",
                            ubicacionActual = "Escenario Principal"
                        )
                    )
                    repositorioEquipo.registrarNuevoEquipo(
                        Equipo(
                            id = "eq_03",
                            codigoQR = "QR-LED-SCREEN-P3",
                            nombre = "Pantalla LED P3.9 (Gabinete 50x50)",
                            categoria = "Video",
                            numeroSerie = "SN-771239",
                            estadoActual = EstadoEquipo.EN_TRANSITO,
                            ubicacionActual = "Camión de Flete 2"
                        )
                    )
                }

                val eventosExistentes = repositorioEvento.obtenerTodosLosEventos().firstOrNull() ?: emptyList()
                if (eventosExistentes.isEmpty()) {
                    val horaActual = System.currentTimeMillis()
                    repositorioEvento.crearNuevoEvento(
                        Evento(
                            id = "evt_01",
                            nombre = "Concierto Auditorio Nacional 2026",
                            cliente = "OCESA Producciones",
                            fechaInicio = horaActual,
                            fechaFin = horaActual + (2 * 24 * 60 * 60 * 1000L),
                            ubicacion = "Auditorio Nacional, CDMX",
                            idSupervisor = "usr_supervisor_01",
                            nombreSupervisor = "Ana Gómez",
                            idsTecnicosAsignados = listOf("usr_tecnico_01"),
                            estado = EstadoEvento.PLANIFICADO
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error durante la inicialización de datos de prueba en Room: ${e.message}", e)
            }
        }

        setContent {
            StageTrackTheme {
                NavegacionApp(
                    autenticacionViewModel = autenticacionViewModel,
                    equipoViewModel = equipoViewModel,
                    eventoViewModel = eventoViewModel
                )
            }
        }
    }
}
