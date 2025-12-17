package be.ecam.companion

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import be.ecam.companion.di.appModule
import be.ecam.companion.viewmodel.HomeViewModel
import be.ecam.companion.data.SettingsRepository
import be.ecam.companion.ui.*
import be.ecam.companion.ui.admin.*
import be.ecam.companion.ui.student.*
import be.ecam.companion.ui.teacher.*
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.Module



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(extraModules: List<Module> = emptyList()) {

    KoinApplication(application = { modules(appModule + extraModules) }) {

        val homeVm = koinInject<HomeViewModel>()
        val settingsRepo = koinInject<SettingsRepository>()

        MaterialTheme {

            var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

            when (currentScreen) {

                // =======================
                //        LOGIN
                // =======================
                Screen.Login -> {
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

                // =======================
                //      DASHBOARDS
                // =======================
                Screen.AdminDashboard -> {
                    AdminDashboard(
                        onNavigateToAdmins = { currentScreen = Screen.AdminList },
                        onNavigateToStudents = { currentScreen = Screen.StudentList },
                        onNavigateToCalendar = { currentScreen = Screen.Calendar },
                        onNavigateToSettings = { currentScreen = Screen.Settings },
                        onLogout = { currentScreen = Screen.Login },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings },
                        onOpenHome = { currentScreen = Screen.Settings },


                    )
                }

                Screen.StudentDashboard -> {
                    StudentDashboard(
                        onNavigateToGrades = { currentScreen = Screen.MyGrades },
                        onNavigateToCourses = { currentScreen = Screen.MyCourses },
                        onNavigateToCalendar = { currentScreen = Screen.Calendar },
                        onNavigateToSettings = { currentScreen = Screen.Settings },
                        onLogout = { currentScreen = Screen.Login }
                    )
                }

                Screen.TeacherDashboard -> {
                    TeacherDashboard(
                        onNavigateToCalendar = { currentScreen = Screen.Calendar },
                        onNavigateToSettings = { currentScreen = Screen.Settings },
                        onLogout = { currentScreen = Screen.Login }
                    )
                }

                // =======================
                //        ADMIN
                // =======================
                Screen.AdminList -> {
                    ListAdmins(
                        onBack = { currentScreen = Screen.AdminDashboard },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings },
                        onOpenHome = { currentScreen = Screen.Settings }
                    )
                }

                Screen.StudentList -> {
                    ListStudents(
                        onBack = { currentScreen = Screen.AdminDashboard },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings },
                        onOpenHome = { currentScreen = Screen.Settings }
                    )
                }

                // =======================
                //       STUDENT
                // =======================
                Screen.MyGrades -> {
                    MyGradesScreen(
                        onBack = { currentScreen = Screen.StudentDashboard }
                    )
                }

                Screen.MyCourses -> {
                    MyCoursesScreen(
                        onBack = { currentScreen = Screen.StudentDashboard }
                    )
                }

                // =======================
                //       SHARED
                // =======================
                Screen.Calendar -> {
                    CalendarScreen(
                        modifier = Modifier,
                        scheduledByDate = homeVm.scheduledByDate,
                        onOpenHome = {
                            currentScreen = when {
                                currentScreen == Screen.Calendar -> Screen.AdminDashboard
                                else -> Screen.AdminDashboard
                            }
                        },
                        onOpenCalendar = { },
                        onOpenSettings = { currentScreen = Screen.Settings }
                    )
                }

                Screen.Settings -> {
                    SettingsScreen(
                        repo = settingsRepo,
                        onSaved = { homeVm.load() },
                        onLogout = { currentScreen = Screen.Login },
                        onOpenHome = {
                            currentScreen = when {
                                currentScreen == Screen.Settings -> Screen.AdminDashboard
                                else -> Screen.AdminDashboard
                            }
                        },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { }
                    )
                }
            }
        }
    }
}

sealed class Screen {
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
