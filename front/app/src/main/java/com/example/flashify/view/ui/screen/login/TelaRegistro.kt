package com.example.flashify.view.ui.screen.login

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flashify.R
import com.example.flashify.model.util.LOGIN_SCREEN_ROUTE
import com.example.flashify.view.ui.theme.CyanFlashify
import com.example.flashify.view.ui.theme.DarkText
import com.example.flashify.view.ui.theme.LightTextSecondary
import com.example.flashify.view.ui.theme.YellowFlashify
import com.example.flashify.viewmodel.RegisterUIState
import com.example.flashify.viewmodel.RegisterViewModel
import com.example.flashify.viewmodel.SocialLoginViewModel
import com.example.flashify.viewmodel.SocialLoginUIState

@Composable
fun TelaRegistro(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.registerState.collectAsStateWithLifecycle()
    val socialLoginViewModel: SocialLoginViewModel = hiltViewModel()
    val socialLoginState by socialLoginViewModel.socialLoginState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeight = configuration.screenHeightDp.dp

    // Estado de expansão
    var isExpanded by remember { mutableStateOf(false) }

    // Variável para acumular o arrasto e evitar disparos acidentais
    var dragOffset by remember { mutableFloatStateOf(0f) }

    // Define a sensibilidade do arrasto (ex: 50dp)
    val dragThreshold = with(density) { 50.dp.toPx() }

    val scrollState = rememberScrollState()

    // Animação da posição do cartão branco
    val topPadding by animateDpAsState(
        targetValue = if (isExpanded) screenHeight * 0.15f else screenHeight * 0.40f,
        animationSpec = tween(durationMillis = 500),
        label = "TopPaddingAnimation"
    )

    // NestedScrollConnection para capturar o drag
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0 && !isExpanded) {
                    dragOffset += available.y
                    if (dragOffset < -dragThreshold) {
                        isExpanded = true
                        dragOffset = 0f
                    }
                    return available
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0 && isExpanded) {
                    dragOffset += available.y
                    if (dragOffset > dragThreshold) {
                        isExpanded = false
                        dragOffset = 0f
                    }
                    return available
                }
                return Offset.Zero
            }
        }
    }

    // Reseta o offset se o estado mudar
    LaunchedEffect(isExpanded) {
        dragOffset = 0f
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is RegisterUIState.Success -> {
                Toast.makeText(context, "Cadastro realizado!", Toast.LENGTH_LONG).show()
                viewModel.resetState()
                navController.navigate(LOGIN_SCREEN_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            is RegisterUIState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    LaunchedEffect(socialLoginState) {
        when (val state = socialLoginState) {
            is SocialLoginUIState.Success -> {
                Toast.makeText(context, "Cadastro com Google bem-sucedido!", Toast.LENGTH_SHORT).show()
                socialLoginViewModel.resetState()
                navController.navigate(com.example.flashify.model.util.MAIN_SCREEN_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is SocialLoginUIState.Error -> {
                Toast.makeText(context, "Erro no cadastro com Google", Toast.LENGTH_LONG).show()
                socialLoginViewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyanFlashify)
            .imePadding() // AJUSTA QUANDO O TECLADO APARECE
    ) {
        // Header fixo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topPadding)
        ) {
            AuthHeaderSection(
                title = "Crie sua conta",
                subtitle = "no Flashify",
                proportion = 1f
            )
        }

        // Container do Formulário (Cartão Branco)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 16.dp,
                border = BorderStroke(3.dp, CyanFlashify)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Indicador visual de arrasto
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(LightTextSecondary.copy(alpha = 0.4f))
                            .clickable { isExpanded = !isExpanded }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Crie sua conta",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 4.dp)
                    )

                    // --- Campos Sempre Visíveis ---
                    AuthTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Usuário",
                        icon = Icons.Default.Person
                    )

                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        icon = Icons.Default.Email
                    )

                    // --- Botão/Hint para expandir ---
                    AnimatedVisibility(
                        visible = !isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .clickable { isExpanded = true },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expandir",
                                tint = CyanFlashify,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Deslize para cima ou toque aqui",
                                color = CyanFlashify,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // --- Campos Condicionais (Senha) ---
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                        exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AuthTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Senha",
                                icon = Icons.Default.Lock,
                                isPassword = true,
                                passwordVisible = passwordVisible,
                                onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
                            )

                            Text(
                                text = "Mínimo 8 caracteres, 1 maiúscula, 1 número",
                                fontSize = 10.sp,
                                color = LightTextSecondary,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 4.dp)
                                    .offset(y = (-6).dp)
                            )

                            AuthTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = "Confirmar Senha",
                                icon = Icons.Default.Lock,
                                isPassword = true,
                                passwordVisible = confirmPasswordVisible,
                                onPasswordVisibilityChange = {
                                    confirmPasswordVisible = !confirmPasswordVisible
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { viewModel.registerUser(username, email, password) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = YellowFlashify,
                                    disabledContainerColor = YellowFlashify.copy(alpha = 0.6f)
                                ),
                                elevation = ButtonDefaults.buttonElevation(8.dp),
                                enabled = uiState !is RegisterUIState.Loading
                            ) {
                                if (uiState is RegisterUIState.Loading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "CRIAR CONTA",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    thickness = 1.dp,
                                    color = LightTextSecondary.copy(alpha = 0.3f)
                                )
                                Text("ou", color = LightTextSecondary, fontSize = 11.sp)
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    thickness = 1.dp,
                                    color = LightTextSecondary.copy(alpha = 0.3f)
                                )
                            }

                            SocialLoginButton(
                                iconRes = R.drawable.ic_google_logo,
                                onClick = { socialLoginViewModel.signInWithGoogle() },
                                isLoading = socialLoginState is SocialLoginUIState.Loading
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Já tem uma conta? ", color = LightTextSecondary, fontSize = 13.sp)
                        Text(
                            "Faça login",
                            color = CyanFlashify,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable {
                                navController.navigate(LOGIN_SCREEN_ROUTE) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}