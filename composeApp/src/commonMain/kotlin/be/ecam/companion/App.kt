package be.ecam.companion

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import be.ecam.companion.data.SettingsRepository
import be.ecam.companion.di.appModule
import be.ecam.companion.viewmodel.HomeViewModel
import be.ecam.companion.ui.LoginScreen
import be.ecam.companion.ui.CalendarScreen
import be.ecam.companion.ui.DataBibleScreen
import be.ecam.companion.ui.SettingsScreen
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
                        onNavigateToTeachers = { currentScreen = Screen.TeacherList },
                        onNavigateToCalendar = { currentScreen = Screen.Calendar },
                        onNavigateToSettings = { currentScreen = Screen.Settings },
                        onLogout = { currentScreen = Screen.Login },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings },
                        onOpenHome = { currentScreen = Screen.AdminDashboard },
                        onNavigateToBible = { currentScreen = Screen.Bible }
                    )
                }

                Screen.StudentDashboard -> {
                    StudentDashboard(
                        onNavigateToGrades = { currentScreen = Screen.MyGrades },
                        onNavigateToCourses = { currentScreen = Screen.MyCourses },
                        onNavigateToCalendar = { currentScreen = Screen.Calendar },
                        onNavigateToSettings = { currentScreen = Screen.Settings },
                        onLogout = { currentScreen = Screen.Login },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings },
                        onOpenHome = { currentScreen = Screen.AdminDashboard }
                    )
                }

                Screen.TeacherDashboard -> {
                    TeacherDashboard(
                        onNavigateToCalendar = { currentScreen = Screen.Calendar },
                        onNavigateToSettings = { currentScreen = Screen.Settings },
                        onLogout = { currentScreen = Screen.Login },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings },
                        onOpenHome = { currentScreen = Screen.AdminDashboard }
                    )
                }

                // =======================
                //        ADMIN CRUD
                // =======================
                Screen.AdminList -> {
                    ListAdmins(
                        onBack = { currentScreen = Screen.AdminDashboard },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings },
                        onOpenHome = { currentScreen = Screen.AdminDashboard }
                    )
                }

                Screen.StudentList -> {
                    ListStudents(
                        onBack = { currentScreen = Screen.AdminDashboard },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings },
                        onOpenHome = { currentScreen = Screen.AdminDashboard }
                    )
                }

                Screen.TeacherList -> {
                    ListTeachers(
                        onBack = { currentScreen = Screen.AdminDashboard },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings },
                        onOpenHome = { currentScreen = Screen.AdminDashboard }
                    )
                }

                // =======================
                //       STUDENT
                // =======================
                Screen.MyGrades -> {
                    MyGradesScreen(
                        onBack = { currentScreen = Screen.StudentDashboard },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings },
                        onOpenHome = { currentScreen = Screen.AdminDashboard }
                    )
                }

                Screen.MyCourses -> {
                    MyCoursesScreen(
                        onBack = { currentScreen = Screen.StudentDashboard },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings },
                        onOpenHome = { currentScreen = Screen.AdminDashboard }
                    )
                }

                Screen.Bible -> {
                    DataBibleScreen(
                        onBack = { currentScreen = Screen.AdminDashboard },
                        onOpenHome = { currentScreen = Screen.AdminDashboard },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings }
                    )
                }

                // =======================
                //       SHARED
                // =======================
                Screen.Calendar -> {
                    CalendarScreen(
                        modifier = Modifier,
                        scheduledByDate = homeVm.scheduledByDate,
                        onOpenHome = { currentScreen = Screen.AdminDashboard },
                        onOpenCalendar = { },
                        onOpenSettings = { currentScreen = Screen.Settings }
                    )
                }

                Screen.Settings -> {
                    SettingsScreen(
                        repo = settingsRepo,
                        onSaved = { homeVm.load() },
                        onLogout = { currentScreen = Screen.Login },
                        onOpenHome = { currentScreen = Screen.AdminDashboard },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { }
                    )
                }
            }
        }
    }
}

// =======================
//        SCREENS
// =======================
sealed class Screen {
    object Login : Screen()

    object AdminDashboard : Screen()
    object StudentDashboard : Screen()
    object TeacherDashboard : Screen()

    object AdminList : Screen()
    object StudentList : Screen()
    object TeacherList : Screen()

    object MyGrades : Screen()
    object MyCourses : Screen()

    object Calendar : Screen()
    object Settings : Screen()
    object Bible : Screen()



}
