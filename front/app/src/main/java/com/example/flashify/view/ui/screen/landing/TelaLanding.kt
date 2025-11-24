package com.example.flashify.view.ui.screen.landing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.flashify.model.util.LOGIN_SCREEN_ROUTE
import com.example.flashify.model.util.REGISTER_SCREEN_ROUTE
import com.example.flashify.view.ui.theme.CyanFlashify
import com.example.flashify.view.ui.theme.YellowFlashify
import kotlinx.coroutines.delay

val DarkText = Color(0xFF1C1C1E)
val WhiteText = Color(0xFFFFFFFF)

@Composable
fun TelaLanding(navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // Sistema de escala mais agressivo e preciso
    val scale = when {
        screenHeight < 650.dp -> 0.65f   // Muito pequeno
        screenHeight < 730.dp -> 0.75f   // Pequeno
        screenHeight < 820.dp -> 0.85f   // Médio-pequeno
        screenHeight < 900.dp -> 0.92f   // Médio
        else -> 1f                        // Grande
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CyanFlashify,
                        CyanFlashify.copy(alpha = 0.9f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(
                    top = (4 * scale).dp,
                    bottom = (8 * scale).dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.08f))

            // ===== TEXTO HERO (COMPACTO) =====
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Texto com palavra "Otimize" em amarelo
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy((-4 * scale).dp)
                ) {
                    Text(
                        text = "Otimize",
                        fontSize = (46 * scale).sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = YellowFlashify,
                        lineHeight = (50 * scale).sp,
                        letterSpacing = (-1.2).sp,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.2f),
                                offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                    Text(
                        text = "a maneira como você estuda.",
                        fontSize = (46 * scale).sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WhiteText,
                        lineHeight = (50 * scale).sp,
                        letterSpacing = (-1.2).sp,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.2f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }

                Spacer(Modifier.height((12 * scale).dp))

                Text(
                    text = "O Flashify usa IA para extrair o essencial de qualquer texto e criar decks de estudo.",
                    fontSize = (16 * scale).sp,
                    color = WhiteText.copy(alpha = 0.95f),
                    lineHeight = (23 * scale).sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.weight(0.08f))

            // ===== ANIMAÇÕES SOBREPOSTAS =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((300 * scale).dp)
                    .offset(y = (-30 * scale).dp),
                contentAlignment = Alignment.Center
            ) {
                // Quiz à esquerda (mais sobreposto)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = (20 * scale).dp, y = (30 * scale).dp)
                        .rotate(-8f)
                ) {
                    QuizAnimationCard(scale = scale)
                }

                // Flashcard à direita (mais sobreposto)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = (-20 * scale).dp, y = (-30 * scale).dp)
                        .rotate(8f)
                ) {
                    FlashcardAnimationCard(scale = scale)
                }
            }

            Spacer(Modifier.weight(0.05f))

            // ===== BOTÕES =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .padding(bottom = (70 * scale).dp),
                verticalArrangement = Arrangement.spacedBy((14 * scale).dp)
            ) {
                Button(
                    onClick = { navController.navigate(REGISTER_SCREEN_ROUTE) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((54 * scale).dp)
                        .shadow(12.dp, RoundedCornerShape(30.dp)),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YellowFlashify,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "CADASTRE-SE",
                        fontSize = (16 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Já tem uma conta? ",
                        color = WhiteText.copy(alpha = 0.9f),
                        fontSize = (14 * scale).sp
                    )
                    Text(
                        "Entrar",
                        color = YellowFlashify,
                        fontWeight = FontWeight.Bold,
                        fontSize = (14 * scale).sp,
                        modifier = Modifier.clickable {
                            navController.navigate(LOGIN_SCREEN_ROUTE)
                        }
                    )
                }
            }
        }
    }
}

// ===== FLASHCARD ANIMATION =====
@Composable
fun FlashcardAnimationCard(scale: Float) {
    var flipped by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(0) }

    val cards = listOf(
        Triple("Qual é o\nsímbolo\ndo ouro?", "Au", Icons.Default.Science),
        Triple("Capital\nda\nFrança?", "Paris", Icons.Default.Public),
        Triple("√16 = ?", "4", Icons.Default.Calculate)
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(2500)
            flipped = true
            delay(1800)
            flipped = false
            delay(300)
            currentIndex = (currentIndex + 1) % cards.size
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(600)
    )

    Card(
        modifier = Modifier.size(
            width = (210 * scale).dp,
            height = (270 * scale).dp
        ),
        shape = RoundedCornerShape((16 * scale).dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!flipped) Color.White else YellowFlashify
        ),
        elevation = CardDefaults.cardElevation((12 * scale).dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (!flipped) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy((16 * scale).dp),
                    modifier = Modifier.padding((20 * scale).dp)
                ) {
                    Icon(
                        cards[currentIndex].third,
                        contentDescription = null,
                        tint = CyanFlashify,
                        modifier = Modifier.size((50 * scale).dp)
                    )
                    Text(
                        cards[currentIndex].first,
                        fontSize = (16 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = DarkText,
                        lineHeight = (22 * scale).sp
                    )
                }
            } else {
                Text(
                    cards[currentIndex].second,
                    fontSize = (44 * scale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

// ===== QUIZ ANIMATION =====
@Composable
fun QuizAnimationCard(scale: Float) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(0) }

    val questions = listOf(
        Triple("Quantos planetas no Sistema Solar?", listOf("7", "8", "9", "10"), 1),
        Triple("Qual é a capital do Brasil?", listOf("SP", "RJ", "BSB", "SSA"), 2),
        Triple("Qual o maior oceano?", listOf("Atl.", "Ânt.", "Árt.", "Pac."), 3)
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            selectedOption = questions[currentIndex].third
            delay(400)
            showResult = true
            delay(1800)
            selectedOption = null
            showResult = false
            delay(200)
            currentIndex = (currentIndex + 1) % questions.size
        }
    }

    Card(
        modifier = Modifier.size(
            width = (210 * scale).dp,
            height = (310 * scale).dp
        ),
        shape = RoundedCornerShape((16 * scale).dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation((12 * scale).dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = (16 * scale).dp,
                    vertical = (16 * scale).dp
                ),
            verticalArrangement = Arrangement.spacedBy((10 * scale).dp)
        ) {
            // Pergunta
            Text(
                questions[currentIndex].first,
                fontSize = (14 * scale).sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                lineHeight = (20 * scale).sp
            )

            // Opções
            questions[currentIndex].second.forEachIndexed { index, option ->
                val isSelected = selectedOption == index
                val isCorrect = index == questions[currentIndex].third
                val showFeedback = showResult && isSelected

                val bgColor = when {
                    showFeedback && isCorrect -> Color(0xFFDCFCE7)
                    showFeedback && !isCorrect -> Color(0xFFFEE2E2)
                    isSelected -> CyanFlashify.copy(alpha = 0.15f)
                    else -> Color(0xFFF5F5F5)
                }

                val borderColor = when {
                    showFeedback && isCorrect -> Color(0xFF4CAF50)
                    showFeedback && !isCorrect -> Color(0xFFF44336)
                    isSelected -> CyanFlashify
                    else -> Color.Transparent
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape((10 * scale).dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    border = BorderStroke((2 * scale).dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding((10 * scale).dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${('A' + index)}. $option",
                            fontSize = (12 * scale).sp,
                            color = DarkText,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )

                        if (showFeedback) {
                            Icon(
                                if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                                modifier = Modifier.size((16 * scale).dp)
                            )
                        }
                    }
                }
            }
        }
    }
}