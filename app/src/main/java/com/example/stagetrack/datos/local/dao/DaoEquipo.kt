package com.example.stagetrack.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stagetrack.datos.local.entidad.EntidadEquipo
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a datos de Room para la tabla 'equipos'.
 */
@Dao
interface DaoEquipo {

    @Query("SELECT * FROM equipos ORDER BY nombre ASC")
    fun obtenerTodosLosEquipos(): Flow<List<EntidadEquipo>>

    @Query("SELECT * FROM equipos WHERE codigoQR = :codigoQr LIMIT 1")
    fun obtenerEquipoPorCodigoQr(codigoQr: String): Flow<EntidadEquipo?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizarEquipo(equipo: EntidadEquipo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizarEquipos(equipos: List<EntidadEquipo>)

    @Query("UPDATE equipos SET estadoActual = :nuevoEstado, idCustodioActual = :idCustodio, nombreCustodioActual = :nombreCustodio, sincronizado = :sincronizado WHERE id = :idEquipo")
    suspend fun actualizarEstadoCustodia(
        idEquipo: String,
        nuevoEstado: String,
        idCustodio: String?,
        nombreCustodio: String?,
        sincronizado: Boolean
    )
}
