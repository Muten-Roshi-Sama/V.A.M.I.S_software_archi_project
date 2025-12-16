package be.ecam.companion.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.ecam.companion.data.ApiRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

import androidx.compose.material.icons.filled.Person

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onNavigateToAdmins: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToTeachers: () -> Unit,
    onNavigateToCalendar: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    onLogout: () -> Unit
) {
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


                // Teacher Management Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToTeachers
                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.Person,  // or another appropriate icon
                            contentDescription = "Teachers",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Column {
                            Text("Teachers", style = MaterialTheme.typography.titleLarge)
                            Text("Manage teacher accounts", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Other Cards...






            }
        }
    }
}