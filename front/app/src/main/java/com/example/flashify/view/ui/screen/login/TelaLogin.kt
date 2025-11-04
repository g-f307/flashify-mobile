package com.example.flashify.view.ui.screen.login // Certifique-se que o package está correto

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider // Import necessário
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.flashify.R
import com.example.flashify.model.util.MAIN_SCREEN_ROUTE
import com.example.flashify.model.util.REGISTER_SCREEN_ROUTE
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.viewmodel.LoginUIState
import com.example.flashify.viewmodel.LoginViewModel

@Composable
fun TelaLogin(
    navController: NavController
) {
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // --- CORREÇÃO JÁ APLICADA ---
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: LoginViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    )
    // --- FIM DA CORREÇÃO ---

    val uiState by viewModel.loginState.collectAsStateWithLifecycle()
//    val tokenManager = remember { TokenManager(context) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginUIState.Success -> {
                Toast.makeText(context, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()

                // ▼▼▼ REMOVA ESTA LINHA ▼▼▼
                // tokenManager.saveAuthData(state.token, -1) // <-- REMOVER (O ViewModel já fez isso!)

                // Resetar estado antes de navegar pode ser mais seguro
                viewModel.resetState()
                navController.navigate(MAIN_SCREEN_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true // Evita múltiplas instâncias da tela principal
                }
            }
            is LoginUIState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState() // Reseta o estado após mostrar o erro
            }
            else -> {}
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.flashify),
                contentDescription = "Logo Flashify",
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Bem-vindo de volta!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Inicie sessão para aceder aos seus flashcards.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = emailOrUsername,
                onValueChange = { emailOrUsername = it },
                label = { Text("Email ou Nome de Utilizador") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.loginUser(emailOrUsername, password)
                },
                enabled = uiState != LoginUIState.Loading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (uiState == LoginUIState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Entrar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp)) // Espaço antes do divisor
            OrDivider()
            Spacer(modifier = Modifier.height(24.dp)) // Espaço depois do divisor


            // Botão Entrar com Google (Verifique se o ícone existe em res/drawable)
            GoogleSignInButton(onClick = { /* TODO: Lógica de login com Google */ })
            Spacer(modifier = Modifier.height(32.dp))

            ClickableText(
                text = AnnotatedString("Ainda não tem uma conta? Registre-se"),
                onClick = { offset ->
                    navController.navigate(REGISTER_SCREEN_ROUTE)
                },
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        Text(
            text = "OU CONTINUE COM",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
    ) {
        // Certifique-se que 'ic_google_logo' existe em res/drawable
        Icon(
            painter = painterResource(id = R.drawable.ic_google_logo),
            contentDescription = "Google Logo",
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text("Entrar com Google", color = MaterialTheme.colorScheme.onBackground)
    }
}