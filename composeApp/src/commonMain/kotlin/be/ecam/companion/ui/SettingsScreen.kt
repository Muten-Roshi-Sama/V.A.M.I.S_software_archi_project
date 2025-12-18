package be.ecam.companion.ui
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.ecam.companion.data.SettingsRepository
import be.ecam.companion.di.buildBaseUrl
import androidx.compose.material.icons.filled.Menu
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material3.rememberDrawerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun SettingsScreen(
    repo: SettingsRepository,
    onSaved: (() -> Unit)? = null,
    onLogout: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHome: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var host by remember { mutableStateOf("") }
    var portText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        host = repo.getServerHost()
        portText = repo.getServerPort().toString()
    }

    AppDrawer(
        drawerState = drawerState,
        scope = scope,
        onOpenCalendar = onOpenCalendar,
        onOpenSettings = onOpenSettings,
        onOpenHome = onOpenHome
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ☰ Bouton menu
            IconButton(
                onClick = { scope.launch { drawerState.open() } }
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Server configuration",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(16.dp))

            // Host
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = host,
                onValueChange = { host = it },
                label = { Text("Server host (e.g. 192.168.1.10)") },
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            // Port
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = portText,
                onValueChange = { portText = it.filter(Char::isDigit) },
                label = { Text("Port") },
                singleLine = true
            )

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))

            // Save Button
            Button(
                enabled = !saving,
                onClick = {
                    val port = portText.toIntOrNull()
                    if (host.isBlank() || port == null || port !in 1..65535) {
                        error = "Please enter a valid host and port (1–65535)."
                        return@Button
                    }

                    error = null
                    scope.launch {
                        saving = true
                        try {
                            repo.setServerHost(host.trim())
                            repo.setServerPort(port)
                            saved = true
                            onSaved?.invoke()
                        } finally {
                            kotlinx.coroutines.delay(1200)
                            saved = false
                            saving = false
                        }
                    }
                }
            ) {
                Text("Save")
            }

            Spacer(Modifier.height(8.dp))

            // Preview URL
            val preview = run {
                val p = portText.toIntOrNull() ?: 0
                if (host.isNotBlank() && p in 1..65535)
                    buildBaseUrl(host, p)
                else ""
            }
            if (preview.isNotBlank()) {
                Text("Base URL: $preview")
            }

            if (saved) {
                Spacer(Modifier.height(4.dp))
                Text("Saved.")
            }

            Spacer(Modifier.height(32.dp))

            // Logout
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onLogout
            ) {
                Text("Log Out")
            }
        }
    }
}
