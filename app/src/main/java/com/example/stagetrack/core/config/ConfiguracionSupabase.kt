package com.example.stagetrack.core.config

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

/**
 * Configuración e inicialización centralizada del cliente de Supabase para StageTrack.
 */
object ConfiguracionSupabase {
    const val SUPABASE_URL = "https://leirxujoescyaeukajsu.supabase.co"
    const val SUPABASE_KEY = "sb_publishable_l7ib5T3ToZk0HI7LDi7L-Q_qY5ApkeY"

    val cliente by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Storage)
        }
    }

    val storage by lazy {
        cliente.storage
    }
}
