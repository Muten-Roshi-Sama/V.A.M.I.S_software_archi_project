package be.ecam.companion.ui.student

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
import be.ecam.common.api.StudentBulletin
import be.ecam.common.api.Evaluation
import be.ecam.companion.data.ApiRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGradesScreen(onBack: () -> Unit) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Grades") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Back") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
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
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = "${b.firstName} ${b.lastName}",
                                style = MaterialTheme.typography.titleLarge,
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

                    if (b.evaluations.isEmpty()) {
                        Text("No evaluations yet", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(b.evaluations) { eval ->
                                EvaluationCard(eval)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EvaluationCard(evaluation: Evaluation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = evaluation.activityName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Session: ${evaluation.session}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Score:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                val percentage = (evaluation.score.toFloat() / evaluation.maxScore * 100).toInt()
                val scoreColor = when {
                    percentage >= 50 -> Color(0xFF4CAF50)
                    else -> Color(0xFFF44336)
                }
                
                Text(
                    text = "${evaluation.score} / ${evaluation.maxScore} ($percentage%)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }
        }
    }
}
