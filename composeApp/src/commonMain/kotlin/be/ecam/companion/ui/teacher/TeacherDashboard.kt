package be.ecam.companion.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.ecam.companion.data.ApiRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboard(
    onLogout: () -> Unit,
    onNavigateToCalendar: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null
) {
    val repository = koinInject<ApiRepository>()
    val scope = rememberCoroutineScope()
    var userInfo by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val teacher = repository.fetchMyTeacherProfile()
                userInfo = "Welcome, ${teacher.firstName} ${teacher.lastName} (ID: ${teacher.teacherId})"
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
                title = { Text("Teacher Dashboard") },
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
                    text = "Teacher Portal",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.Class,
                            contentDescription = "Classes",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text("My Classes", style = MaterialTheme.typography.titleLarge)
                            Text("View assigned courses", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

<<<<<<< HEAD
                // Grade Students Card
=======
>>>>>>> origin/backend/salwa
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.Grade,
                            contentDescription = "Grading",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Column {
                            Text("Grade Students", style = MaterialTheme.typography.titleLarge)
                            Text("Manage student evaluations", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Calendar Card (placeholder for future implementation)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /* TODO: Navigate to calendar */ }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.CalendarToday,
                            contentDescription = "Calendar",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Column {
                            Text("My Calendar", style = MaterialTheme.typography.titleLarge)
                            Text("View course schedule and events", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}