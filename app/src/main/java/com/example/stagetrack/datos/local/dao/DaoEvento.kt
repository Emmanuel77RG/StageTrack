package com.example.stagetrack.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stagetrack.datos.local.entidad.EntidadEvento
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a datos de Room para la tabla 'eventos'.
 */
@Dao
interface DaoEvento {

    @Query("SELECT * FROM eventos ORDER BY fechaInicio DESC")
    fun obtenerTodosLosEventos(): Flow<List<EntidadEvento>>

    @Query("SELECT * FROM eventos WHERE idsTecnicosAsignadosCsv LIKE '%' || :idTecnico || '%' ORDER BY fechaInicio DESC")
    fun obtenerEventosPorTecnico(idTecnico: String): Flow<List<EntidadEvento>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizarEvento(evento: EntidadEvento)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizarEventos(eventos: List<EntidadEvento>)

    @Query("UPDATE eventos SET estado = :nuevoEstado WHERE id = :idEvento")
    suspend fun actualizarEstadoEvento(idEvento: String, nuevoEstado: String)
}
