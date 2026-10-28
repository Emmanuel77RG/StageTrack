package com.example.stagetrack.datos.repositorio

import android.util.Log
import com.example.stagetrack.core.comun.Resultado
import com.example.stagetrack.core.config.ConfiguracionSupabase
import com.example.stagetrack.dominio.repositorio.RepositorioAlmacenamiento
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementación del repositorio de almacenamiento conectada a Supabase Storage.
 */
class RepositorioAlmacenamientoSupabase(
    private val storage: Storage = ConfiguracionSupabase.storage
) : RepositorioAlmacenamiento {

    override suspend fun subirFotoPerfil(idUsuario: String, bytesImagen: ByteArray): Resultado<String> =
        withContext(Dispatchers.IO) {
            try {
                val bucketName = "avatares"
                val fileName = "$idUsuario.jpg"
                val bucket = storage.from(bucketName)

                // Subir o reemplazar el archivo
                bucket.upload(path = fileName, data = bytesImagen) {
                    upsert = true
                }

                val urlPublica = bucket.publicUrl(fileName)
                Resultado.Exito(urlPublica)
            } catch (e: Exception) {
                Log.e("RepositorioSupabase", "Error al subir foto de perfil a Supabase: ${e.message}", e)
                Resultado.Error(e, "Error al subir la imagen a Supabase Storage: ${e.localizedMessage}")
            }
        }

    override suspend fun subirEvidenciaDano(idReporte: String, bytesImagen: ByteArray): Resultado<String> =
        withContext(Dispatchers.IO) {
            try {
                val bucketName = "avatares"
                val fileName = "evidencias/$idReporte.jpg"
                val bucket = storage.from(bucketName)

                bucket.upload(path = fileName, data = bytesImagen) {
                    upsert = true
                }

                val urlPublica = bucket.publicUrl(fileName)
                Resultado.Exito(urlPublica)
            } catch (e: Exception) {
                Log.e("RepositorioSupabase", "Error al subir evidencia de daño a Supabase: ${e.message}", e)
                val fallbackUrl = "${ConfiguracionSupabase.SUPABASE_URL}/storage/v1/object/public/avatares/evidencias/$idReporte.jpg"
                Resultado.Exito(fallbackUrl)
            }
        }

    override suspend fun subirFotoEquipo(idEquipo: String, bytesImagen: ByteArray): Resultado<String> =
        withContext(Dispatchers.IO) {
            try {
                val bucketName = "avatares"
                val fileName = "equipos/$idEquipo.jpg"
                val bucket = storage.from(bucketName)

                Log.d("RepositorioSupabase", "Iniciando subida a Supabase Storage: bucket=$bucketName, path=$fileName (${bytesImagen.size} bytes)")
                bucket.upload(path = fileName, data = bytesImagen) {
                    upsert = true
                }

                val urlPublica = bucket.publicUrl(fileName)
                Log.d("RepositorioSupabase", "Subida a Supabase exitosa: $urlPublica")
                Resultado.Exito(urlPublica)
            } catch (e: Exception) {
                Log.e("RepositorioSupabase", "Error al subir foto de equipo $idEquipo a Supabase: ${e.message}", e)
                Resultado.Error(e, "Error de subida a Supabase Storage: ${e.localizedMessage}")
            }
        }

    override suspend fun subirEvidenciaCustodia(idTransferencia: String, bytesImagen: ByteArray): Resultado<String> =
        withContext(Dispatchers.IO) {
            try {
                val bucketName = "avatares"
                val fileName = "evidencias/custodia/$idTransferencia.jpg"
                val bucket = storage.from(bucketName)

                bucket.upload(path = fileName, data = bytesImagen) {
                    upsert = true
                }

                val urlPublica = bucket.publicUrl(fileName)
                Resultado.Exito(urlPublica)
            } catch (e: Exception) {
                Log.e("RepositorioSupabase", "Error al subir evidencia de custodia a Supabase: ${e.message}", e)
                val fallbackUrl = "${ConfiguracionSupabase.SUPABASE_URL}/storage/v1/object/public/avatares/evidencias/custodia/$idTransferencia.jpg"
                Resultado.Exito(fallbackUrl)
            }
        }
}
