package com.example.flashify.view.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.flashify.model.manager.ProfileImageManager

/**
 * Componente reutilizável de Avatar do usuário
 * Exibe foto de perfil do Google ou inicial do nome
 */
@Composable
fun ProfileAvatar(
    username: String?,
    size: Dp = 64.dp,
    fontSize: Int = 28,
    borderWidth: Dp = 2.dp
) {
    val context = LocalContext.current
    val profileImageManager = remember { ProfileImageManager(context) }
    val profileImageUrl by profileImageManager.profileImageUrl.collectAsState(initial = null)
    val primaryColor = MaterialTheme.colorScheme.primary

    val initial = username?.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    // ✅ LOG para debug
    LaunchedEffect(profileImageUrl) {
        Log.d("ProfileAvatar", "🔍 URL da foto: $profileImageUrl")
        Log.d("ProfileAvatar", "👤 Username: $username")
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (profileImageUrl == null) {
                    Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.3f),
                                    primaryColor.copy(alpha = 0.15f)
                                )
                            )
                        )
                        .border(borderWidth, primaryColor.copy(alpha = 0.4f), CircleShape)
                } else {
                    Modifier.border(borderWidth, primaryColor.copy(alpha = 0.4f), CircleShape)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (profileImageUrl != null && profileImageUrl!!.isNotEmpty()) {
            Log.d("ProfileAvatar", "✅ Carregando imagem: $profileImageUrl")
            // Exibe a foto do Google
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(profileImageUrl)
                    .crossfade(true)
                    .listener(
                        onStart = {
                            Log.d("ProfileAvatar", "🔵 Iniciando carregamento da imagem")
                        },
                        onSuccess = { _, _ ->
                            Log.d("ProfileAvatar", "✅ Imagem carregada com sucesso!")
                        },
                        onError = { _, result ->
                            Log.e("ProfileAvatar", "❌ Erro ao carregar imagem: ${result.throwable.message}")
                        }
                    )
                    .build(),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Log.d("ProfileAvatar", "⚠️ Exibindo fallback (inicial do nome)")
            // Fallback: Exibe inicial do nome
            Text(
                text = initial,
                fontWeight = FontWeight.ExtraBold,
                color = primaryColor,
                fontSize = fontSize.sp
            )
        }
    }
}

/**
 * Variante com ícone padrão (para TelaConfiguracao)
 */
@Composable
fun ProfileAvatarWithIcon(
    username: String?,
    size: Dp = 64.dp,
    iconSize: Dp = 40.dp,
    borderWidth: Dp = 2.dp
) {
    val context = LocalContext.current
    val profileImageManager = remember { ProfileImageManager(context) }
    val profileImageUrl by profileImageManager.profileImageUrl.collectAsState(initial = null)
    val primaryColor = MaterialTheme.colorScheme.primary

    // ✅ LOG para debug
    LaunchedEffect(profileImageUrl) {
        Log.d("ProfileAvatarIcon", "🔍 URL da foto: $profileImageUrl")
        Log.d("ProfileAvatarIcon", "👤 Username: $username")
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (profileImageUrl == null) {
                    Modifier
                        .background(primaryColor.copy(alpha = 0.2f))
                        .border(borderWidth, primaryColor.copy(alpha = 0.4f), CircleShape)
                } else {
                    Modifier.border(borderWidth, primaryColor.copy(alpha = 0.4f), CircleShape)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (profileImageUrl != null && profileImageUrl!!.isNotEmpty()) {
            Log.d("ProfileAvatarIcon", "✅ Carregando imagem: $profileImageUrl")
            // Exibe a foto do Google
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(profileImageUrl)
                    .crossfade(true)
                    .listener(
                        onStart = {
                            Log.d("ProfileAvatarIcon", "🔵 Iniciando carregamento da imagem")
                        },
                        onSuccess = { _, _ ->
                            Log.d("ProfileAvatarIcon", "✅ Imagem carregada com sucesso!")
                        },
                        onError = { _, result ->
                            Log.e("ProfileAvatarIcon", "❌ Erro ao carregar imagem: ${result.throwable.message}")
                        }
                    )
                    .build(),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Log.d("ProfileAvatarIcon", "⚠️ Exibindo fallback (ícone)")
            // Fallback: Ícone padrão
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Foto de Perfil",
                modifier = Modifier.size(iconSize),
                tint = primaryColor
            )
        }
    }
}