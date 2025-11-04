package com.example.flashify.view.ui.screen.principal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.flashify.model.data.NavItem
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.util.*
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.view.ui.components.NavegacaoBotaoAbaixo
import com.example.flashify.view.ui.theme.TextSecondary
import com.example.flashify.view.ui.theme.YellowAccent
import com.example.flashify.viewmodel.SettingsViewModel
import com.example.flashify.viewmodel.UserState

@Composable
fun TelaPrincipalConfiguracao(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val userState by settingsViewModel.userState.collectAsStateWithLifecycle()

    var selectedItem by remember { mutableStateOf(4) }
    val navItems = listOf(
        NavItem("Início", Icons.Default.Home),
        NavItem("Biblioteca", Icons.Default.FormatListBulleted),
        NavItem("Criar", Icons.Default.Add),
        NavItem("Progresso", Icons.Default.TrendingUp),
        NavItem("Config", Icons.Default.Settings)
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                NavegacaoBotaoAbaixo(
                    modifier = Modifier.clip(RoundedCornerShape(50)),
                    navItems = navItems,
                    selectedItem = selectedItem,
                    onItemSelected = { clickedIndex ->
                        selectedItem = clickedIndex
                        when (navItems[clickedIndex].label) {
                            "Início" -> navController.navigate(MAIN_SCREEN_ROUTE) {
                                popUpTo(CONFIGURATION_SCREEN_ROUTE) { inclusive = true }
                            }
                            "Criar" -> navController.navigate(CREATE_FLASHCARD_ROUTE) {
                                popUpTo(CONFIGURATION_SCREEN_ROUTE)
                            }
                            "Biblioteca" -> navController.navigate(BIBLIOTECA_SCREEN_ROUTE) {
                                popUpTo(CONFIGURATION_SCREEN_ROUTE)
                            }
                            "Progresso" -> navController.navigate(PROGRESSO_SCREEN_ROUTE) {
                                popUpTo(CONFIGURATION_SCREEN_ROUTE)
                            }
                            "Config" -> { /* Já está aqui */ }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        GradientBackgroundScreen {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurações",
                            modifier = Modifier.size(32.dp),
                            tint = YellowAccent
                        )
                        Text(
                            text = "Configurações",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Profile Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (val state = userState) {
                                is UserState.Loading -> {
                                    CircularProgressIndicator(color = YellowAccent)
                                }
                                is UserState.Error -> {
                                    Text(
                                        state.message,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                is UserState.Success -> {
                                    val user = state.user
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(CircleShape)
                                                    .background(YellowAccent.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AccountCircle,
                                                    contentDescription = "Foto de Perfil",
                                                    modifier = Modifier.size(40.dp),
                                                    tint = YellowAccent
                                                )
                                            }
                                            Column {
                                                Text(
                                                    user.username,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    user.email,
                                                    color = TextSecondary,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { /* TODO: Editar perfil */ },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = YellowAccent
                                            )
                                        ) {
                                            Text("Editar Perfil", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Preferences Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Preferências",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            ConfiguracaoSwitchItem(
                                icon = Icons.Default.Notifications,
                                text = "Notificações",
                                description = "Receber lembretes de estudo",
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it }
                            )
                            Divider(color = Color.Gray.copy(alpha = 0.3f))
                            ConfiguracaoSwitchItem(
                                icon = Icons.Default.DarkMode,
                                text = "Modo Escuro",
                                description = "Tema escuro ativado",
                                checked = darkModeEnabled,
                                onCheckedChange = { darkModeEnabled = it }
                            )
                            Divider(color = Color.Gray.copy(alpha = 0.3f))
                            ConfiguracaoSwitchItem(
                                icon = Icons.Default.VolumeUp,
                                text = "Som",
                                description = "Efeitos sonoros",
                                checked = soundEnabled,
                                onCheckedChange = { soundEnabled = it }
                            )
                        }
                    }
                }

                // Support Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Suporte",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            ConfiguracaoClickItem(
                                icon = Icons.Default.HelpOutline,
                                text = "Central de Ajuda",
                                onClick = { /* TODO */ }
                            )
                            Divider(color = Color.Gray.copy(alpha = 0.3f))
                            ConfiguracaoClickItem(
                                icon = Icons.Default.Description,
                                text = "Termos de Uso",
                                onClick = { /* TODO */ }
                            )
                            Divider(color = Color.Gray.copy(alpha = 0.3f))
                            ConfiguracaoClickItem(
                                icon = Icons.Default.Shield,
                                text = "Política de Privacidade",
                                onClick = { /* TODO */ }
                            )
                        }
                    }
                }

                // Logout Button
                item {
                    OutlinedButton(
                        onClick = {
                            tokenManager.clearToken()
                            navController.navigate(LOGIN_SCREEN_ROUTE) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF44336)
                        )
                    ) {
                        Icon(Icons.Default.ExitToApp, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Sair da Conta", fontWeight = FontWeight.Bold)
                    }
                }

                // Version info
                item {
                    Text(
                        "Versão 2.0.0",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ConfiguracaoSwitchItem(
    icon: ImageVector,
    text: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(YellowAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = YellowAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    description,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = YellowAccent,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun ConfiguracaoClickItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary
        )
    }
}
