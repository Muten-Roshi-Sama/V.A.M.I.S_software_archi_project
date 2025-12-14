package be.ecam.companion.ui

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import be.ecam.companion.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenAdmins: () -> Unit,
    onOpenStudents: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTeachers: () -> Unit,
    onOpenBible: () -> Unit
) {
    val vm = koinInject<HomeViewModel>()
    LaunchedEffect(Unit) { vm.load() }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Utilisation du drawer réutilisable
    AppDrawer(
        drawerState = drawerState,
        scope = scope,
        onOpenCalendar = onOpenCalendar,
        onOpenSettings = onOpenSettings
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Open menu")
            }

            Spacer(Modifier.height(16.dp))

            Text("Home", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            if (vm.lastErrorMessage.isNotEmpty()) {
                Text(vm.lastErrorMessage, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(onClick = onOpenAdmins) {
                Text("Open Admins List")
            }

            Spacer(Modifier.height(12.dp))

            Button(onClick = onOpenStudents) {
                Text("See all student reports")
            }

            Spacer(Modifier.height(12.dp))

            Button(onClick = onOpenTeachers) {
                Text("Voir les enseignants")
            }

            Spacer(Modifier.height(12.dp))

            Button(onClick = onOpenBible) {
                Text("\ud83d\udcd2 Bible des Programmes")
            }

            Spacer(Modifier.height(20.dp))

            Text(vm.helloMessage)
        }
    }
}
