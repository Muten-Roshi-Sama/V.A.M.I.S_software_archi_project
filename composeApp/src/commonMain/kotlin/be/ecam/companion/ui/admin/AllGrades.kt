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
    var bulletins by remember {
        mutableStateOf<List<StudentBulletin>>(emptyList())
    }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            bulletins = repo.fetchAllStudentBulletins()
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
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
                "Student's grades",
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
                bulletins == null -> {
                    Text("No grades available")
                }
                else -> {
                    if (bulletins.isEmpty()) {
                        Text("No grades available")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(bulletins) { b ->

                                // ===== Carte étudiant =====
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            "${b.firstName} ${b.lastName}",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text("Email: ${b.studentEmail}")
                                        Text("Matricule: ${b.matricule}")
                                        Text("Year: ${b.year}")
                                        b.option?.let { Text("Option: $it") }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // ===== Header tableau =====
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF5DDFF))
                                        .border(1.dp, Color.Gray)
                                        .padding(vertical = 10.dp)
                                ) {
                                    TableCell("Course", 0.55f, true)
                                    TableCell("Session", 0.15f, true)
                                    TableCell("Score", 0.15f, true)
                                    TableCell("Max", 0.15f, true)
                                }

                                // ===== Évaluations =====
                                b.evaluations.forEach { eval ->
                                    EvaluationRow(eval)
                                }
                            }
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
