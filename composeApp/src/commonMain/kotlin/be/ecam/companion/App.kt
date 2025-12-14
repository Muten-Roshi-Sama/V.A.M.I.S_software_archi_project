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

import be.ecam.companion.ui.LoginScreen
import be.ecam.companion.ui.admin.ListAdmins
import be.ecam.companion.ui.admin.ListStudents
import be.ecam.companion.ui.CalendarScreen
import be.ecam.companion.ui.SettingsScreen
import be.ecam.companion.ui.admin.AdminDashboard
import be.ecam.companion.ui.student.StudentDashboard
import be.ecam.companion.ui.teacher.TeacherDashboard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(extraModules: List<Module> = emptyList()) {
    KoinApplication(application = { modules(appModule + extraModules) }) {
        MaterialTheme {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

            when (val screen = currentScreen) {
                is Screen.Login -> {
                    LoginScreen(
                        onLoginSuccess = { role ->
                            currentScreen = when (role.lowercase()) {
                                "admin" -> Screen.AdminDashboard
                                "student" -> Screen.StudentDashboard
                                "teacher" -> Screen.TeacherDashboard
                                else -> Screen.AdminDashboard
                            }
                        }
                    )
                }
                
                is Screen.AdminDashboard -> {
                    AdminDashboard(
                        onNavigateToAdmins = { currentScreen = Screen.AdminList },
                        onNavigateToStudents = { currentScreen = Screen.StudentList },
                        onNavigateToCalendar = { currentScreen = Screen.Calendar },
                        onNavigateToSettings = { currentScreen = Screen.Settings },
                        onLogout = { currentScreen = Screen.Login }
                    )
                }
                
                is Screen.StudentDashboard -> {
                    StudentDashboard(
                        onLogout = { currentScreen = Screen.Login },
                        onNavigateToGrades = { currentScreen = Screen.MyGrades },
                        onNavigateToCourses = { currentScreen = Screen.MyCourses },
                        onNavigateToCalendar = { currentScreen = Screen.Calendar },
                        onNavigateToSettings = { currentScreen = Screen.Settings }
                    )
                }
                
                is Screen.TeacherDashboard -> {
                    TeacherDashboard(
                        onLogout = { currentScreen = Screen.Login },
                        onNavigateToCalendar = { currentScreen = Screen.Calendar },
                        onNavigateToSettings = { currentScreen = Screen.Settings }
                    )
                }
                
                is Screen.AdminList -> {
                    ListAdmins(
                        onBack = { currentScreen = Screen.AdminDashboard }
                    )
                }
                
                is Screen.StudentList -> {
                    ListStudents(
                        onBack = { currentScreen = Screen.AdminDashboard }
                    )
                }
                
                is Screen.MyGrades -> {
                    be.ecam.companion.ui.student.MyGradesScreen(
                        onBack = { currentScreen = Screen.StudentDashboard }
                    )
                }
                
                is Screen.MyCourses -> {
                    be.ecam.companion.ui.student.MyCoursesScreen(
                        onBack = { currentScreen = Screen.StudentDashboard }
                    )
                }
                
                is Screen.Calendar -> {
                    CalendarScreen()
                }
                
                is Screen.Settings -> {
                    val settingsRepo = koinInject<SettingsRepository>()
                    SettingsScreen(
                        repo = settingsRepo,
                        onSaved = { currentScreen = when {
                            currentScreen == Screen.Settings -> Screen.AdminDashboard
                            else -> Screen.AdminDashboard
                        }}
                    )
                }
            }
        }
    }
}

private sealed class Screen {
    object Login : Screen()
    object AdminDashboard : Screen()
    object StudentDashboard : Screen()
    object TeacherDashboard : Screen()
    object AdminList : Screen()
    object StudentList : Screen()
    object MyGrades : Screen()
    object MyCourses : Screen()
    object Calendar : Screen()
    object Settings : Screen()
}