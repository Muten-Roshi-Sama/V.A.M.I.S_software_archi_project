package be.ecam.companion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.ecam.companion.data.SettingsRepository
import be.ecam.companion.di.appModule
import be.ecam.companion.ui.CalendarScreen
import be.ecam.companion.ui.SettingsScreen
import be.ecam.companion.ui.LoginScreen
import be.ecam.companion.viewmodel.HomeViewModel
import companion.composeapp.generated.resources.Res
import companion.composeapp.generated.resources.calendar
import companion.composeapp.generated.resources.home
import companion.composeapp.generated.resources.settings
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.Module

// ===== IMPORTS UI =====
import be.ecam.companion.ui.ListAdmins
import be.ecam.companion.ui.Screen
import be.ecam.companion.ui.HomeScreen
import be.ecam.companion.ui.CalendarScreen
import be.ecam.companion.ui.SettingsScreen
import be.ecam.companion.ui.DataStudentsScreen   //Add

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(extraModules: List<Module> = emptyList()) {
    KoinApplication(application = { modules(appModule + extraModules) }) {
        val vm = koinInject<HomeViewModel>()
        MaterialTheme {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }//Variable that says: “Which screen is currently displayed?”
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = { /* small drawer */ }
            ) {
                Scaffold(
                    // topBar = { AppTopBar(...) },
                    // bottomBar = { AppBottomBar(...) }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        when (currentScreen) {
                            is Screen.Login -> LoginScreen(
                                onLoginSuccess = { currentScreen = Screen.Home }
                            )
                            is Screen.Home -> HomeScreen(
                                onOpenAdmins = { currentScreen = Screen.ListAdmins },
                                //If currentScreen = DataStudents → display the report card screen :
                                onOpenStudents = { currentScreen = Screen.DataStudents },
                                // Je rajoute ici une page pour le calendar dans le menu
                                onOpenCalendar = { currentScreen = Screen.Calendar },
                                // Je rajoute ici une apage qui va vers les settings
                                onOpenSettings = { currentScreen = Screen.Settings }
                            )
                            is Screen.Calendar -> CalendarScreen(
                                modifier = Modifier.fillMaxSize(),
                                scheduledByDate = vm.scheduledByDate
                            )
                            is Screen.Settings -> SettingsScreen(
                                repo = koinInject(),
                                onSaved = { vm.load() },
                                onLogout = { currentScreen = Screen.Login }
                            )
                            is Screen.ListAdmins -> ListAdmins(onBack = { currentScreen = Screen.Home })
                            // AJOUT : nouvel écran
                            is Screen.DataStudents -> DataStudentsScreen(onBack = { currentScreen = Screen.Home })
                        }
                    }
                }
            }

            // Le code commenté que tu avais (BottomBar, etc.) reste intact
            // var selectedScreen by remember { mutableStateOf(BottomItem.HOME) }
            // ... tout ton code commenté reste là, je ne touche à rien
        }
    }
}

private enum class BottomItem {
    HOME, CALENDAR, SETTINGS;

    @Composable
    fun getLabel() = when (this) {
        HOME -> stringResource(Res.string.home)
        CALENDAR -> stringResource(Res.string.calendar)
        SETTINGS -> stringResource(Res.string.settings)
    }

    fun getIconRes() = when (this) {
        HOME -> Icons.Filled.Home
        CALENDAR -> Icons.Filled.CalendarMonth
        SETTINGS -> Icons.Filled.Settings
    }
}
