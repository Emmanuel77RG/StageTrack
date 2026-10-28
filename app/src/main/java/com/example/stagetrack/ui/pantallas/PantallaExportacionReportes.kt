package com.example.stagetrack.ui.pantallas

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.TransferenciaCustodia
import com.example.stagetrack.ui.viewmodel.EquipoViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de Exportación de Reportes Ejecutivos para el Administrador General (Paso 5).
 * Permite generar y almacenar informes consolidados (CSV / Texto) directamente en Descargas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaExportacionReportes(
    equipoViewModel: EquipoViewModel,
    onVolverClick: () -> Unit
) {
    val context = LocalContext.current
    val equipos by equipoViewModel.listaEquipos.collectAsState()
    val historialGlobal by equipoViewModel.historialGlobalCustodia.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Reportes Ejecutivos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Almacenamiento y exportación de informes (CSV / Texto)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolverClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            TarjetaOpcionReporte(
                titulo = "Inventario Consolidado de Activos",
                descripcion = "Listado completo de equipos con su respectivo estado operativo, categoría, número de serie y ubicación. Almacena un archivo .csv en la carpeta Descargas.",
                icono = Icons.Default.Inventory2,
                etiquetaBoton = "Generar y Guardar Inventario (CSV)",
                onClick = {
                    exportarCsvInventario(context, equipos)
                }
            )

            TarjetaOpcionReporte(
                titulo = "Informe de Valoración de Activos",
                descripcion = "Resumen ejecutivo del estado logístico del inventario (En bodega, en custodia, dados de baja y averiados).",
                icono = Icons.Default.Assessment,
                etiquetaBoton = "Generar y Guardar Valoración (TXT)",
                onClick = {
                    exportarTextoValoracion(context, equipos)
                }
            )

            TarjetaOpcionReporte(
                titulo = "Reporte Global de Daños e Incidencias",
                descripcion = "Consolidado de equipos reportados con averías, sellos SHA-256 de evidencia y severidad registrada.",
                icono = Icons.Default.Warning,
                etiquetaBoton = "Generar y Guardar Reporte de Daños",
                onClick = {
                    exportarReporteDanados(context, equipos)
                }
            )

            TarjetaOpcionReporte(
                titulo = "Log Completo de Trazabilidad y Custodias",
                descripcion = "Historial inmutable de transferencias, cambios de custodios, coordenadas GPS y firmas criptográficas SHA-256.",
                icono = Icons.Default.Description,
                etiquetaBoton = "Generar y Guardar Trazabilidad (CSV)",
                onClick = {
                    exportarCsvTrazabilidad(context, historialGlobal, equipos)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TarjetaOpcionReporte(
    titulo: String,
    descripcion: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    etiquetaBoton: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(etiquetaBoton, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun exportarCsvInventario(context: Context, equipos: List<Equipo>) {
    val sb = StringBuilder()
    sb.append("ID,Nombre,Categoria,NumeroSerie,CodigoQR,EstadoActual,UbicacionActual,CustodioActual\n")
    equipos.forEach { eq ->
        sb.append("\"${eq.id}\",\"${eq.nombre}\",\"${eq.categoria}\",\"${eq.numeroSerie}\",\"${eq.codigoQR}\",\"${eq.estadoActual}\",\"${eq.ubicacionActual}\",\"${eq.nombreCustodioActual ?: "Ninguno"}\"\n")
    }
    guardarYAlmacenarReporte(context, "inventario_stagetrack_${System.currentTimeMillis()}.csv", sb.toString())
}

private fun exportarTextoValoracion(context: Context, equipos: List<Equipo>) {
    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
    val total = equipos.size
    val enBodega = equipos.count { it.estadoActual.name == "EN_BODEGA" }
    val enCustodia = equipos.count { it.estadoActual.name == "EN_CUSTODIA" || it.estadoActual.name == "EN_TRANSITO" }
    val danados = equipos.count { it.estadoActual.name == "REPORTADO_DANADO" }
    val dadosBaja = equipos.count { it.estadoActual.name == "DADO_DE_BAJA" }

    val sb = StringBuilder()
    sb.append("=== INFORME DE VALORACIÓN Y ESTADO DE ACTIVOS - STAGETRACK ===\n")
    sb.append("Fecha de emisión: $fecha\n\n")
    sb.append("• Total de Activos Registrados: $total\n")
    sb.append("• Equipos en Bodega (Disponibles): $enBodega\n")
    sb.append("• Equipos en Custodia / Tránsito: $enCustodia\n")
    sb.append("• Equipos Reportados Dañados: $danados\n")
    sb.append("• Equipos Dados de Baja (Archivados): $dadosBaja\n\n")
    sb.append("Desglose por Categoría:\n")
    equipos.groupBy { it.categoria }.forEach { (cat, lista) ->
        sb.append(" - $cat: ${lista.size} activos\n")
    }

    guardarYAlmacenarReporte(context, "valoracion_activos_${System.currentTimeMillis()}.txt", sb.toString())
}

private fun exportarReporteDanados(context: Context, equipos: List<Equipo>) {
    val danados = equipos.filter { it.estadoActual.name == "REPORTADO_DANADO" }
    val sb = StringBuilder()
    sb.append("ID,Nombre,Categoria,NumeroSerie,Ubicacion,CustodioResponsable\n")
    danados.forEach { eq ->
        sb.append("\"${eq.id}\",\"${eq.nombre}\",\"${eq.categoria}\",\"${eq.numeroSerie}\",\"${eq.ubicacionActual}\",\"${eq.nombreCustodioActual ?: "N/D"}\"\n")
    }
    guardarYAlmacenarReporte(context, "reporte_global_danos_${System.currentTimeMillis()}.csv", sb.toString())
}

private fun exportarCsvTrazabilidad(context: Context, transferencias: List<TransferenciaCustodia>, equipos: List<Equipo>) {
    val mapaEquipos = equipos.associateBy { it.id }
    val sb = StringBuilder()
    sb.append("ID_Transferencia,Equipo,Emisor,Receptor,FechaHora,GPS,Hash_SHA256\n")
    transferencias.forEach { t ->
        val eq = mapaEquipos[t.idEquipo]?.nombre ?: t.idEquipo
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(t.fechaHoraTimestamp))
        val gps = if (t.latitudGps != null && t.longitudGps != null) "${t.latitudGps},${t.longitudGps}" else "N/A"
        sb.append("\"${t.id}\",\"$eq\",\"${t.idUsuarioEmisor}\",\"${t.idUsuarioReceptor}\",\"$fecha\",\"$gps\",\"${t.hashSha256Evidencia}\"\n")
    }
    guardarYAlmacenarReporte(context, "log_trazabilidad_${System.currentTimeMillis()}.csv", sb.toString())
}

private fun guardarYAlmacenarReporte(context: Context, nombreArchivo: String, contenido: String) {
    try {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
            put(MediaStore.MediaColumns.MIME_TYPE, if (nombreArchivo.endsWith(".csv")) "text/csv" else "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/StageTrack_Reports")
        }

        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(contenido.toByteArray())
            }
            Toast.makeText(
                context,
                "¡Reporte guardado en Descargas/StageTrack_Reports/$nombreArchivo!",
                Toast.LENGTH_LONG
            ).show()
        } else {
            // Fallback a almacenamiento interno si MediaStore devuelve null
            val archivo = File(context.cacheDir, nombreArchivo)
            FileOutputStream(archivo).use { it.write(contenido.toByteArray()) }

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                archivo
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Exportar Reporte Ejecutivo"))
            Toast.makeText(context, "Reporte exportado: $nombreArchivo", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al guardar el reporte: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}
