package com.example.stagetrack.ui.pantallas

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.core.content.FileProvider
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.TransferenciaCustodia
import com.example.stagetrack.ui.viewmodel.EquipoViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de Exportación de Reportes Ejecutivos para el Administrador General.
 * Permite generar y almacenar informes consolidados en formatos CSV/TXT y PDF
 * directamente en la carpeta Descargas/StageTrack_Reports.
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
                            text = "Exportación de informes consolidados (CSV, TXT y PDF)",
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
                descripcion = "Listado completo de equipos con su estado operativo, categoría, número de serie y ubicación.",
                icono = Icons.Default.Inventory2,
                etiquetaTextoCsv = "Guardar CSV",
                onExportarCsvTxt = { exportarCsvInventario(context, equipos) },
                onExportarPdf = { exportarPdfInventario(context, equipos) }
            )

            TarjetaOpcionReporte(
                titulo = "Informe de Valoración de Activos",
                descripcion = "Resumen ejecutivo del estado logístico del inventario (Bodega, Custodia, Averiados y Dados de Baja).",
                icono = Icons.Default.Assessment,
                etiquetaTextoCsv = "Guardar TXT",
                onExportarCsvTxt = { exportarTextoValoracion(context, equipos) },
                onExportarPdf = { exportarPdfValoracion(context, equipos) }
            )

            TarjetaOpcionReporte(
                titulo = "Reporte Global de Daños e Incidencias",
                descripcion = "Consolidado de equipos reportados con averías, sellos SHA-256 de evidencia y custodios responsables.",
                icono = Icons.Default.Warning,
                etiquetaTextoCsv = "Guardar CSV",
                onExportarCsvTxt = { exportarReporteDanados(context, equipos) },
                onExportarPdf = { exportarPdfDanados(context, equipos) }
            )

            TarjetaOpcionReporte(
                titulo = "Log Completo de Trazabilidad y Custodias",
                descripcion = "Historial inmutable de transferencias, cambios de custodios, coordenadas GPS y firmas criptográficas SHA-256.",
                icono = Icons.Default.Description,
                etiquetaTextoCsv = "Guardar CSV",
                onExportarCsvTxt = { exportarCsvTrazabilidad(context, historialGlobal, equipos) },
                onExportarPdf = { exportarPdfTrazabilidad(context, historialGlobal, equipos) }
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
    etiquetaTextoCsv: String,
    onExportarCsvTxt: () -> Unit,
    onExportarPdf: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onExportarCsvTxt,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(etiquetaTextoCsv, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onExportarPdf,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exportar PDF", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== FUNCIONES EXPORTACIÓN CSV / TXT ====================

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

// ==================== FUNCIONES EXPORTACIÓN PDF ====================

private fun exportarPdfInventario(context: Context, equipos: List<Equipo>) {
    val pdfDocument = PdfDocument()
    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val paintTitle = Paint().apply {
        textSize = 16f
        isFakeBoldText = true
        color = Color.rgb(30, 41, 59)
    }
    val paintSub = Paint().apply {
        textSize = 10f
        color = Color.rgb(100, 116, 139)
    }
    val paintHeader = Paint().apply {
        textSize = 9f
        isFakeBoldText = true
        color = Color.rgb(15, 23, 42)
    }
    val paintText = Paint().apply {
        textSize = 8.5f
        color = Color.rgb(30, 41, 59)
    }
    val paintLine = Paint().apply {
        color = Color.rgb(203, 213, 225)
        strokeWidth = 1f
    }

    var pageNum = 1
    var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas: Canvas = page.canvas

    fun dibujarEncabezado() {
        canvas.drawText("STAGETRACK - INVENTARIO CONSOLIDADO DE ACTIVOS", 40f, 45f, paintTitle)
        canvas.drawText("Fecha de emisión: $fecha | Total Activos: ${equipos.size}", 40f, 62f, paintSub)
        canvas.drawLine(40f, 72f, 555f, 72f, paintLine)

        canvas.drawText("ID / SERIE", 40f, 90f, paintHeader)
        canvas.drawText("NOMBRE DEL EQUIPO", 140f, 90f, paintHeader)
        canvas.drawText("CATEGORÍA", 290f, 90f, paintHeader)
        canvas.drawText("ESTADO", 380f, 90f, paintHeader)
        canvas.drawText("CUSTODIO", 470f, 90f, paintHeader)
        canvas.drawLine(40f, 96f, 555f, 96f, paintLine)
    }

    dibujarEncabezado()
    var yPos = 115f

    equipos.forEach { eq ->
        if (yPos > 790f) {
            pdfDocument.finishPage(page)
            pageNum++
            pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            dibujarEncabezado()
            yPos = 115f
        }

        val serie = if (eq.numeroSerie.length > 14) eq.numeroSerie.substring(0, 14) + ".." else eq.numeroSerie
        val nombre = if (eq.nombre.length > 22) eq.nombre.substring(0, 22) + ".." else eq.nombre
        val cat = if (eq.categoria.length > 13) eq.categoria.substring(0, 13) + ".." else eq.categoria
        val estado = eq.estadoActual.name
        val custodio = eq.nombreCustodioActual ?: "Bodega"
        val custodioCorto = if (custodio.length > 13) custodio.substring(0, 13) + ".." else custodio

        canvas.drawText(serie, 40f, yPos, paintText)
        canvas.drawText(nombre, 140f, yPos, paintText)
        canvas.drawText(cat, 290f, yPos, paintText)
        canvas.drawText(estado, 380f, yPos, paintText)
        canvas.drawText(custodioCorto, 470f, yPos, paintText)

        yPos += 18f
    }

    pdfDocument.finishPage(page)
    val outputStream = ByteArrayOutputStream()
    pdfDocument.writeTo(outputStream)
    pdfDocument.close()

    guardarYAlmacenarReporteBytes(
        context,
        "inventario_stagetrack_${System.currentTimeMillis()}.pdf",
        outputStream.toByteArray(),
        "application/pdf"
    )
}

private fun exportarPdfValoracion(context: Context, equipos: List<Equipo>) {
    val pdfDocument = PdfDocument()
    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val total = equipos.size
    val enBodega = equipos.count { it.estadoActual.name == "EN_BODEGA" }
    val enCustodia = equipos.count { it.estadoActual.name == "EN_CUSTODIA" || it.estadoActual.name == "EN_TRANSITO" }
    val danados = equipos.count { it.estadoActual.name == "REPORTADO_DANADO" }
    val dadosBaja = equipos.count { it.estadoActual.name == "DADO_DE_BAJA" }

    val paintTitle = Paint().apply {
        textSize = 16f
        isFakeBoldText = true
        color = Color.rgb(30, 41, 59)
    }
    val paintSub = Paint().apply {
        textSize = 10f
        color = Color.rgb(100, 116, 139)
    }
    val paintSection = Paint().apply {
        textSize = 12f
        isFakeBoldText = true
        color = Color.rgb(15, 23, 42)
    }
    val paintText = Paint().apply {
        textSize = 10f
        color = Color.rgb(30, 41, 59)
    }
    val paintLine = Paint().apply {
        color = Color.rgb(203, 213, 225)
        strokeWidth = 1f
    }

    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    canvas.drawText("STAGETRACK - INFORME DE VALORACIÓN DE ACTIVOS", 40f, 45f, paintTitle)
    canvas.drawText("Fecha de emisión: $fecha | Administrador General", 40f, 62f, paintSub)
    canvas.drawLine(40f, 72f, 555f, 72f, paintLine)

    var yPos = 100f
    canvas.drawText("Resumen Ejecutivo Logístico", 40f, yPos, paintSection)
    canvas.drawLine(40f, yPos + 6f, 555f, yPos + 6f, paintLine)

    yPos += 28f
    canvas.drawText("• Total de Activos Registrados: $total activos", 50f, yPos, paintText)
    yPos += 22f
    canvas.drawText("• Equipos Disponibles en Bodega: $enBodega activos", 50f, yPos, paintText)
    yPos += 22f
    canvas.drawText("• Equipos en Custodia / Tránsito: $enCustodia activos", 50f, yPos, paintText)
    yPos += 22f
    canvas.drawText("• Equipos Reportados Dañados: $danados activos", 50f, yPos, paintText)
    yPos += 22f
    canvas.drawText("• Equipos Dados de Baja (Archivados): $dadosBaja activos", 50f, yPos, paintText)

    yPos += 45f
    canvas.drawText("Desglose por Categoría de Equipo", 40f, yPos, paintSection)
    canvas.drawLine(40f, yPos + 6f, 555f, yPos + 6f, paintLine)

    yPos += 28f
    equipos.groupBy { it.categoria }.forEach { (cat, lista) ->
        canvas.drawText(" - Categoría \"$cat\": ${lista.size} activos", 50f, yPos, paintText)
        yPos += 20f
    }

    yPos += 60f
    canvas.drawLine(40f, yPos, 220f, yPos, paintLine)
    canvas.drawText("Firma Administrador General", 40f, yPos + 16f, paintSub)

    pdfDocument.finishPage(page)
    val outputStream = ByteArrayOutputStream()
    pdfDocument.writeTo(outputStream)
    pdfDocument.close()

    guardarYAlmacenarReporteBytes(
        context,
        "valoracion_activos_${System.currentTimeMillis()}.pdf",
        outputStream.toByteArray(),
        "application/pdf"
    )
}

private fun exportarPdfDanados(context: Context, equipos: List<Equipo>) {
    val danados = equipos.filter { it.estadoActual.name == "REPORTADO_DANADO" }
    val pdfDocument = PdfDocument()
    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val paintTitle = Paint().apply {
        textSize = 16f
        isFakeBoldText = true
        color = Color.rgb(185, 28, 28)
    }
    val paintSub = Paint().apply {
        textSize = 10f
        color = Color.rgb(100, 116, 139)
    }
    val paintHeader = Paint().apply {
        textSize = 9f
        isFakeBoldText = true
        color = Color.rgb(15, 23, 42)
    }
    val paintText = Paint().apply {
        textSize = 8.5f
        color = Color.rgb(30, 41, 59)
    }
    val paintLine = Paint().apply {
        color = Color.rgb(203, 213, 225)
        strokeWidth = 1f
    }

    var pageNum = 1
    var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas

    fun dibujarEncabezado() {
        canvas.drawText("STAGETRACK - REPORTE GLOBAL DE DAÑOS E INCIDENCIAS", 40f, 45f, paintTitle)
        canvas.drawText("Fecha de emisión: $fecha | Equipos Dañados: ${danados.size}", 40f, 62f, paintSub)
        canvas.drawLine(40f, 72f, 555f, 72f, paintLine)

        canvas.drawText("N° SERIE", 40f, 90f, paintHeader)
        canvas.drawText("NOMBRE EQUIPO", 150f, 90f, paintHeader)
        canvas.drawText("CATEGORÍA", 300f, 90f, paintHeader)
        canvas.drawText("UBICACIÓN", 400f, 90f, paintHeader)
        canvas.drawText("CUSTODIO", 480f, 90f, paintHeader)
        canvas.drawLine(40f, 96f, 555f, 96f, paintLine)
    }

    dibujarEncabezado()
    var yPos = 115f

    if (danados.isEmpty()) {
        canvas.drawText("No existen equipos registrados actualmente con reporte de daño activo.", 40f, yPos, paintText)
    } else {
        danados.forEach { eq ->
            if (yPos > 790f) {
                pdfDocument.finishPage(page)
                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                dibujarEncabezado()
                yPos = 115f
            }

            val serie = if (eq.numeroSerie.length > 15) eq.numeroSerie.substring(0, 15) + ".." else eq.numeroSerie
            val nombre = if (eq.nombre.length > 22) eq.nombre.substring(0, 22) + ".." else eq.nombre
            val cat = if (eq.categoria.length > 13) eq.categoria.substring(0, 13) + ".." else eq.categoria
            val ubi = if (eq.ubicacionActual.length > 12) eq.ubicacionActual.substring(0, 12) + ".." else eq.ubicacionActual
            val custodio = eq.nombreCustodioActual ?: "N/D"

            canvas.drawText(serie, 40f, yPos, paintText)
            canvas.drawText(nombre, 150f, yPos, paintText)
            canvas.drawText(cat, 300f, yPos, paintText)
            canvas.drawText(ubi, 400f, yPos, paintText)
            canvas.drawText(custodio, 480f, yPos, paintText)

            yPos += 20f
        }
    }

    pdfDocument.finishPage(page)
    val outputStream = ByteArrayOutputStream()
    pdfDocument.writeTo(outputStream)
    pdfDocument.close()

    guardarYAlmacenarReporteBytes(
        context,
        "reporte_global_danos_${System.currentTimeMillis()}.pdf",
        outputStream.toByteArray(),
        "application/pdf"
    )
}

private fun exportarPdfTrazabilidad(context: Context, transferencias: List<TransferenciaCustodia>, equipos: List<Equipo>) {
    val mapaEquipos = equipos.associateBy { it.id }
    val pdfDocument = PdfDocument()
    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    val paintTitle = Paint().apply {
        textSize = 16f
        isFakeBoldText = true
        color = Color.rgb(30, 41, 59)
    }
    val paintSub = Paint().apply {
        textSize = 10f
        color = Color.rgb(100, 116, 139)
    }
    val paintHeader = Paint().apply {
        textSize = 9f
        isFakeBoldText = true
        color = Color.rgb(15, 23, 42)
    }
    val paintText = Paint().apply {
        textSize = 8f
        color = Color.rgb(30, 41, 59)
    }
    val paintLine = Paint().apply {
        color = Color.rgb(203, 213, 225)
        strokeWidth = 1f
    }

    var pageNum = 1
    var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas

    fun dibujarEncabezado() {
        canvas.drawText("STAGETRACK - LOG COMPLETO DE TRAZABILIDAD Y CUSTODIAS", 40f, 45f, paintTitle)
        canvas.drawText("Fecha de emisión: $fecha | Total Registros: ${transferencias.size}", 40f, 62f, paintSub)
        canvas.drawLine(40f, 72f, 555f, 72f, paintLine)

        canvas.drawText("EQUIPO", 40f, 90f, paintHeader)
        canvas.drawText("FECHA Y HORA", 160f, 90f, paintHeader)
        canvas.drawText("GPS", 270f, 90f, paintHeader)
        canvas.drawText("HASH SHA-256 (FIRMA)", 370f, 90f, paintHeader)
        canvas.drawLine(40f, 96f, 555f, 96f, paintLine)
    }

    dibujarEncabezado()
    var yPos = 115f

    if (transferencias.isEmpty()) {
        canvas.drawText("No existen registros de transferencias de custodia almacenados.", 40f, yPos, paintText)
    } else {
        transferencias.forEach { t ->
            if (yPos > 790f) {
                pdfDocument.finishPage(page)
                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                dibujarEncabezado()
                yPos = 115f
            }

            val eqNombre = mapaEquipos[t.idEquipo]?.nombre ?: t.idEquipo
            val eqCorto = if (eqNombre.length > 20) eqNombre.substring(0, 20) + ".." else eqNombre
            val fTexto = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(t.fechaHoraTimestamp))
            val gps = if (t.latitudGps != null && t.longitudGps != null) String.format(Locale.US, "%.3f,%.3f", t.latitudGps, t.longitudGps) else "N/A"
            val hashCorto = if (t.hashSha256Evidencia.length > 22) t.hashSha256Evidencia.substring(0, 22) + ".." else t.hashSha256Evidencia

            canvas.drawText(eqCorto, 40f, yPos, paintText)
            canvas.drawText(fTexto, 160f, yPos, paintText)
            canvas.drawText(gps, 270f, yPos, paintText)
            canvas.drawText(hashCorto, 370f, yPos, paintText)

            yPos += 18f
        }
    }

    pdfDocument.finishPage(page)
    val outputStream = ByteArrayOutputStream()
    pdfDocument.writeTo(outputStream)
    pdfDocument.close()

    guardarYAlmacenarReporteBytes(
        context,
        "log_trazabilidad_${System.currentTimeMillis()}.pdf",
        outputStream.toByteArray(),
        "application/pdf"
    )
}

// ==================== ALMACENAMIENTO DE ARCHIVOS (MEDIASTORE) ====================

private fun guardarYAlmacenarReporte(context: Context, nombreArchivo: String, contenido: String) {
    guardarYAlmacenarReporteBytes(
        context,
        nombreArchivo,
        contenido.toByteArray(),
        if (nombreArchivo.endsWith(".csv")) "text/csv" else "text/plain"
    )
}

private fun guardarYAlmacenarReporteBytes(
    context: Context,
    nombreArchivo: String,
    bytes: ByteArray,
    mimeType: String
) {
    try {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/StageTrack_Reports")
        }

        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(bytes)
            }
            Toast.makeText(
                context,
                "¡Reporte guardado en Descargas/StageTrack_Reports/$nombreArchivo!",
                Toast.LENGTH_LONG
            ).show()
        } else {
            val archivo = File(context.cacheDir, nombreArchivo)
            FileOutputStream(archivo).use { it.write(bytes) }

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                archivo
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
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
