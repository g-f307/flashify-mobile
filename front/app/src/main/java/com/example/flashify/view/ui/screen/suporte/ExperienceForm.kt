package com.example.flashify.view.ui.screen.suporte

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperienceForm(
    onBack: () -> Unit,
    isDarkTheme: Boolean
) {
    var showSuccess by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var rating by remember { mutableIntStateOf(0) }
    var easeOfUse by remember { mutableStateOf("") }
    var mostUsedFeature by remember { mutableStateOf("") }
    var wouldRecommend by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }

    var showErrors by remember { mutableStateOf(false) }

    val blueColor = if (isDarkTheme) Color(0xFF42A5F5) else Color(0xFF1976D2)

    if (showSuccess) {
        SuccessScreen(
            title = "Obrigado pelo Feedback!",
            message = "Sua opinião é muito importante para nós e nos ajuda a criar uma melhor experiência.",
            iconColor = blueColor,
            onDismiss = onBack,
            isDarkTheme = isDarkTheme
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header fixo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    "Voltar",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Relatar Experiência",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Sua jornada com o Flashify",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Conteúdo scrollável
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Rating
            Text(
                "Como você avalia sua experiência geral? *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                (1..5).forEach { star ->
                    IconButton(
                        onClick = { rating = star },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            if (star <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "$star estrelas",
                            tint = if (star <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Muito ruim",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Excelente",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (showErrors && rating == 0) {
                Text(
                    "Selecione uma avaliação",
                    color = if (isDarkTheme) Color(0xFFEF5350) else Color(0xFFD32F2F),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Facilidade
            Text(
                "O Flashify é fácil de usar? *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EaseOption("😊", "Muito fácil", easeOfUse == "muito_facil", Modifier.weight(1f), isDarkTheme) { easeOfUse = "muito_facil" }
                EaseOption("😐", "Normal", easeOfUse == "normal", Modifier.weight(1f), isDarkTheme) { easeOfUse = "normal" }
                EaseOption("😕", "Difícil", easeOfUse == "dificil", Modifier.weight(1f), isDarkTheme) { easeOfUse = "dificil" }
            }

            Spacer(Modifier.height(16.dp))

            // Funcionalidade mais usada
            Text(
                "Qual funcionalidade você mais usa? *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "flashcards" to "📚 Estudar com Flashcards",
                    "quiz" to "🎯 Fazer Quizzes",
                    "upload" to "📄 Upload de PDFs/Imagens",
                    "folders" to "📁 Organização em Pastas",
                    "progress" to "📊 Acompanhar Progresso",
                    "outros" to "✨ Outros"
                ).forEach { (value, label) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = mostUsedFeature == value,
                                onClick = { mostUsedFeature = value }
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (mostUsedFeature == value) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (mostUsedFeature == value) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = mostUsedFeature == value,
                                onClick = { mostUsedFeature = value },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                label,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Recomendaria
            Text(
                "Você recomendaria o Flashify? *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RecommendOption("👍", "Sim!", wouldRecommend == "sim", Modifier.weight(1f), Color(0xFF4CAF50), isDarkTheme) { wouldRecommend = "sim" }
                RecommendOption("🤔", "Talvez", wouldRecommend == "talvez", Modifier.weight(1f), Color(0xFFFFC107), isDarkTheme) { wouldRecommend = "talvez" }
                RecommendOption("👎", "Não", wouldRecommend == "nao", Modifier.weight(1f), Color(0xFFF44336), isDarkTheme) { wouldRecommend = "nao" }
            }

            Spacer(Modifier.height(16.dp))

            // Feedback adicional
            Text(
                "Quer compartilhar mais alguma coisa? (opcional)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = feedback,
                onValueChange = { feedback = it },
                placeholder = { Text("Conte-nos mais sobre sua experiência...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(Modifier.height(16.dp))
        }

        // Botões fixos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = !isSubmitting,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    showErrors = true
                    if (rating > 0 && easeOfUse.isNotEmpty() && mostUsedFeature.isNotEmpty() && wouldRecommend.isNotEmpty()) {
                        isSubmitting = true
                        scope.launch {
                            val success = sendExperienceFeedback(rating, easeOfUse, mostUsedFeature, wouldRecommend, feedback)
                            isSubmitting = false
                            if (success) showSuccess = true
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Enviar", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun EaseOption(
    emoji: String,
    label: String,
    selected: Boolean,
    modifier: Modifier,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(65.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            1.5.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun RecommendOption(
    emoji: String,
    label: String,
    selected: Boolean,
    modifier: Modifier,
    color: Color,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(65.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.5.dp,
            if (selected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

suspend fun sendExperienceFeedback(
    rating: Int,
    easeOfUse: String,
    mostUsedFeature: String,
    wouldRecommend: String,
    feedback: String
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val webhook = "https://discordapp.com/api/webhooks/1438399615165796384/20Z_yNxlpJEFKtXyx-Y1YVleLZRP75W_z5E6VW8MYeoobSnQfaj0fVwMdkHzk0VL1cVd"
            val stars = "⭐".repeat(rating)
            val json = JSONObject().apply {
                put("embeds", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("title", "💬 Nova Experiência Relatada")
                        put("color", 3447003)
                        put("fields", org.json.JSONArray().apply {
                            put(JSONObject().apply { put("name", "⭐ Avaliação"); put("value", "$stars ($rating/5)"); put("inline", false) })
                            put(JSONObject().apply { put("name", "🎯 Facilidade de Uso"); put("value", easeOfUse); put("inline", true) })
                            put(JSONObject().apply { put("name", "📱 Funcionalidade Favorita"); put("value", mostUsedFeature); put("inline", true) })
                            put(JSONObject().apply { put("name", "👥 Recomendaria"); put("value", wouldRecommend); put("inline", true) })
                            if (feedback.isNotEmpty()) {
                                put(JSONObject().apply { put("name", "💭 Feedback Adicional"); put("value", feedback.take(1000)) })
                            }
                        })
                        put("timestamp", java.time.Instant.now().toString())
                    })
                })
            }
            val connection = URL(webhook).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.write(json.toString().toByteArray())
            val responseCode = connection.responseCode
            connection.disconnect()
            responseCode in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}