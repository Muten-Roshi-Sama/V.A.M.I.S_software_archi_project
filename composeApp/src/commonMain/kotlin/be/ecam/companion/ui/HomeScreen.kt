package be.ecam.companion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import be.ecam.companion.viewmodel.HomeViewModel

@Composable
fun HomeScreen(onOpenAdmins: () -> Unit, onOpenStudents: () -> Unit, onOpenTeachers: () -> Unit, onOpenBible: () -> Unit) {
    val vm = koinInject<HomeViewModel>()
    LaunchedEffect(Unit) { vm.load() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Home", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        if (vm.lastErrorMessage.isNotEmpty()) {
            Text(vm.lastErrorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(onClick = onOpenAdmins) { Text("Open Admins List") }
        Spacer(Modifier.height(12.dp))

        //When you click → the onOpenStudents() function is called(Which is defined in App.kt)
        Button(onClick = onOpenStudents) {
            Text("See all student reports")
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onOpenTeachers) { Text("Voir les enseignants") }

        Spacer(Modifier.height(12.dp))
        Button(onClick = onOpenBible) {
            Text("📚 Bible des Programmes")
        }
        Spacer(Modifier.height(12.dp))

        Text(vm.helloMessage)
    }
}
