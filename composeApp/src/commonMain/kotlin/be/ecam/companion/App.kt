package be.ecam.companion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Check
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
import be.ecam.companion.ui.DataStudentsScreen
import be.ecam.companion.ui.DataTeacherScreen
import be.ecam.companion.ui.DataBibleScreen
import be.ecam.companion.ui.GradesScreen
import be.ecam.companion.ui.CourseGradeUi
import be.ecam.companion.ui.IspListScreen
import be.ecam.companion.ui.IspEditScreen
import be.ecam.companion.ui.IspCourseUi





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(extraModules: List<Module> = emptyList()) {
    KoinApplication(application = { modules(appModule + extraModules) }) {
        val vm = koinInject<HomeViewModel>()
        MaterialTheme {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }//Variable that says: “Which screen is currently displayed?”
            val ispCourses = remember {
                mutableStateListOf<IspCourseUi>(
                    IspCourseUi("Web Development", "WD4P", "COM", 4, "DBS", 5, 27),
                    IspCourseUi("Advanced Database", "AD3T", "COM", 3, "SRZ", 5, 14),
                    IspCourseUi("Accounting", "AC4T", "COM", 4, "VMN", 5, 20),
                    IspCourseUi("Algorithm Complexity", "AL4T", "COM", 4, "RSS", 5, 24),
                    IspCourseUi("Artificial Intelligence", "AI4P", "COM", 4, "RCH", 5, 22),
                    IspCourseUi("Project Management", "PM4T", "COM", 4, "ROM", 5, 22),
                )
            }

var allowIspEdit by remember { mutableStateOf(false) }

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
                                onOpenHome = { currentScreen = Screen.Home },
                                onOpenCalendar = { currentScreen = Screen.Calendar },
                                onOpenTeachers = { currentScreen = Screen.Teachers },
                                onOpenBible = { currentScreen = Screen.Bible },
                                onOpenGrades = { currentScreen = Screen.Grades },
                                onOpenIspList = { currentScreen = Screen.IspList },
                                onOpenSettings = { currentScreen = Screen.Settings },
                            )
                            is Screen.Calendar -> CalendarScreen(
                                modifier = Modifier.fillMaxSize(),
                                scheduledByDate = vm.scheduledByDate,
                                onOpenHome = { currentScreen = Screen.Home },
                                onOpenCalendar = { currentScreen = Screen.Calendar },
                                onOpenGrades = { currentScreen = Screen.Grades },
                                onOpenIspList = { currentScreen = Screen.IspList },
                                onOpenSettings = { currentScreen = Screen.Settings },

                            )
                            is Screen.Settings -> SettingsScreen(
                                repo = koinInject(),
                                onSaved = { vm.load() },
                                onLogout = { currentScreen = Screen.Login },
                                onOpenHome = { currentScreen = Screen.Home },
                                onOpenCalendar = { currentScreen = Screen.Calendar },
                                onOpenGrades = { currentScreen = Screen.Grades },
                                onOpenIspList = { currentScreen = Screen.IspList },
                                onOpenSettings = { currentScreen = Screen.Settings },

                            )

                            is Screen.Grades -> GradesScreen(
                                grades = listOf(
                                    CourseGradeUi("Web Development", "WD4P", 12, 5),
                                    CourseGradeUi("Advanced Database", "AD3T", 15, 5),
                                    CourseGradeUi("Accounting", "AC4T", null, 5), // ✅ ne s'affiche pas
                                    CourseGradeUi("Algorithm Complexity", "AL4T", 12, 5),
                                    CourseGradeUi("Artificial Intelligence", "AI4P", 10, 5),
                                    CourseGradeUi("Project Management", "PM4T", 10, 5),
                                ),
                                onOpenHome = { currentScreen = Screen.Home },
                                onOpenCalendar = { currentScreen = Screen.Calendar },
                                onOpenGrades = { currentScreen = Screen.Grades },
                                onOpenIspList = { currentScreen = Screen.IspList },
                                onOpenSettings = { currentScreen = Screen.Settings },
                            )

                            is Screen.IspList -> IspListScreen(
                                courses = ispCourses,
                                onAddCourse = {
                                    allowIspEdit = true
                                    currentScreen = Screen.IspEdit
                                },
                                onBack = { currentScreen = Screen.Home },
                                onOpenHome = { currentScreen = Screen.Home },
                                onOpenCalendar = { currentScreen = Screen.Calendar },
                                onOpenIspList = { currentScreen = Screen.IspList },
                                onOpenGrades = { currentScreen = Screen.Grades },
                                onOpenSettings = { currentScreen = Screen.Settings },

                            )


                            is Screen.IspEdit -> {
                                if (!allowIspEdit) {
                                    currentScreen = Screen.IspList
                                } else {
                                    IspEditScreen(
                                        onConfirm = { newCourse ->
                                            ispCourses.add(newCourse)
                                            allowIspEdit = false
                                            currentScreen = Screen.IspList
                                        },
                                        onCancel = {
                                            allowIspEdit = false
                                            currentScreen = Screen.IspList
                                        }
                                    )
                                }
                            }

                            is Screen.ListAdmins -> ListAdmins(onBack = { currentScreen = Screen.Home })
                            is Screen.DataStudents -> DataStudentsScreen(onBack = { currentScreen = Screen.Home })
                            is Screen.Teachers -> DataTeacherScreen(onBack = { currentScreen = Screen.Home })
                            is Screen.Bible -> DataBibleScreen(onBack = { currentScreen = Screen.Home })
                        }
                    }
                }
            }
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
