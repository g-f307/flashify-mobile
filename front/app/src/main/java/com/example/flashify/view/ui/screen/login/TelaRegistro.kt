package com.example.flashify.view.ui.screen.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
    viewModel: RegisterViewModel = viewModel() // 1. Injeta o ViewModel na tela
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 2. Observa o estado da UI a partir do ViewModel
    val uiState by viewModel.registerState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 3. Bloco que reage a mudanças no estado para mostrar mensagens e navegar
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is RegisterUIState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                // Navega para a tela de login após o sucesso do registo
                navController.navigate(LOGIN_SCREEN_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            is RegisterUIState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {} // Não faz nada nos outros estados
        }
    }
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.flashify),
                contentDescription = "Logo Flashify",
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Textos de boas-vindas
            Text(
                text = "Crie a sua conta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Comece a transformar os seus textos em flashcards hoje mesmo.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Campo Nome de Utilizador
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nome de utilizador") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Senha
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

            // Botão Criar conta
            Button(
                onClick = {
                    // 4. Chama a função no ViewModel quando o botão é clicado
                    viewModel.registerUser(username, email, password)
                },
                // 5. O botão fica desativado e mostra um spinner quando 'uiState' for 'Loading'
                enabled = uiState != RegisterUIState.Loading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (uiState == RegisterUIState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Criar conta", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            // Divisor "OU CONTINUE COM"
            OrDivider() // Reutilizando o Composable da tela de Login
            Spacer(modifier = Modifier.height(24.dp))

            // Botão Entrar com Google
            GoogleSignInButton(onClick = { /* TODO: Lógica de login com Google */ }) // Reutilizando
            Spacer(modifier = Modifier.height(32.dp))

            // Link para Iniciar Sessão
            ClickableText(
                text = AnnotatedString("Já tem uma conta? Inicie sessão"),
                onClick = { offset ->
                    navController.navigate(LOGIN_SCREEN_ROUTE) {
                        // Limpa a pilha de navegação para não empilhar telas de login/registo
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}