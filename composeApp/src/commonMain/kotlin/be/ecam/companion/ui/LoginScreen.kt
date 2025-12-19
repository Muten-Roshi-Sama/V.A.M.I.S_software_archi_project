package be.ecam.companion.ui

import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import be.ecam.companion.data.ApiRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit
) {
    val repository = koinInject<ApiRepository>()
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("admin1@school.com") }
    var password by remember { mutableStateOf("admin123") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LoginBackground(modifier = Modifier.fillMaxSize())

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val showSideImage = maxWidth >= 720.dp

            val formContent: @Composable ColumnScope.() -> Unit = {
                LoginHeaderVisual(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp)
                )

                Spacer(Modifier.height(24.dp))

                Text("Connexion", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
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
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            error = null
                            try {
                                repository.login(email.trim(), password)
                                val userInfo = repository.getMe()
                                onLoginSuccess(userInfo.role)
                            } catch (e: Exception) {
                                error = e.message ?: "Login failed"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Se connecter")
                    }
                }
            }

            if (showSideImage) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    LoginFormPanel(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .padding(24.dp),
                        content = formContent
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            } else {
                LoginFormPanel(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    content = formContent
                )
            }
        }
    }
}

@Composable
private fun LoginFormPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
        }
    }
}

@Composable
private fun LoginBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        RemoteImage(
            url = AppImages.ECAM_LOGIN_BACKGROUND_URL,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f))
        )
    }
}

@Composable
private fun LoginHeaderVisual(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        EcamLogo(
            modifier = Modifier.size(140.dp),
            contentDescription = "ECAM logo",
            contentScale = ContentScale.Fit,
        )
    }
}
