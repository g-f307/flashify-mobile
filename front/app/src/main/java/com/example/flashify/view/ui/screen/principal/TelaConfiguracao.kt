package com.example.flashify.view.ui.screen.principal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.flashify.model.data.NavItem
import com.example.flashify.model.manager.ProfileImageManager
import com.example.flashify.model.manager.ThemeManager
import com.example.flashify.model.manager.TokenManager
import com.example.flashify.model.util.*
import com.example.flashify.view.ui.components.GradientBackgroundScreen
import com.example.flashify.view.ui.components.NavegacaoBotaoAbaixo
import com.example.flashify.view.ui.components.ProfileAvatarWithIcon
import com.example.flashify.viewmodel.SettingsViewModel
import com.example.flashify.viewmodel.UserState
import kotlinx.coroutines.launch

@Composable
fun TelaPrincipalConfiguracao(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- LÓGICA DO TEMA ---
    val themeManager = remember { ThemeManager(context) }
    val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = isSystemInDarkTheme())

    val tokenManager = remember { TokenManager(context) }
    val profileImageManager = remember { ProfileImageManager(context) } // ✅ NOVO
    val userState by settingsViewModel.userState.collectAsStateWithLifecycle()

    // Cores do Tema
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val cyanColor = if (isDarkTheme) Color(0xFF00BCD4) else Color(0xFF0097A7)

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
            Column(modifier = Modifier.navigationBarsPadding()) {
                NavegacaoBotaoAbaixo(
                    navItems = navItems,
                    selectedItem = selectedItem,
                    onItemSelected = { clickedIndex ->
                        selectedItem = clickedIndex
                        when (navItems[clickedIndex].label) {
                            "Início" -> navController.navigate(MAIN_SCREEN_ROUTE) { popUpTo(CONFIGURATION_SCREEN_ROUTE) { inclusive = true } }
                            "Criar" -> navController.navigate(CREATE_FLASHCARD_ROUTE) { popUpTo(CONFIGURATION_SCREEN_ROUTE) }
                            "Biblioteca" -> navController.navigate(BIBLIOTECA_SCREEN_ROUTE) { popUpTo(CONFIGURATION_SCREEN_ROUTE) }
                            "Progresso" -> navController.navigate(PROGRESSO_SCREEN_ROUTE) { popUpTo(CONFIGURATION_SCREEN_ROUTE) }
                            "Config" -> { /* Já está aqui */ }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        GradientBackgroundScreen(isDarkTheme = isDarkTheme) {
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
                            tint = primaryColor
                        )
                        Text(
                            text = "Configurações",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Profile Card ✅ ATUALIZADO
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (val state = userState) {
                                is UserState.Loading -> {
                                    CircularProgressIndicator(color = primaryColor)
                                }
                                is UserState.Error -> {
                                    Text(state.message, color = MaterialTheme.colorScheme.error)
                                }
                                is UserState.Success -> {
                                    val user = state.user
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // ✅ ALTERADO: Usando o novo componente ProfileAvatarWithIcon
                                        ProfileAvatarWithIcon(
                                            username = user.username,
                                            size = 64.dp,
                                            iconSize = 40.dp,
                                            borderWidth = 2.dp
                                        )

                                        Column {
                                            Text(
                                                user.username,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                user.email,
                                                color = onSurfaceVariant,
                                                fontSize = 14.sp
                                            )
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                icon = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.WbSunny,
                                text = "Modo Escuro",
                                description = if (isDarkTheme) "Tema escuro ativado" else "Tema claro ativado",
                                checked = isDarkTheme,
                                onCheckedChange = {
                                    scope.launch { themeManager.toggleTheme() }
                                },
                                accentColor = cyanColor
                            )
                        }
                    }
                }

                // Support Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                onClick = { navController.navigate(CENTRAL_AJUDA_ROUTE) }
                            )
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ConfiguracaoClickItem(
                                icon = Icons.Default.Memory,
                                text = "Consumo de IA",
                                onClick = { navController.navigate(CONSUMO_IA_ROUTE) }
                            )
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ConfiguracaoClickItem(
                                icon = Icons.Default.MenuBook,
                                text = "Manual do Usuário",
                                onClick = { navController.navigate(MANUAL_USUARIO_ROUTE) }
                            )
                        }
                    }
                }

                // Logout Button ✅ ATUALIZADO
                item {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                tokenManager.clearToken()
                                profileImageManager.clearProfileImage() // ✅ LIMPAR FOTO
                                navController.navigate(LOGIN_SCREEN_ROUTE) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
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
                        color = onSurfaceVariant,
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
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

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
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
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
                    color = onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
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
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}