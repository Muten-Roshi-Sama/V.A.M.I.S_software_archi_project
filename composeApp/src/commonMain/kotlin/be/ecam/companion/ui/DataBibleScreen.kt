package be.ecam.companion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.ecam.common.api.ProgramWithDetails
import be.ecam.companion.data.ApiRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu


@Composable
fun DataBibleScreen(
                    onBack: () -> Unit,
                    onOpenCalendar: () -> Unit,
                    onOpenSettings: () -> Unit,
                    onOpenHome: () -> Unit,) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val repo = koinInject<ApiRepository>()
    var programs by remember { mutableStateOf<List<ProgramWithDetails>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(Unit) {
        scope.launch {
            try {
                programs = repo.fetchBible()
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }
    AppDrawer(
        drawerState = drawerState,
        scope = scope,
        onOpenCalendar = onOpenCalendar,
        onOpenSettings = onOpenSettings,
        onOpenHome = onOpenHome
    ){


        Column(Modifier.fillMaxSize().padding(16.dp)) {
            IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
            Row {
                TextButton(onClick = onBack) {
                    Text("Retour")
                }
            }
            Text("Bible des Programmes d'Études", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                programs.isNullOrEmpty() -> Text("Aucun programme trouvé")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(programs!!) { program ->
                        ProgramCard(program)
                    }
                }
            }
        }
    }

}

@Composable
fun ProgramCard(program: ProgramWithDetails) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            // En-tête du programme
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = program.year + if (program.optionCode != null) " - ${program.optionName}" else "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "${program.totalEcts} ECTS",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Masquer" else "Voir détails")
                }
            }

            // Détails (si expanded)
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                // Liste des cours
                Text("📚 Cours (${program.courses.size})", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                program.courses.forEach { course ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• ${course.courseName}", fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Text("${course.totalHours}h", fontSize = 14.sp, color = Color.Gray)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Liste des modules
                Text("🎓 Modules (${program.modules.size})", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                program.modules.forEach { module ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(module.activityName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text("Code: ${module.activityCode} | ${module.ects} ECTS", fontSize = 12.sp, color = Color.Gray)
                            if (module.description.isNotEmpty()) {
                                Text(module.description, fontSize = 12.sp, color = Color.Gray)
                            }
                            Text("Coordinateur: ${module.coordinator}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}