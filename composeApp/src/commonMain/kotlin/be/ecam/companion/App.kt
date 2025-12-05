package be.ecam.companion

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.ecam.companion.di.appModule
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.core.module.Module

import be.ecam.companion.ui.LoginScreen
import be.ecam.companion.ui.ListAdmins

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(extraModules: List<Module> = emptyList()) {
    KoinApplication(application = { modules(appModule + extraModules) }) {
        MaterialTheme {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

            when (val screen = currentScreen) {
                is Screen.Login -> {
                    LoginScreen(
                        onLoginSuccess = {
                            currentScreen = Screen.AdminList
                        }
                    )
                }
                is Screen.AdminList -> {
                    ListAdmins(
                        onBack = {
                            currentScreen = Screen.Login
                        }
                    )
                }
            }
        }
    }
}

private sealed class Screen {
    object Login : Screen()
    object AdminList : Screen()
}