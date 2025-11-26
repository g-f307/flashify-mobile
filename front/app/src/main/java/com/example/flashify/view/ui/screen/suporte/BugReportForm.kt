package com.example.flashify.view.ui.screen.suporte

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
fun BugReportForm(
    onBack: () -> Unit,
    isDarkTheme: Boolean
) {
    var showSuccess by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Form states
    var priority by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf("") }
    var expected by remember { mutableStateOf("") }
    var actual by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var additionalInfo by remember { mutableStateOf("") }

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showFrequencyMenu by remember { mutableStateOf(false) }

    // Validation errors
    var showErrors by remember { mutableStateOf(false) }

    val redColor = if (isDarkTheme) Color(0xFFEF5350) else Color(0xFFD32F2F)

    if (showSuccess) {
        SuccessScreen(
            title = "Relatório Enviado!",
            message = "Recebemos seu relatório e nossa equipe já está analisando. Obrigado por nos ajudar a melhorar!",
            iconColor = Color(0xFF4CAF50),
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
                    "Relatar um Bug",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Preencha os campos abaixo",
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

            // Prioridade
            Text(
                "Qual a gravidade do problema? *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PriorityOption("🟢", "Baixa", priority == "baixa", Modifier.weight(1f), isDarkTheme) { priority = "baixa" }
                PriorityOption("🟡", "Média", priority == "media", Modifier.weight(1f), isDarkTheme) { priority = "media" }
                PriorityOption("🔴", "Alta", priority == "alta", Modifier.weight(1f), isDarkTheme) { priority = "alta" }
            }
            if (showErrors && priority.isEmpty()) {
                Text(
                    "Selecione a gravidade",
                    color = redColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Categoria
            Text(
                "Em qual área ocorreu? *",
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
                    label = { Text("Selecione uma área") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
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
                        "Login / Autenticação", "Upload de Arquivos", "Geração de Flashcards",
                        "Geração de Quiz", "Modo de Estudo", "Organização (Pastas)",
                        "Desempenho / Lentidão", "Problemas Visuais", "Outros"
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

            // Título
            Text(
                "Resuma o problema em uma frase *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Ex: Não consigo fazer upload de PDF") },
                modifier = Modifier.fillMaxWidth(),
                isError = showErrors && title.isEmpty(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(Modifier.height(16.dp))

            // Passos
            Text(
                "Como reproduzir o problema? *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = steps,
                onValueChange = { steps = it },
                placeholder = { Text("1. Vou até...\n2. Clico em...\n3. Aparece...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                maxLines = 5,
                isError = showErrors && steps.isEmpty(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(Modifier.height(16.dp))

            // Esperado
            Text(
                "O que você esperava? *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = expected,
                onValueChange = { expected = it },
                placeholder = { Text("Eu esperava que...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                maxLines = 4,
                isError = showErrors && expected.isEmpty(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(Modifier.height(12.dp))

            // Aconteceu
            Text(
                "O que aconteceu? *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = actual,
                onValueChange = { actual = it },
                placeholder = { Text("Mas o que aconteceu foi...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                maxLines = 4,
                isError = showErrors && actual.isEmpty(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(Modifier.height(16.dp))

            // Frequência
            Text(
                "Com que frequência isso acontece?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = showFrequencyMenu,
                onExpandedChange = { showFrequencyMenu = it }
            ) {
                OutlinedTextField(
                    value = frequency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Selecione") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFrequencyMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                ExposedDropdownMenu(
                    expanded = showFrequencyMenu,
                    onDismissRequest = { showFrequencyMenu = false }
                ) {
                    listOf(
                        "Sempre / Toda vez", "Frequentemente", "Às vezes", "Raramente", "Aconteceu apenas uma vez"
                    ).forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                frequency = item
                                showFrequencyMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Info adicional
            Text(
                "Informações adicionais (opcional)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = additionalInfo,
                onValueChange = { additionalInfo = it },
                placeholder = { Text("Qualquer detalhe extra...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(Modifier.height(16.dp))
        }

        // Botões fixos no rodapé
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
                    if (priority.isNotEmpty() && title.isNotEmpty() && steps.isNotEmpty() &&
                        expected.isNotEmpty() && actual.isNotEmpty()) {
                        isSubmitting = true
                        scope.launch {
                            val success = sendBugReport(priority, category, title, steps, expected, actual, frequency, additionalInfo)
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
fun PriorityOption(
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

suspend fun sendBugReport(
    priority: String,
    category: String,
    title: String,
    steps: String,
    expected: String,
    actual: String,
    frequency: String,
    additionalInfo: String
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val webhook = "https://discordapp.com/api/webhooks/1438399121865179218/oTjebWnvzqCnhuh8ssmGGp-1FqHv3upj7d-qPD1aLikwgZ8q63EefhD9397SxbG8yWS1"

            val embedColor = when(priority) {
                "alta" -> 15158332
                "media" -> 16776960
                else -> 5763719
            }

            val json = JSONObject().apply {
                put("embeds", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("title", "🐛 Novo Bug Reportado")
                        put("color", embedColor)
                        put("fields", org.json.JSONArray().apply {
                            put(JSONObject().apply { put("name", "📌 Prioridade"); put("value", priority.uppercase()); put("inline", true) })
                            put(JSONObject().apply { put("name", "📂 Categoria"); put("value", category.ifEmpty { "Não especificado" }); put("inline", true) })
                            put(JSONObject().apply { put("name", "📝 Título"); put("value", title) })
                            put(JSONObject().apply { put("name", "📄 Passos para Reproduzir"); put("value", steps.take(1000)) })
                            put(JSONObject().apply { put("name", "✅ Esperado"); put("value", expected.take(500)) })
                            put(JSONObject().apply { put("name", "❌ Obtido"); put("value", actual.take(500)) })
                            if (frequency.isNotEmpty()) {
                                put(JSONObject().apply { put("name", "⏱️ Frequência"); put("value", frequency) })
                            }
                            if (additionalInfo.isNotEmpty()) {
                                put(JSONObject().apply { put("name", "ℹ️ Info Adicional"); put("value", additionalInfo.take(500)) })
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