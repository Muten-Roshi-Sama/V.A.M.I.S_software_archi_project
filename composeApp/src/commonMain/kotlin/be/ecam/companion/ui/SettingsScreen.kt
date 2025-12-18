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
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Bookmarks
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.rememberDrawerState

import androidx.compose.material3.rememberDrawerState


import androidx.compose.foundation.layout.*

import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.unit.dp

import be.ecam.companion.di.buildBaseUrl
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repo: SettingsRepository,
    onSaved: (() -> Unit)? = null,
    onLogout: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHome: () -> Unit = {}
) {
    // 🔹 UN SEUL drawerState (celui de AppDrawer)
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
                .padding(16.dp)
        ) {

            // ☰ Bouton menu (UNIQUE)
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

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = host,
                onValueChange = { host = it },
                label = { Text("Server host (e.g. 192.168.1.10)") },
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

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

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onLogout
            ) {
                Text("Log Out")
            }
    Column {
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
        Button(enabled = !saving, onClick = {
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
                    // show saved feedback briefly
                    kotlinx.coroutines.delay(1200)
                    saved = false
                    saving = false
                }
            }
        }) {
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
            Text("Saved. ReloadingÔÇª")
        }
    }
}}}
