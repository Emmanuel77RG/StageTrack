package com.example.stagetrack

import com.example.stagetrack.datos.mapeadores.MapeadorCustodia
import com.example.stagetrack.datos.mapeadores.MapeadorEquipo
import com.example.stagetrack.dominio.modelo.Equipo
import com.example.stagetrack.dominio.modelo.EstadoEquipo
import com.example.stagetrack.dominio.modelo.TransferenciaCustodia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PruebaBaseDatosRoom {

    @Test
    fun mapeadorEquipo_conversionBidireccional_mantieneDatosCorrectos() {
        val equipoOriginal = Equipo(
            id = "eq_test_100",
            codigoQR = "QR-TEST-MAPPING",
            nombre = "Micrófono Shure SM58",
            categoria = "Audio",
            numeroSerie = "SN-SM58-001",
            estadoActual = EstadoEquipo.EN_BODEGA,
            ubicacionActual = "Almacén A"
        )

        val entidad = MapeadorEquipo.aEntidad(equipoOriginal, sincronizado = false)
        val equipoMapeado = MapeadorEquipo.aDominio(entidad)

        assertEquals(equipoOriginal.id, equipoMapeado.id)
        assertEquals(equipoOriginal.codigoQR, equipoMapeado.codigoQR)
        assertEquals(equipoOriginal.nombre, equipoMapeado.nombre)
        assertEquals(equipoOriginal.estadoActual, equipoMapeado.estadoActual)
    }

    @Test
    fun mapeadorCustodia_conversionTransferencia_mantieneHashSha256() {
        val transferenciaOriginal = TransferenciaCustodia(
            id = "trans_test_01",
            idEquipo = "eq_test_100",
            idUsuarioEmisor = "usr_01",
            idUsuarioReceptor = "usr_02",
            uriFotografiaEvidencia = "file:///evidencia/foto.jpg",
            hashSha256Evidencia = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        )

        val entidad = MapeadorCustodia.transferenciaAEntidad(transferenciaOriginal, sincronizado = false)
        val transferenciaMapeada = MapeadorCustodia.transferenciaADominio(entidad)

        assertEquals(transferenciaOriginal.hashSha256Evidencia, transferenciaMapeada.hashSha256Evidencia)
        assertNotNull(transferenciaMapeada.uriFotografiaEvidencia)
    }
}
