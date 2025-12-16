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
fun LoginScreen(onLoginSuccess: (String) -> Unit) {  // <-- Changed signature
    val repository = koinInject<ApiRepository>()
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("admin1@school.com") }
    var password by remember { mutableStateOf("admin123") }
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
                        val loginResponse = repository.login(email.trim(), password)
                        println("✅ LOGIN SUCCESS: token=${loginResponse.accessToken}")
                        
                        // Fetch user role
                        val userInfo = repository.getMe()
                        println("✅ USER ROLE: ${userInfo.role}")
                        
                        onLoginSuccess(userInfo.role)  // <-- Pass role to parent
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