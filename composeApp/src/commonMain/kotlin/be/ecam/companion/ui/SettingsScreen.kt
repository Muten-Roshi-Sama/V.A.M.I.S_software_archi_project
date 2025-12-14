package be.ecam.companion.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import be.ecam.companion.data.SettingsRepository
import be.ecam.companion.di.buildBaseUrl
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.rememberDrawerState

import androidx.compose.material3.rememberDrawerState

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
    ){
        IconButton(
            onClick = { scope.launch { drawerState.open() } },
        ) {
            Icon(Icons.Filled.Menu, contentDescription = "Menu")
        }

        Column {
            Text("Server configuration")
            Spacer(Modifier.height(8.dp))
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .width(240.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                    .shadow(8.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 24.dp, horizontal = 16.dp)
            ) {
                Text("Menu", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(24.dp))

                DrawerItem("Home", Icons.Filled.Home) {
                    scope.launch { drawerState.close() }
                    onOpenHome()
                }

                DrawerItem("Calendar", Icons.Filled.CalendarMonth) {
                    scope.launch { drawerState.close() }
                    onOpenCalendar()
                }

                DrawerItem("Settings", Icons.Filled.Settings) {
                    scope.launch { drawerState.close() }
                    onOpenSettings()
                }
            }
        },
        scrimColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.32f)
    ) {
    Column {
        IconButton(
            onClick = { scope.launch { drawerState.open() } }
        ) {
            Icon(Icons.Filled.Menu, contentDescription = "Open menu")
        }

        Text("Server configuration")
        Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = host,
                onValueChange = { host = it },
                label = { Text("Server host (e.g. 192.168.1.10 or http://example.com)") },
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = portText,
                onValueChange = { portText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Port") },
                singleLine = true
            )

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!)
            }

            Spacer(Modifier.height(12.dp))
            Button(
                enabled = !saving,
                onClick = {
                    val port = portText.toIntOrNull()
                    if (host.isBlank() || port == null || port !in 1..65535) {
                        error = "Please enter a valid host and port (1-65535)."
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

            val preview = run {
                val p = portText.toIntOrNull() ?: 0
                if (host.isNotBlank() && p in 1..65535) buildBaseUrl(host, p) else ""
            }

            if (preview.isNotBlank()) {
                Text("Base URL: $preview")
            }

            if (saved) {
                Spacer(Modifier.height(4.dp))
                Text("Saved. Reloading…")
            }

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onLogout() }
            ) {
                Text("Log Out")
            }
        }
    }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onLogout() }
        ) {
            Text("Log Out")
        }
    }
    }
}


