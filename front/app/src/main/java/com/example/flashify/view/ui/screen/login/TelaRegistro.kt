package com.example.flashify.view.ui.screen.login

import android.widget.Toast
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.flashify.R
import com.example.flashify.model.util.LOGIN_SCREEN_ROUTE
import com.example.flashify.viewmodel.RegisterUIState
import com.example.flashify.viewmodel.RegisterViewModel

@Composable
fun TelaRegistro(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Erros de validação
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.registerState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Obter configuração da tela
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // Calcular tamanhos responsivos
    val isSmallScreen = screenHeight < 650.dp
    val isVerySmallScreen = screenHeight < 550.dp
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
        isSmallScreen -> 10.dp
        else -> 16.dp
    }

    val sectionSpacing = when {
        isVerySmallScreen -> 12.dp
        isSmallScreen -> 16.dp
        else -> 24.dp
    }

    val buttonHeight = when {
        isVerySmallScreen -> 44.dp
        isSmallScreen -> 46.dp
        else -> 50.dp
    }

    val fieldFontSize = when {
        isVerySmallScreen -> 12.sp
        isSmallScreen -> 13.sp
        else -> 14.sp
    }

    val supportTextSize = when {
        isVerySmallScreen -> 9.sp
        isSmallScreen -> 10.sp
        else -> 12.sp
    }

    // Função de validação de email
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Função de validação de senha forte
    fun validatePasswordStrength(password: String): String? {
        return when {
            password.length < 8 -> "Mínimo 8 caracteres"
            !password.any { it.isDigit() } -> "Adicione um número"
            !password.any { it.isUpperCase() } -> "Adicione maiúscula"
            !password.any { it.isLowerCase() } -> "Adicione minúscula"
            else -> null
        }
    }

    // Função de validação completa
    fun validateFields(): Boolean {
        var isValid = true

        when {
            username.isBlank() -> {
                usernameError = "Campo obrigatório"
                isValid = false
            }
            username.length < 3 -> {
                usernameError = "Mínimo 3 caracteres"
                isValid = false
            }
            username.length > 20 -> {
                usernameError = "Máximo 20 caracteres"
                isValid = false
            }
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> {
                usernameError = "Apenas letras, números e _"
                isValid = false
            }
            else -> usernameError = null
        }

        when {
            email.isBlank() -> {
                emailError = "Campo obrigatório"
                isValid = false
            }
            !isValidEmail(email) -> {
                emailError = "Email inválido"
                isValid = false
            }
            else -> emailError = null
        }

        when {
            password.isBlank() -> {
                passwordError = "Campo obrigatório"
                isValid = false
            }
            else -> {
                passwordError = validatePasswordStrength(password)
                if (passwordError != null) isValid = false
            }
        }

        when {
            confirmPassword.isBlank() -> {
                confirmPasswordError = "Campo obrigatório"
                isValid = false
            }
            confirmPassword != password -> {
                confirmPasswordError = "Senhas não coincidem"
                isValid = false
            }
            else -> confirmPasswordError = null
        }

        return isValid
    }

    // Observar estado da UI
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is RegisterUIState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                navController.navigate(LOGIN_SCREEN_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            is RegisterUIState.Error -> {
                when {
                    state.message.contains("username", ignoreCase = true) &&
                            state.message.contains("exists", ignoreCase = true) -> {
                        usernameError = "Usuário já existe"
                        Toast.makeText(
                            context,
                            "Nome de usuário já cadastrado. Escolha outro.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    state.message.contains("email", ignoreCase = true) &&
                            state.message.contains("exists", ignoreCase = true) -> {
                        emailError = "Email já cadastrado"
                        Toast.makeText(
                            context,
                            "Email já cadastrado. Faça login ou use outro email.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    state.message.contains("password", ignoreCase = true) -> {
                        passwordError = "Senha insegura"
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }

                    state.message.contains("network") ||
                            state.message.contains("timeout") -> {
                        Toast.makeText(
                            context,
                            "Erro de conexão. Verifique sua internet.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    state.message.contains("500") -> {
                        Toast.makeText(
                            context,
                            "Erro no servidor. Tente novamente mais tarde.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                }
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
                    .padding(vertical = if (isSmallScreen) 12.dp else 24.dp),
                verticalArrangement = if (isSmallScreen)
                    Arrangement.Top
                else
                    Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isSmallScreen) {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Logo
                Image(
                    painter = painterResource(id = R.drawable.flashify),
                    contentDescription = "Logo Flashify",
                    modifier = Modifier.size(logoSize)
                )
                Spacer(modifier = Modifier.height(sectionSpacing))

                // Textos de boas-vindas
                Text(
                    text = "Crie a sua conta",
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Comece a estudar de forma mais inteligente.",
                    fontSize = subtitleSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(sectionSpacing))

                // Campo Nome de Utilizador
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        if (usernameError != null) usernameError = null
                    },
                    label = { Text("Usuário", fontSize = fieldFontSize) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = usernameError != null,
                    supportingText = {
                        Text(
                            text = usernameError ?: "Letras, números e _",
                            color = if (usernameError != null)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = supportTextSize
                        )
                    }
                )
                Spacer(modifier = Modifier.height(verticalSpacing))

                // Campo de Email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null) emailError = null
                    },
                    label = { Text("E-mail", fontSize = fieldFontSize) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = emailError != null,
                    supportingText = if (emailError != null) {
                        {
                            Text(
                                text = emailError!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = supportTextSize
                            )
                        }
                    } else null
                )
                Spacer(modifier = Modifier.height(verticalSpacing))

                // Campo de Senha
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (passwordError != null) passwordError = null
                    },
                    label = { Text("Senha", fontSize = fieldFontSize) },
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
                                    "Ocultar"
                                else
                                    "Mostrar",
                                modifier = Modifier.size(if (isSmallScreen) 18.dp else 20.dp)
                            )
                        }
                    },
                    supportingText = {
                        Text(
                            text = passwordError ?: "8+ caracteres, 1 maiúscula, 1 número",
                            color = if (passwordError != null)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = supportTextSize
                        )
                    }
                )
                Spacer(modifier = Modifier.height(verticalSpacing))

                // Campo de Confirmação de Senha
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        if (confirmPasswordError != null) confirmPasswordError = null
                    },
                    label = { Text("Confirmar Senha", fontSize = fieldFontSize) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    isError = confirmPasswordError != null,
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible)
                                    "Ocultar"
                                else
                                    "Mostrar",
                                modifier = Modifier.size(if (isSmallScreen) 18.dp else 20.dp)
                            )
                        }
                    },
                    supportingText = if (confirmPasswordError != null) {
                        {
                            Text(
                                text = confirmPasswordError!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = supportTextSize
                            )
                        }
                    } else null
                )
                Spacer(modifier = Modifier.height(sectionSpacing))

                // Botão Criar conta
                Button(
                    onClick = {
                        if (validateFields()) {
                            viewModel.registerUser(username, email, password)
                        }
                    },
                    enabled = uiState != RegisterUIState.Loading,
                    modifier = Modifier.fillMaxWidth().height(buttonHeight),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (uiState == RegisterUIState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            "Criar conta",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isSmallScreen) 14.sp else 16.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(sectionSpacing))

                // Divisor "OU CONTINUE COM"
                OrDivider(isSmallScreen = isSmallScreen)
                Spacer(modifier = Modifier.height(sectionSpacing))

                // Botão Entrar com Google
                GoogleSignInButton(
                    onClick = { /* TODO: Lógica de login com Google */ },
                    isSmallScreen = isSmallScreen,
                    buttonHeight = buttonHeight
                )

                Spacer(modifier = Modifier.height(sectionSpacing))

                // Link para Iniciar Sessão
                ClickableText(
                    text = AnnotatedString("Já tem uma conta? Faça login"),
                    onClick = {
                        navController.navigate(LOGIN_SCREEN_ROUTE) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    },
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSmallScreen) 13.sp else 14.sp
                    )
                )

                if (isSmallScreen) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}