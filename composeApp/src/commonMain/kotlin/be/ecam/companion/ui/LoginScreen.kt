package be.ecam.companion.ui

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import be.ecam.companion.data.ApiRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val repository = koinInject<ApiRepository>()
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("admin1@admin.com") }  // Pre-filled for testing
    var password by remember { mutableStateOf("pass123") }  // Pre-filled for testing
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Connexion", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("e-mail") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    error = null
                    try {
                        val response = repository.login(email.trim(), password)
                        println("✅ LOGIN SUCCESS: token=${response.accessToken}")
                        onLoginSuccess()
                    } catch (e: Exception) {
                        error = e.message ?: "Login failed"
                        println("❌ LOGIN ERROR: ${e.stackTraceToString()}")
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Se connecter")
            }
        }
    }
}