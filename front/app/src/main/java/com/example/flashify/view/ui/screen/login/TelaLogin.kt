package com.example.flashify.view.ui.screen.login

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flashify.R
import com.example.flashify.model.util.MAIN_SCREEN_ROUTE
import com.example.flashify.model.util.REGISTER_SCREEN_ROUTE
import com.example.flashify.view.ui.theme.CyanFlashify
import com.example.flashify.view.ui.theme.DarkText
import com.example.flashify.view.ui.theme.LightTextSecondary
import com.example.flashify.view.ui.theme.YellowFlashify
import com.example.flashify.viewmodel.LoginUIState
import com.example.flashify.viewmodel.LoginViewModel
import com.example.flashify.viewmodel.SocialLoginViewModel
import com.example.flashify.viewmodel.SocialLoginUIState

@Composable
fun TelaLogin(navController: NavController) {
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val loginViewModel: LoginViewModel = hiltViewModel()
    val socialLoginViewModel: SocialLoginViewModel = hiltViewModel()

    val loginState by loginViewModel.loginState.collectAsStateWithLifecycle()
    val socialLoginState by socialLoginViewModel.socialLoginState.collectAsStateWithLifecycle()

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginUIState.Success -> {
                Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                loginViewModel.resetState()
                navController.navigate(MAIN_SCREEN_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is LoginUIState.Error -> {
                val errorMessage = state.message.takeIf { it.isNotBlank() } ?: "Erro ao fazer login"
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                loginViewModel.resetState()
            }
            else -> {}
        }
    }

    LaunchedEffect(socialLoginState) {
        when (val state = socialLoginState) {
            is SocialLoginUIState.Success -> {
                Toast.makeText(context, "Login com Google bem-sucedido!", Toast.LENGTH_SHORT).show()
                socialLoginViewModel.resetState()
                navController.navigate(MAIN_SCREEN_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is SocialLoginUIState.Error -> {
                Toast.makeText(context, "Erro no login com Google", Toast.LENGTH_LONG).show()
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Header com 40% da tela (reduzido para caber tudo)
            AuthHeaderSection(
                title = "Bem-vindo de volta",
                subtitle = "ao Flashify",
                proportion = 0.40f
            )

            // Formulário - SEM SCROLL, TUDO DEVE CABER NA TELA
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 16.dp,
                border = BorderStroke(3.dp, CyanFlashify)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()) // SCROLL PARA QUANDO TECLADO APARECE
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Inicie sessão",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 2.dp)
                    )

                    AuthTextField(
                        value = emailOrUsername,
                        onValueChange = { emailOrUsername = it },
                        label = "Email ou Usuário",
                        icon = Icons.Default.Email
                    )

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
                        text = "Esqueceu a senha?",
                        color = CyanFlashify,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { }
                    )

                    Button(
                        onClick = { loginViewModel.loginUser(emailOrUsername, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YellowFlashify,
                            disabledContainerColor = YellowFlashify.copy(alpha = 0.6f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(8.dp),
                        enabled = loginState !is LoginUIState.Loading
                    ) {
                        if (loginState is LoginUIState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("ENTRAR", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    val errorMessage = (loginState as? LoginUIState.Error)?.message
                    AnimatedVisibility(visible = errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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

                    // LINK PARA REGISTRO - COMPACTO E SEM ESPAÇOS EXTRAS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Não tem uma conta? ",
                            color = LightTextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            "Criar",
                            color = CyanFlashify,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable {
                                navController.navigate(REGISTER_SCREEN_ROUTE)
                            }
                        )
                    }
                }
            }
        }
    }
}