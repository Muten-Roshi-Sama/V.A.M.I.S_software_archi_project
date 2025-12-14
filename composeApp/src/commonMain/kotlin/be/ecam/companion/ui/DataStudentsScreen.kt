package be.ecam.companion.ui
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import be.ecam.companion.data.ApiRepository
//import be.ecam.companion.data.StudentBulletin
//import kotlinx.coroutines.launch
//import org.koin.compose.koinInject
//
//@Composable //Compose function = one screen
//fun DataStudentsScreen(onBack: () -> Unit) { //onBack = when you click “Back,” you return to the previous screen
//    val repository = koinInject<ApiRepository>() //It is ktorApiReapository that knows how to retrieve data
//
//    //state variables//
//    var students by remember { mutableStateOf<List<StudentBulletin>?>(null) }
//    var isLoading by remember { mutableStateOf(true) }
//    var error by remember { mutableStateOf<String?>(null) }
//    val scope = rememberCoroutineScope() //To launch coroutines (background tasks)
//
//    LaunchedEffect(Unit) {
//        scope.launch {
//            try {
//                students = repository.fetchAllStudentBulletins() //update “students” if we have a response
//            } catch (e: Exception) {
//                error = "Erreur : ${e.message}"
//            } finally {
//                isLoading = false //the interface refreshes
//            }
//        }
//    }
//
//    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
//            TextButton(onClick = onBack) { Text("Retour") }
//        }
//        Text("Bulletins des étudiants", style = MaterialTheme.typography.titleLarge)
//        Spacer(Modifier.height(16.dp))
//        //Depending on the status, the following is displayed:The spinning circle, An error message, “No students”, Or the actual list
//        when {
//            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
//            error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
//            students.isNullOrEmpty() -> Text("Aucun étudiant")
//            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
//                items(students!!) { student ->
//                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Text("${student.firstName} ${student.lastName}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
//                            Text("Matricule: ${student.matricule} • ${student.year}")
//                            Spacer(Modifier.height(12.dp))
//                            Text("Notes :", fontWeight = FontWeight.SemiBold)
//                            student.evaluations.forEach { eval ->
//                                val percent = (eval.score.toDouble() / eval.maxScore * 100).toInt()
//                                val success = eval.score >= eval.maxScore * 0.5
//                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
//                                    Text("• ${eval.activityName}")
//                                    Text("${eval.score}/${eval.maxScore} ($percent%)",
//                                        color = if (success) Color(0xFF2E7D32) else Color.Red,
//                                        fontWeight = FontWeight.Bold)
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}