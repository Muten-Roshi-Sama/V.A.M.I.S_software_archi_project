package be.ecam.companion

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import be.ecam.companion.data.SettingsRepository
import be.ecam.companion.di.appModule
import be.ecam.companion.viewmodel.HomeViewModel
import be.ecam.companion.ui.LoginScreen
import be.ecam.companion.ui.CalendarScreen
//import be.ecam.companion.ui.CourseGradeUi
import be.ecam.companion.ui.DataBibleScreen
import be.ecam.companion.ui.admin.GradesScreen
import be.ecam.companion.ui.IspListScreen
import be.ecam.companion.ui.SettingsScreen
import be.ecam.companion.ui.admin.*
import be.ecam.companion.ui.student.*
import be.ecam.companion.ui.teacher.*
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.Module
import be.ecam.companion.ui.IspCourseUi
import be.ecam.companion.ui.IspEditScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(extraModules: List<Module> = emptyList()) {

    KoinApplication(application = { modules(appModule + extraModules) }) {

        val homeVm = koinInject<HomeViewModel>()
        val settingsRepo = koinInject<SettingsRepository>()

        MaterialTheme {

            val ispCourses = remember {
                mutableStateListOf(
                    IspCourseUi("Web Development", "WD4P", "COM", 4, "DBS", 5, 27),
                    IspCourseUi("Advanced Database", "AD3T", "COM", 3, "SRZ", 5, 14),
                    IspCourseUi("Accounting", "AC4T", "COM", 4, "VMN", 5, 20),
                )
            }

            // ✅ autorisation d’édition ISP
            var allowIspEdit by remember { mutableStateOf(false) }

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
                        onNavigateToBible = { currentScreen = Screen.Bible },
                        onNavigateToGrades = { currentScreen = Screen.Grades }
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
                        onOpenHome = { currentScreen = Screen.StudentDashboard },
                        onNavigateToISP = { currentScreen = Screen.ISP }
                    )
                }

                Screen.TeacherDashboard -> {
                    TeacherDashboard(
                        onNavigateToCalendar = { currentScreen = Screen.Calendar },
                        onNavigateToSettings = { currentScreen = Screen.Settings },
                        onLogout = { currentScreen = Screen.Login },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings },
                        onOpenHome = { currentScreen = Screen.TeacherDashboard}
                    )
                }

                Screen.Grades -> {
                    GradesScreen(
                        grades = listOf(
                            CourseGradeUi("Web Development", "WD4P", 12, 5),
                            CourseGradeUi("Advanced Database", "AD3T", 15, 5),
                            CourseGradeUi("Accounting", "AC4T", null, 5), // ✅ ne s'affiche pas
                            CourseGradeUi("Algorithm Complexity", "AL4T", 12, 5),
                            CourseGradeUi("Artificial Intelligence", "AI4P", 10, 5),
                            CourseGradeUi("Project Management", "PM4T", 10, 5),
                        ),
                        onOpenHome = { currentScreen = Screen.AdminDashboard },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings }


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

                Screen.Bible -> {
                    DataBibleScreen(
                        onBack = { currentScreen = Screen.AdminDashboard },
                        onOpenHome = { currentScreen = Screen.AdminDashboard },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings }
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
                        onOpenHome = { currentScreen = Screen.StudentDashboard }
                    )
                }

                Screen.MyCourses -> {
                    MyCoursesScreen(
                        onBack = { currentScreen = Screen.StudentDashboard },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings },
                        onOpenHome = { currentScreen = Screen.StudentDashboard }
                    )
                }

                Screen.ISP -> {
                    IspListScreen(
                        courses = ispCourses,
                        onAddCourse = {
                            allowIspEdit = true
                            currentScreen = Screen.IspEdit
                        },
                        onBack = { currentScreen = Screen.StudentDashboard },
                        onOpenHome = { currentScreen = Screen.StudentDashboard },
                        onOpenCalendar = { currentScreen = Screen.Calendar },
                        onOpenSettings = { currentScreen = Screen.Settings },
                    )
                }


                Screen.IspEdit -> {
                    if (!allowIspEdit) {
                        currentScreen = Screen.ISP
                    } else {
                        IspEditScreen(
                            onConfirm = { newCourse ->
                                ispCourses.add(newCourse)
                                allowIspEdit = false
                                currentScreen = Screen.ISP
                            },
                            onCancel = {
                                allowIspEdit = false
                                currentScreen = Screen.ISP
                            }
                        )
                    }
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
    object ISP : Screen()
    object Grades : Screen()

    object IspEdit : Screen()


}
