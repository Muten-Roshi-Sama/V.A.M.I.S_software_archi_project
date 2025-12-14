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
import be.ecam.companion.data.ApiRepository
import be.ecam.common.api.Evaluation          //Shared DTO
import be.ecam.common.api.StudentBulletin    //Shared DTO
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu


@Composable
fun DataStudentsScreen(
    onBack: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val repository = koinInject<ApiRepository>()
    var students by remember { mutableStateOf<List<StudentBulletin>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                students = repository.fetchAllStudentBulletins()
            } catch (e: Exception) {
                error = "Erreur : ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    AppDrawer(
        drawerState = drawerState,
        scope = scope,
        onOpenCalendar = onOpenCalendar,
        onOpenSettings = onOpenSettings
    ){
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier.align(Alignment.Start)
            ) {
                IconButton(
                    onClick = { scope.launch { drawerState.open() } },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                }
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                TextButton(onClick = onBack) {
                    Text("Retour")
                }
            }

            Text("Bulletins des étudiants", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                error != null -> {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
                students.isNullOrEmpty() -> {
                    Text("Aucun étudiant")
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(students!!) { student ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "${student.firstName} ${student.lastName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    Text("Matricule: ${student.matricule} • ${student.year}")
                                    Spacer(Modifier.height(12.dp))
                                    Text("Notes :", fontWeight = FontWeight.SemiBold)

                                    student.evaluations.forEach { eval ->
                                        val percent = (eval.score.toDouble() / eval.maxScore * 100).toInt()
                                        val success = eval.score >= eval.maxScore * 0.5

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("• ${eval.activityName}")
                                            Text(
                                                text = "${eval.score}/${eval.maxScore} ($percent%)",
                                                color = if (success) Color(0xFF2E7D32) else Color.Red,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}