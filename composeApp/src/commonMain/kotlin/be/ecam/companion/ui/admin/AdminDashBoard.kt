package be.ecam.companion.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.ecam.companion.data.ApiRepository
import be.ecam.companion.ui.AppDrawer
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import androidx.compose.ui.text.style.TextAlign


import androidx.compose.material.icons.filled.Person
import be.ecam.companion.ui.Components.DashboardTile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onNavigateToAdmins: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToTeachers: () -> Unit,
    onNavigateToBible: () -> Unit,
    //onNavigateToTeachers: () -> Unit,
    onNavigateToCalendar: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    onLogout: () -> Unit,
    //onOpenTeachers: () -> Unit,
    // Rubriques du menu déroulant
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHome: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val repository = koinInject<ApiRepository>()
    val scope = rememberCoroutineScope()
    var userInfo by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val admin = repository.fetchMyAdminProfile()
                userInfo = "Welcome, ${admin.firstName} ${admin.lastName} (ID: ${admin.id})"
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
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                        }
                    },
                    title = {
                        Text(
                            "Admin Dashboard",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
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
        ){ padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),

                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            )
            {


            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = userInfo ?: "Welcome",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Manage Tables",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardTile(
                            title = "Admins",
                            subtitle = "Manage admins",
                            icon = Icons.Filled.AdminPanelSettings,
                            onClick = onNavigateToAdmins,
                            modifier = Modifier.weight(1f)
                        )

                        DashboardTile(
                            title = "Students",
                            subtitle = "Manage students",
                            icon = Icons.Filled.School,
                            onClick = onNavigateToStudents,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardTile(
                            title = "Teachers",
                            subtitle = "Manage teachers",
                            icon = Icons.Filled.Person,
                            onClick = onNavigateToTeachers,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardTile(
                            title = "Bible",
                            subtitle = "Manage teachers",
                            icon = Icons.Filled.MenuBook,
                            onClick = onNavigateToBible,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Other Cards...






            }
        }
    }
}
}