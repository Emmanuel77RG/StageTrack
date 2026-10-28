package com.example.stagetrack.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stagetrack.datos.local.entidad.EntidadReporteDano
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a datos de Room para la tabla 'reportes_dano'.
 */
@Dao
interface DaoReporteDano {

    @Query("SELECT * FROM reportes_dano WHERE idEquipo = :idEquipo ORDER BY fechaHoraTimestamp DESC")
    fun obtenerReportesDanoPorEquipo(idEquipo: String): Flow<List<EntidadReporteDano>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReporteDano(reporte: EntidadReporteDano)
}
