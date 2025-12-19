package be.ecam.companion.ui.admin



import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.ecam.common.api.StudentBulletin
import be.ecam.common.api.Evaluation
import be.ecam.companion.data.ApiRepository
import be.ecam.companion.ui.AppDrawer
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AllGrades(
    onBack: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHome: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val repo = koinInject<ApiRepository>()
    var bulletin by remember { mutableStateOf<StudentBulletin?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                bulletin = repo.fetchMyGrades()
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Menu + back
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                }
                TextButton(onClick = onBack) {
                    Text("← Back")
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "My Grades",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Text("Error: $error", color = Color.Red)
                }
                bulletin == null -> {
                    Text("No grades available")
                }
                else -> {
                    val b = bulletin!!

                    // Infos étudiant
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = "${b.firstName} ${b.lastName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Email: ${b.studentEmail}")
                            Text("Matricule: ${b.matricule}")
                            Text("Year: ${b.year}")
                            b.option?.let { Text("Option: $it") }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Evaluations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    // Table header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5DDFF))
                            .border(1.dp, Color.Gray)
                            .padding(vertical = 10.dp)
                    ) {
                        TableCell("Course name", weight = 0.55f, isHeader = true)
                        TableCell("Session", weight = 0.15f, isHeader = true)
                        TableCell("Score", weight = 0.15f, isHeader = true)
                        TableCell("Max", weight = 0.15f, isHeader = true)
                    }

                    Spacer(Modifier.height(2.dp))

                    // Body scrollable
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(b.evaluations) { eval ->
                            EvaluationRow(eval)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    gradeColor: Boolean = false
) {
    val color = when {
        gradeColor -> {
            val g = text.toIntOrNull()
            if (g != null && g < 10) Color.Red else Color(0xFF00AA00)
        }
        else -> Color.Black
    }

    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 12.dp),
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        color = color
    )
}

@Composable
fun EvaluationRow(evaluation: Evaluation) {
    val percentage = (evaluation.score.toFloat() / evaluation.maxScore * 100).toInt()
    val scoreColor = if (percentage >= 50) Color(0xFF00AA00) else Color.Red

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray)
            .padding(vertical = 12.dp)
    ) {
        TableCell(evaluation.activityName, weight = 0.55f)
        TableCell(evaluation.session, weight = 0.15f)
        TableCell(evaluation.score.toString(), weight = 0.15f, gradeColor = true)
        TableCell(evaluation.maxScore.toString(), weight = 0.15f)
    }
}
