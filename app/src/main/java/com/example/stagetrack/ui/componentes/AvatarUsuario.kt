package com.example.stagetrack.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage

/**
 * Componente de avatar de usuario que renderiza la imagen desde la URL de Supabase usando Coil,
 * o muestra las iniciales / ícono del usuario como fallback.
 */
@Composable
fun AvatarUsuario(
    urlFotografia: String?,
    nombreUsuario: String,
    modifier: Modifier = Modifier,
    tamano: Dp = 48.dp
) {
    val iniciales = obtenerIniciales(nombreUsuario)

    if (!urlFotografia.isNullOrBlank()) {
        SubcomposeAsyncImage(
            model = urlFotografia,
            contentDescription = "Foto de perfil de $nombreUsuario",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(tamano)
                .clip(CircleShape),
            loading = {
                FallbackAvatar(iniciales = iniciales, tamano = tamano)
            },
            error = {
                FallbackAvatar(iniciales = iniciales, tamano = tamano)
            }
        )
    } else {
        FallbackAvatar(iniciales = iniciales, tamano = tamano, modifier = modifier)
    }
}

@Composable
private fun FallbackAvatar(
    iniciales: String,
    tamano: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (iniciales.isNotBlank()) {
            Text(
                text = iniciales,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = (tamano.value * 0.38f).sp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(tamano * 0.6f)
            )
        }
    }
}

private fun obtenerIniciales(nombre: String): String {
    val partes = nombre.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        partes.isEmpty() -> ""
        partes.size == 1 -> partes[0].take(2).uppercase()
        else -> "${partes[0].first()}${partes[1].first()}".uppercase()
    }
}
