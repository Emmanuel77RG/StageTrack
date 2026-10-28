package com.example.stagetrack.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stagetrack.datos.local.entidad.EntidadUsuario
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a datos de Room para la tabla 'usuarios'.
 */
@Dao
interface DaoUsuario {

    @Query("SELECT * FROM usuarios ORDER BY fechaCreacion DESC")
    fun obtenerTodosLosUsuarios(): Flow<List<EntidadUsuario>>

    @Query("SELECT * FROM usuarios ORDER BY fechaCreacion DESC")
    suspend fun obtenerListaTodasEntidadesDirecta(): List<EntidadUsuario>

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun obtenerUsuarioPorCorreo(correo: String): EntidadUsuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun obtenerUsuarioPorId(id: String): EntidadUsuario?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizarUsuario(usuario: EntidadUsuario)

    @Query("UPDATE usuarios SET claveHash = :nuevaClaveHash WHERE id = :idUsuario")
    suspend fun actualizarContrasena(idUsuario: String, nuevaClaveHash: String)

    @Query("UPDATE usuarios SET urlFotografia = :urlFotografia WHERE id = :idUsuario")
    suspend fun actualizarFotoPerfil(idUsuario: String, urlFotografia: String)

    @Query("UPDATE usuarios SET correoRecuperacion = :correoRecuperacion WHERE id = :idUsuario")
    suspend fun actualizarCorreoRecuperacion(idUsuario: String, correoRecuperacion: String?)

    @Query("UPDATE usuarios SET esPrimerInicio = :esPrimerInicio WHERE id = :idUsuario")
    suspend fun actualizarEstadoPrimerInicio(idUsuario: String, esPrimerInicio: Boolean)

    @Query("UPDATE usuarios SET correoVerificado = :correoVerificado WHERE id = :idUsuario")
    suspend fun actualizarCorreoVerificado(idUsuario: String, correoVerificado: Boolean)

    @Query("UPDATE usuarios SET activo = :activo WHERE id = :idUsuario")
    suspend fun actualizarEstadoActivo(idUsuario: String, activo: Boolean)

    @Query("UPDATE usuarios SET nombre = :nombre, correo = :correo, rol = :rol WHERE id = :idUsuario")
    suspend fun actualizarDatosUsuario(idUsuario: String, nombre: String, correo: String, rol: String)

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int

    @Query("SELECT COUNT(*) FROM usuarios WHERE rol = 'ADMINISTRADOR_GENERAL' AND activo = 1")
    suspend fun contarAdministradoresActivos(): Int

    @Query("DELETE FROM usuarios WHERE correo = 'director@stagetrack.com' OR id = 'usr_director_01'")
    suspend fun eliminarUsuarioDirectorLegacy()

    @Query("UPDATE usuarios SET rol = 'SUPERVISOR' WHERE rol = 'DIRECTOR_TECNICO'")
    suspend fun migrarRolesDirectoresASupervisor()
}
