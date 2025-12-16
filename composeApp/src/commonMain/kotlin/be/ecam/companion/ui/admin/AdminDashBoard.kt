package be.ecam.companion.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.ecam.companion.data.ApiRepository
import be.ecam.companion.ui.AppDrawer
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onNavigateToAdmins: () -> Unit,
    onNavigateToStudents: () -> Unit,
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
                title = { Text("Admin Dashboard") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Open menu")
            }

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

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToAdmins
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.AdminPanelSettings,
                            contentDescription = "Admins",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text("Admin Management", style = MaterialTheme.typography.titleLarge)
                            Text("View and manage administrators", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToStudents
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.School,
                            contentDescription = "Students",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Column {
                            Text("Student Management", style = MaterialTheme.typography.titleLarge)
                            Text("View and manage students", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
}