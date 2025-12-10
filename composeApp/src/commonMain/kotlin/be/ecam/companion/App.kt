package be.ecam.companion

import androidx.compose.material3.*
import androidx.compose.runtime.*
import be.ecam.companion.di.appModule
import be.ecam.companion.data.ApiRepository
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.Module

import be.ecam.companion.ui.LoginScreen
import be.ecam.companion.ui.admin.AdminDashboard
import be.ecam.companion.ui.admin.ListAdmins
import be.ecam.companion.ui.student.StudentDashboard
import be.ecam.companion.ui.student.ListStudents
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
                            // Route to correct dashboard based on role
                            currentScreen = when (role.lowercase()) {
                                "admin" -> Screen.AdminDashboard
                                "student" -> Screen.StudentDashboard
                                "teacher" -> Screen.TeacherDashboard
                                else -> Screen.AdminDashboard // fallback
                            }
                        }
                    )
                }
                
                is Screen.AdminDashboard -> {
                    AdminDashboard(
                        onNavigateToAdmins = { currentScreen = Screen.AdminList },
                        onNavigateToStudents = { currentScreen = Screen.StudentList },
                        onLogout = { currentScreen = Screen.Login }
                    )
                }
                
                is Screen.StudentDashboard -> {
                    StudentDashboard(
                        onLogout = { currentScreen = Screen.Login }
                    )
                }
                
                is Screen.TeacherDashboard -> {
                    TeacherDashboard(
                        onLogout = { currentScreen = Screen.Login }
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
}