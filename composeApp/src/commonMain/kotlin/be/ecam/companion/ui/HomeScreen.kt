package be.ecam.companion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import be.ecam.companion.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onOpenAdmins: () -> Unit,
    onOpenStudents: () -> Unit,
    onOpenCalendar: () -> Unit,
//    onOpenSettings: () -> Unit
) {
    val vm = koinInject<HomeViewModel>()
    LaunchedEffect(Unit) { vm.load() }

    Row(modifier = Modifier.fillMaxSize()) {

        // -------------------------
        //   🌟 SIDEBAR 🌟
        // -------------------------
        Column(
            modifier = Modifier
                .width(200.dp)
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Menu", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            SidebarButton("Home") { /* reste sur Home */ }
            SidebarButton("Calendar") { onOpenCalendar() }
//          SidebarButton("Settings") { onOpenSettings() }
        }

        // -------------------------
        //   CONTENU PRINCIPAL 🌟
        // -------------------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            Spacer(Modifier.height(20.dp))

            Text(vm.helloMessage)
        }
    }
}

@Composable
fun SidebarButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(label)
    }
}
