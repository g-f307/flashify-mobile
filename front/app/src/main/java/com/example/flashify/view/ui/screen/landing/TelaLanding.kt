package com.example.flashify.view.ui.screen.landing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.flashify.R
import com.example.flashify.model.util.LOGIN_SCREEN_ROUTE
import com.example.flashify.model.util.REGISTER_SCREEN_ROUTE
import kotlinx.coroutines.delay

// ============ CORES DO FLASHIFY ============
val YellowFlashify = Color(0xFFFFC300)
val CyanFlashify = Color(0xFF6BDEF3)
val DarkText = Color(0xFF1C1C1E)
val GrayText = Color(0xFF6E6E73)
val WhiteBackground = Color(0xFFFFFBF0)

// ============ ANIMAÇÕES INTEGRADAS COM TEXTO ============

@Composable
fun FlashcardFeature() {
    var flipped by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(0) }

    val cards = listOf(
        Triple("Qual é o símbolo\ndo ouro?", "Au", Icons.Default.Science),
        Triple("Capital da\nFrança?", "Paris", Icons.Default.Public),
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Animação do card
        Card(
            modifier = Modifier.size(160.dp, 200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (!flipped) Color.White else CyanFlashify
            ),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (!flipped) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            cards[currentIndex].third,
                            contentDescription = null,
                            tint = YellowFlashify,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            cards[currentIndex].first,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = DarkText,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    Text(
                        cards[currentIndex].second,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        // Texto integrado
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Flashcards Inteligentes",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Text(
                "Revise e memorize com repetição espaçada",
                fontSize = 13.sp,
                color = GrayText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
fun QuizFeature() {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(0) }

    val questions = listOf(
        Triple("Capital do Brasil?", listOf("SP", "RJ", "BSB", "SSA"), 2),
        Triple("Planetas no Sistema?", listOf("7", "8", "9", "10"), 1),
        Triple("Maior oceano?", listOf("ATL", "IND", "ART", "PAC"), 3)
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Animação do quiz
        Card(
            modifier = Modifier.width(240.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    questions[currentIndex].first,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                questions[currentIndex].second.forEachIndexed { index, option ->
                    val isSelected = selectedOption == index
                    val isCorrect = index == questions[currentIndex].third
                    val showFeedback = showResult && isSelected

                    val bgColor = when {
                        showFeedback && isCorrect -> Color(0xFFDCFCE7)
                        showFeedback && !isCorrect -> Color(0xFFFEE2E2)
                        isSelected -> YellowFlashify.copy(alpha = 0.2f)
                        else -> Color(0xFFF5F5F5)
                    }

                    val borderColor = when {
                        showFeedback && isCorrect -> Color(0xFF4CAF50)
                        showFeedback && !isCorrect -> Color(0xFFF44336)
                        isSelected -> YellowFlashify
                        else -> Color.Transparent
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        border = BorderStroke(1.5.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${('A' + index)}. $option",
                                fontSize = 12.sp,
                                color = DarkText
                            )

                            if (showFeedback) {
                                Icon(
                                    if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Texto integrado
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Quizzes Automáticos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Text(
                "Teste seus conhecimentos com perguntas geradas por IA",
                fontSize = 13.sp,
                color = GrayText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
fun AIFeature() {
    var step by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            step = (step + 1) % 4
        }
    }

    val steps = listOf(
        "Analisando...",
        "Extraindo...",
        "Gerando...",
        "Pronto!"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Animação
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(YellowFlashify),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(45.dp)
                )
            }

            Text(
                steps[step],
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText
            )

            if (step < 3) {
                LinearProgressIndicator(
                    modifier = Modifier.width(120.dp),
                    color = CyanFlashify,
                    trackColor = Color(0xFFE0E0E0)
                )
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Texto integrado
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "IA Geradora",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Text(
                "Transforme PDFs e textos em flashcards instantaneamente",
                fontSize = 13.sp,
                color = GrayText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

// ============ LANDING PAGE ============

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TelaLanding(navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val isVerySmall = screenHeight < 650.dp
    val isSmall = screenHeight < 750.dp

    val pagerState = rememberPagerState(pageCount = { 3 })

    LaunchedEffect(Unit) {
        while (true) {
            delay(7000)
            val nextPage = (pagerState.currentPage + 1) % 3
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteBackground)
    ) {
        // Decorações sutis
        Box(
            modifier = Modifier
                .size(150.dp)
                .offset(x = 100.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(YellowFlashify.copy(alpha = 0.08f))
                .align(Alignment.TopEnd)
        )
        Box(
            modifier = Modifier
                .size(130.dp)
                .offset(x = (-50).dp, y = 40.dp)
                .clip(CircleShape)
                .background(CyanFlashify.copy(alpha = 0.08f))
                .align(Alignment.BottomStart)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = when {
                        isVerySmall -> 20.dp
                        isSmall -> 24.dp
                        else -> 28.dp
                    }
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header compacto
            CompactHeader(isVerySmall)

            // Carrossel integrado
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        when (page) {
                            0 -> AIFeature()
                            1 -> FlashcardFeature()
                            2 -> QuizFeature()
                        }
                    }
                }

                // Indicadores simples
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(
                                    if (pagerState.currentPage == index) 24.dp else 8.dp,
                                    8.dp
                                )
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (pagerState.currentPage == index)
                                        YellowFlashify
                                    else
                                        Color(0xFFE0E0E0)
                                )
                                .animateContentSize()
                        )
                    }
                }
            }

            // CTAs
            CTAButtons(
                navController,
                isVerySmall = isVerySmall
            )

            Spacer(Modifier.height(if (isVerySmall) 20.dp else 24.dp))
        }
    }
}

@Composable
private fun CompactHeader(isVerySmall: Boolean) {
    var scale by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        delay(100)
        scale = 1f
    }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(top = if (isVerySmall) 32.dp else 40.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.flashify),
            contentDescription = "Logo",
            modifier = Modifier
                .size(if (isVerySmall) 48.dp else 56.dp)
                .scale(animatedScale)
        )

        // Texto com gradiente APENAS aqui
        Text(
            "Flashify",
            fontSize = if (isVerySmall) 32.sp else 36.sp,
            fontWeight = FontWeight.ExtraBold,
            style = LocalTextStyle.current.copy(
                brush = Brush.linearGradient(
                    colors = listOf(YellowFlashify, CyanFlashify)
                )
            )
        )

        Text(
            "Aprenda de forma inteligente",
            fontSize = if (isVerySmall) 13.sp else 14.sp,
            color = GrayText
        )
    }
}

@Composable
private fun CTAButtons(navController: NavController, isVerySmall: Boolean) {
    val buttonHeight = if (isVerySmall) 50.dp else 54.dp
    val fontSize = if (isVerySmall) 14.sp else 15.sp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botão primário - SEM gradiente, cor sólida
        Button(
            onClick = { navController.navigate(REGISTER_SCREEN_ROUTE) },
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
                .shadow(6.dp, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = YellowFlashify,
                contentColor = Color.Black
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Rocket,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Começar Gratuitamente",
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Botão secundário - Borda simples
        OutlinedButton(
            onClick = { navController.navigate(LOGIN_SCREEN_ROUTE) },
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(2.dp, CyanFlashify),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = DarkText
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Login,
                    contentDescription = null,
                    tint = CyanFlashify,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Já tenho conta",
                    fontSize = fontSize,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}