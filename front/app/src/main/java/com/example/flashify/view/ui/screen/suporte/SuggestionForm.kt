package com.example.flashify.view.ui.screen.suporte

import androidx.compose.foundation.BorderStroke
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
fun SuggestionForm(
    onBack: () -> Unit,
    isDarkTheme: Boolean
) {
    var showSuccess by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var category by remember { mutableStateOf("") }
    var impact by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var useCase by remember { mutableStateOf("") }

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    val yellowColor = MaterialTheme.colorScheme.primary

    if (showSuccess) {
        SuccessScreen(
            title = "Sugestão Recebida!",
            message = "Obrigado por contribuir! Vamos analisar sua sugestão e considerar para futuras atualizações.",
            iconColor = yellowColor,
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
                    "Sugerir Melhorias",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Compartilhe suas ideias",
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

            // Categoria
            Text(
                "Esta sugestão está relacionada a qual área? *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = showCategoryMenu,
                onExpandedChange = { showCategoryMenu = it }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Selecione uma categoria") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                ExposedDropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    listOf(
                        "📚 Flashcards", "🎯 Quizzes", "📖 Modo de Estudo",
                        "📁 Organização (Pastas/Biblioteca)", "📊 Acompanhamento de Progresso",
                        "🤖 Inteligência Artificial", "🎨 Interface / Design",
                        "📱 Versão Mobile", "🔗 Integrações", "🎮 Gamificação", "✨ Outros"
                    ).forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                category = item
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Impacto
            Text(
                "Qual seria o impacto desta melhoria para você? *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "alto" to ("🔥 Alto impacto" to "Mudaria significativamente minha experiência"),
                    "medio" to ("💡 Impacto moderado" to "Melhoraria minha experiência de forma notável"),
                    "baixo" to ("✨ Seria legal ter" to "Um adicional interessante, mas não essencial")
                ).forEach { (value, labels) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = impact == value,
                                onClick = { impact = value }
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (impact == value) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (impact == value) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = impact == value,
                                onClick = { impact = value },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    labels.first,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    labels.second,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Título
            Text(
                "Resuma sua sugestão em uma frase *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Ex: Adicionar modo escuro para os flashcards") },
                modifier = Modifier.fillMaxWidth(),
                isError = showErrors && title.isEmpty(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(Modifier.height(16.dp))

            // Descrição
            Text(
                "Descreva sua sugestão em detalhes *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Explique sua ideia: o que seria, como funcionaria, quais benefícios traria...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 6,
                isError = showErrors && description.isEmpty(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Text(
                "Quanto mais detalhes, melhor conseguiremos entender sua visão!",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Caso de uso
            Text(
                "Dê um exemplo de como você usaria isso (opcional)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = useCase,
                onValueChange = { useCase = it },
                placeholder = { Text("Ex: Quando estou estudando à noite, seria útil ter um modo escuro...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(Modifier.height(12.dp))

            // Info Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "💡 Dica: Não se preocupe se sua sugestão já foi feita por outra pessoa. Múltiplos pedidos nos ajudam a priorizar as funcionalidades mais desejadas!",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

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
                    if (title.isNotEmpty() && description.isNotEmpty() && impact.isNotEmpty()) {
                        isSubmitting = true
                        scope.launch {
                            val success = sendSuggestion(category, impact, title, description, useCase)
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

suspend fun sendSuggestion(
    category: String,
    impact: String,
    title: String,
    description: String,
    useCase: String
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val webhook = "https://discordapp.com/api/webhooks/1438399909651939436/KmbtElGtyZyly7vRo68vVFY_NppP-7-gdJVKfOip4-wkLHH4ZOldPt4ArYgJ4sEm7ztT"
            val embedColor = when(impact) {
                "alto" -> 15158332
                "medio" -> 16776960
                else -> 5763719
            }
            val json = JSONObject().apply {
                put("embeds", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("title", "💡 Nova Sugestão de Melhoria")
                        put("color", embedColor)
                        put("fields", org.json.JSONArray().apply {
                            put(JSONObject().apply { put("name", "📂 Categoria"); put("value", category.ifEmpty { "Não especificado" }); put("inline", true) })
                            put(JSONObject().apply { put("name", "⚡ Impacto"); put("value", impact.uppercase()); put("inline", true) })
                            put(JSONObject().apply { put("name", "📝 Título"); put("value", title) })
                            put(JSONObject().apply { put("name", "📋 Descrição"); put("value", description.take(1000)) })
                            if (useCase.isNotEmpty()) {
                                put(JSONObject().apply { put("name", "💭 Caso de Uso"); put("value", useCase.take(500)) })
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