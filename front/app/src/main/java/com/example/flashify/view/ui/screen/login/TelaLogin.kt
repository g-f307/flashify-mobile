package com.example.flashify.view.ui.screen.login

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flashify.R
import com.example.flashify.model.util.MAIN_SCREEN_ROUTE
import com.example.flashify.model.util.REGISTER_SCREEN_ROUTE
import com.example.flashify.viewmodel.LoginUIState
import com.example.flashify.viewmodel.LoginViewModel
import com.example.flashify.viewmodel.SocialLoginViewModel
import com.example.flashify.viewmodel.SocialLoginUIState

@Composable
fun TelaLogin(navController: NavController) {
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // ✅ ATUALIZADO: hiltViewModel()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val socialLoginViewModel: SocialLoginViewModel = hiltViewModel()

    val loginState by loginViewModel.loginState.collectAsStateWithLifecycle()
    val socialLoginState by socialLoginViewModel.socialLoginState.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val isSmallScreen = screenHeight < 600.dp
    val isVerySmallScreen = screenHeight < 500.dp
    val isLargeScreen = screenWidth > 600.dp

    val horizontalPadding = when {
        isLargeScreen -> (screenWidth * 0.25f).coerceAtMost(200.dp)
        else -> 32.dp
    }

    val logoSize = when {
        isVerySmallScreen -> 35.dp
        isSmallScreen -> 40.dp
        else -> 50.dp
    }

    val titleSize = when {
        isVerySmallScreen -> 22.sp
        isSmallScreen -> 24.sp
        else -> 28.sp
    }

    val subtitleSize = when {
        isVerySmallScreen -> 13.sp
        isSmallScreen -> 14.sp
        else -> 16.sp
    }

    val verticalSpacing = when {
        isVerySmallScreen -> 8.dp
        isSmallScreen -> 12.dp
        else -> 16.dp
    }

    val sectionSpacing = when {
        isVerySmallScreen -> 16.dp
        isSmallScreen -> 20.dp
        else -> 24.dp
    }

    val buttonHeight = when {
        isVerySmallScreen -> 44.dp
        isSmallScreen -> 46.dp
        else -> 50.dp
    }

    fun validateFields(): Boolean {
        var isValid = true

        when {
            emailOrUsername.isBlank() -> {
                emailError = "Campo obrigatório"
                isValid = false
            }
            emailOrUsername.length < 3 -> {
                emailError = "Deve ter pelo menos 3 caracteres"
                isValid = false
            }
            else -> emailError = null
        }

        when {
            password.isBlank() -> {
                passwordError = "Campo obrigatório"
                isValid = false
            }
            password.length < 6 -> {
                passwordError = "A senha deve ter pelo menos 6 caracteres"
                isValid = false
            }
            else -> passwordError = null
        }

        return isValid
    }

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
                val errorMessage = when {
                    state.message.contains("401") ||
                            state.message.contains("Unauthorized") ||
                            state.message.contains("incorreto") ->
                        "Email/usuário ou senha incorretos"

                    state.message.contains("404") ||
                            state.message.contains("Not Found") ->
                        "Usuário não encontrado. Verifique seus dados."

                    state.message.contains("network") ||
                            state.message.contains("timeout") ->
                        "Erro de conexão. Verifique sua internet."

                    state.message.contains("500") ->
                        "Erro no servidor. Tente novamente mais tarde."

                    else -> state.message
                }

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
                val errorMessage = when {
                    state.message.contains("cancelled") ||
                            state.message.contains("cancelado") ->
                        "Login cancelado"

                    state.message.contains("network") ->
                        "Erro de conexão. Verifique sua internet."

                    else -> "Erro no login com Google: ${state.message}"
                }

                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                socialLoginViewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding)
                    .padding(vertical = if (isSmallScreen) 16.dp else 32.dp),
                verticalArrangement = if (isSmallScreen)
                    Arrangement.Top
                else
                    Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isSmallScreen) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Image(
                    painter = painterResource(id = R.drawable.flashify),
                    contentDescription = "Logo Flashify",
                    modifier = Modifier.size(logoSize)
                )
                Spacer(modifier = Modifier.height(sectionSpacing))

                Text(
                    text = "Bem-vindo de volta!",
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Inicie sessão para aceder aos seus flashcards.",
                    fontSize = subtitleSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(sectionSpacing))

                OutlinedTextField(
                    value = emailOrUsername,
                    onValueChange = {
                        emailOrUsername = it
                        if (emailError != null) emailError = null
                    },
                    label = { Text("Email ou Utilizador", fontSize = if (isSmallScreen) 13.sp else 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = emailError != null,
                    supportingText = if (emailError != null) {
                        {
                            Text(
                                text = emailError!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = if (isVerySmallScreen) 10.sp else 12.sp
                            )
                        }
                    } else null
                )
                Spacer(modifier = Modifier.height(verticalSpacing))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (passwordError != null) passwordError = null
                    },
                    label = { Text("Senha", fontSize = if (isSmallScreen) 13.sp else 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    isError = passwordError != null,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible)
                                    "Ocultar senha"
                                else
                                    "Mostrar senha",
                                modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp)
                            )
                        }
                    },
                    supportingText = if (passwordError != null) {
                        {
                            Text(
                                text = passwordError!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = if (isVerySmallScreen) 10.sp else 12.sp
                            )
                        }
                    } else null
                )
                Spacer(modifier = Modifier.height(sectionSpacing))

                Button(
                    onClick = {
                        if (validateFields()) {
                            loginViewModel.loginUser(emailOrUsername, password)
                        }
                    },
                    enabled = loginState != LoginUIState.Loading,
                    modifier = Modifier.fillMaxWidth().height(buttonHeight),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (loginState == LoginUIState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            "Entrar",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isSmallScreen) 14.sp else 16.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(sectionSpacing))
                OrDivider(isSmallScreen = isSmallScreen)
                Spacer(modifier = Modifier.height(sectionSpacing))

                GoogleSignInButton(
                    onClick = { socialLoginViewModel.signInWithGoogle() },
                    isLoading = socialLoginState is SocialLoginUIState.Loading,
                    isSmallScreen = isSmallScreen,
                    buttonHeight = buttonHeight
                )

                Spacer(modifier = Modifier.height(sectionSpacing))

                ClickableText(
                    text = AnnotatedString("Ainda não tem uma conta? Registre-se"),
                    onClick = { navController.navigate(REGISTER_SCREEN_ROUTE) },
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSmallScreen) 13.sp else 14.sp
                    )
                )

                if (isSmallScreen) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun OrDivider(isSmallScreen: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Divider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Text(
            text = "OU CONTINUE COM",
            modifier = Modifier.padding(horizontal = if (isSmallScreen) 12.dp else 16.dp),
            fontSize = if (isSmallScreen) 10.sp else 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Divider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean = false,
    isSmallScreen: Boolean = false,
    buttonHeight: androidx.compose.ui.unit.Dp = 50.dp
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth().height(buttonHeight),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Logo",
                modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(if (isSmallScreen) 8.dp else 12.dp))
            Text(
                "Entrar com Google",
                fontSize = if (isSmallScreen) 13.sp else 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}