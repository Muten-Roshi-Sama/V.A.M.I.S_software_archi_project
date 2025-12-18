package be.ecam.companion.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.ecam.companion.data.ApiRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import be.ecam.companion.ui.AppDrawer
import be.ecam.companion.ui.Components.DashboardTile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(
    onLogout: () -> Unit,
    onNavigateToGrades: () -> Unit = {},
    onNavigateToCourses: () -> Unit = {},
    onNavigateToCalendar: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHome: () -> Unit,
    onNavigateToISP: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val repository = koinInject<ApiRepository>()
    val scope = rememberCoroutineScope()
    var userInfo by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val student = repository.fetchMyStudentProfile()
                userInfo = "Welcome, ${student.firstName} ${student.lastName} (ID: ${student.studentId})"
            } catch (e: Exception) {
                userInfo = "Error loading user info"
            } finally {
                isLoading = false
            }
        }
    }

    AppDrawer(
        drawerState = drawerState,
        scope = scope,
        onOpenCalendar = onOpenCalendar,
        onOpenSettings = onOpenSettings,
        onOpenHome = onOpenHome
    ){
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                    }
                },
                title = { Text("Student Dashboard") },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            repository.logout()
                            onLogout()
                        }
                    }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = userInfo ?: "Welcome",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Student Portal",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                    //onClick = onNavigateToCourses
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DashboardTile(
                            title = "My Courses",
                            subtitle = "View enrolled courses",
                            icon = Icons.Filled.AdminPanelSettings,
                            onClick = onNavigateToCourses,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardTile(
                            title = "My Grades",
                            subtitle = "View academic performance",
                            icon = Icons.Filled.AdminPanelSettings,
                            onClick = onNavigateToGrades,
                            modifier = Modifier.weight(1f)
                        )

                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DashboardTile(
                            title = "My Grades",
                            //subtitle = "View enrolled courses",
                            icon = Icons.Filled.AdminPanelSettings,
                            onClick = onNavigateToGrades,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardTile(
                            title = "ISP",
                            //subtitle = "View academic performance",
                            icon = Icons.Filled.AdminPanelSettings,
                            onClick = onNavigateToISP,
                            modifier = Modifier.weight(1f)
                        )

                    }
                }
            }
        }
    }}
}