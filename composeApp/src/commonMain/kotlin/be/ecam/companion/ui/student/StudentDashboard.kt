package be.ecam.companion.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Assignment
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
fun StudentDashboard(onLogout: () -> Unit) {
    val repository = koinInject<ApiRepository>()
    val scope = rememberCoroutineScope()
    var userInfo by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val info = repository.getMe()
                userInfo = "Welcome, Student (ID: ${info.id})"
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
                    text = "Student Portal",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(Modifier.height(16.dp))

                // My Courses Card (placeholder)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /* TODO: Navigate to courses */ }
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
                            contentDescription = "Courses",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text("My Courses", style = MaterialTheme.typography.titleLarge)
                            Text("View enrolled courses", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // My Grades Card (placeholder)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /* TODO: Navigate to grades */ }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.Assignment,
                            contentDescription = "Grades",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Column {
                            Text("My Grades", style = MaterialTheme.typography.titleLarge)
                            Text("View academic performance", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}