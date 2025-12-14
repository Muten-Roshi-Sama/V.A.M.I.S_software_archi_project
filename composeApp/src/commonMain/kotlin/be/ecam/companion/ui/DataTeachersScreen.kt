package be.ecam.companion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.ecam.common.api.Teacher
import be.ecam.companion.data.ApiRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu


@Composable
fun DataTeacherScreen(
    onBack: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val repo = koinInject<ApiRepository>()
    var teachers by remember { mutableStateOf<List<Teacher>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try { teachers = repo.fetchAllTeachers() }
            catch (e: Exception) { error = e.message }
            finally { isLoading = false }
        }
    }

    AppDrawer(
        drawerState = drawerState,
        scope = scope,
        onOpenCalendar = onOpenCalendar,
        onOpenSettings = onOpenSettings
    ){
        Column(Modifier.fillMaxSize().padding(16.dp)) {

            IconButton(
                onClick = { scope.launch { drawerState.open() } }
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }


            Row { TextButton(onClick = onBack) { Text("Retour") } }
            Text("Enseignants", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                teachers.isNullOrEmpty() -> Text("Aucun enseignant")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(teachers!!) { t ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text("${t.first_name} ${t.last_name}", style = MaterialTheme.typography.titleMedium)
                                Text(t.email)
                                Spacer(Modifier.height(8.dp))
                                t.modules.forEach { m ->
                                    Text("• ${m.activity_name} (${m.activity_code}) — ${m.ects} ECTS")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}