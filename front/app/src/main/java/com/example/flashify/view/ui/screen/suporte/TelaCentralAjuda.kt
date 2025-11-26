package com.example.flashify.view.ui.screen.suporte

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.flashify.model.manager.ThemeManager
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import kotlinx.coroutines.delay

enum class FormType {
    NONE, BUG, EXPERIENCE, SUGGESTION
}

data class FormOption(
    val type: FormType,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val color: Color,
    val bgColor: Color
)

@Composable
fun TelaCentralAjuda(navController: NavController) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = isSystemInDarkTheme())

    var selectedForm by remember { mutableStateOf(FormType.NONE) }

    val formOptions = remember(isDarkTheme) {
        listOf(
            FormOption(
                type = FormType.BUG,
                icon = Icons.Default.BugReport,
                title = "Relatar um Bug",
                description = "Encontrou algo que não está funcionando? Nos ajude a corrigir!",
                color = if (isDarkTheme) Color(0xFFEF5350) else Color(0xFFD32F2F),
                bgColor = if (isDarkTheme) Color(0xFFEF5350).copy(alpha = 0.2f) else Color(0xFFFFCDD2)
            ),
            FormOption(
                type = FormType.EXPERIENCE,
                icon = Icons.Default.RateReview,
                title = "Relatar Experiência",
                description = "Compartilhe como foi usar o Flashify e nos ajude a melhorar.",
                color = if (isDarkTheme) Color(0xFF42A5F5) else Color(0xFF1976D2),
                bgColor = if (isDarkTheme) Color(0xFF42A5F5).copy(alpha = 0.2f) else Color(0xFFBBDEFB)
            ),
            FormOption(
                type = FormType.SUGGESTION,
                icon = Icons.Default.Lightbulb,
                title = "Sugerir Melhorias",
                description = "Tem uma ideia para tornar o Flashify ainda melhor? Adoramos ouvir!",
                color = if (isDarkTheme) Color(0xFFFFC107) else Color(0xFFF57F17),
                bgColor = if (isDarkTheme) Color(0xFFFFC107).copy(alpha = 0.2f) else Color(0xFFFFF9C4)
            )
        )
    }

    GradientBackgroundScreen(isDarkTheme = isDarkTheme) {
        AnimatedContent(
            targetState = selectedForm,
            transitionSpec = {
                if (targetState == FormType.NONE) {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                } else {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                }
            },
            label = "form_transition"
        ) { currentForm ->
            when (currentForm) {
                FormType.NONE -> CentralAjudaHome(
                    formOptions = formOptions,
                    onFormSelected = { selectedForm = it },
                    onBack = { navController.popBackStack() },
                    isDarkTheme = isDarkTheme
                )
                FormType.BUG -> BugReportForm(
                    onBack = { selectedForm = FormType.NONE },
                    isDarkTheme = isDarkTheme
                )
                FormType.EXPERIENCE -> ExperienceForm(
                    onBack = { selectedForm = FormType.NONE },
                    isDarkTheme = isDarkTheme
                )
                FormType.SUGGESTION -> SuggestionForm(
                    onBack = { selectedForm = FormType.NONE },
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentralAjudaHome(
    formOptions: List<FormOption>,
    onFormSelected: (FormType) -> Unit,
    onBack: () -> Unit,
    isDarkTheme: Boolean
) {
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
                    contentDescription = "Voltar",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Central de Suporte",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Conteúdo scrollável
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            Text(
                "Como podemos ajudar você hoje? Escolha uma opção abaixo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            // Cards de opções
            formOptions.forEachIndexed { index, option ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClick = { onFormSelected(option.type) },
                            onClickLabel = option.title
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.5.dp, option.color.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(option.bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                tint = option.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                option.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                option.description,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                if (index < formOptions.size - 1) {
                    Spacer(Modifier.height(10.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Card de dica
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "💡 Dica",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "Quanto mais detalhes você fornecer, melhor conseguiremos entender e atender sua solicitação. Agradecemos por dedicar seu tempo para nos ajudar a melhorar!",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun SuccessScreen(
    title: String,
    message: String,
    iconColor: Color,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean
) {
    LaunchedEffect(Unit) {
        delay(2500)
        onDismiss()
    }

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                title,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Text(
                message,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
        }
    }
}