package com.example.stagetrack.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stagetrack.datos.local.entidad.EntidadTransferenciaCustodia
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a datos de Room para la tabla 'transferencias_custodia'.
 */
@Dao
interface DaoCustodia {

    @Query("SELECT * FROM transferencias_custodia WHERE idEquipo = :idEquipo ORDER BY fechaHoraTimestamp DESC")
    fun obtenerHistorialCustodia(idEquipo: String): Flow<List<EntidadTransferenciaCustodia>>

    @Query("SELECT * FROM transferencias_custodia ORDER BY fechaHoraTimestamp DESC")
    fun obtenerHistorialGlobalCustodia(): Flow<List<EntidadTransferenciaCustodia>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTransferencia(transferencia: EntidadTransferenciaCustodia)

    @Query("SELECT * FROM transferencias_custodia WHERE sincronizado = 0")
    suspend fun obtenerTransferenciasPendientesSincronizacion(): List<EntidadTransferenciaCustodia>
}
