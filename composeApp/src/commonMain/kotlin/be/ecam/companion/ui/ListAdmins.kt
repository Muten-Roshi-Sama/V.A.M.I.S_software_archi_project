package be.ecam.companion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.ecam.common.api.AdminDTO

// Import Api Routes
import be.ecam.companion.data.ApiRepository
import org.koin.compose.koinInject

@Composable
fun ListAdmins(onBack: () -> Unit) {
    // Get repository from Koin
    val repository = koinInject<ApiRepository>()

    var admins by remember { mutableStateOf<List<AdminDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        try {
            admins = repository.fetchAdmins()
            println("✅ ADMINS FETCHED: $admins")
            admins.forEach { a ->
                println("   ID=${a.id} | name=${a.firstName} ${a.lastName} | email=${a.email}")
            }
        } catch (e: Exception) {
            error = e.message ?: "Unknown error"
            println("❌ ERROR fetching admins: ${e.stackTraceToString()}")
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            TextButton(onClick = onBack) { Text("← Back") }
        }
        Text("Admins", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        when {
            isLoading -> CircularProgressIndicator()
            error != null -> {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }
            admins.isEmpty() -> Text("No admins found")
            else -> LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(admins) { admin ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier
                            .padding(12.dp)
                        ) {
                            Text("ID: ${admin.id}", style = MaterialTheme.typography.labelSmall)
                            Text("${admin.firstName} ${admin.lastName}", style = MaterialTheme.typography.titleMedium)
                            Text(admin.email, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}