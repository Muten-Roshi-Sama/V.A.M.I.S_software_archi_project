package be.ecam.companion.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.ecam.common.api.StudentDTO
import be.ecam.companion.data.ApiRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ListStudents(onBack: () -> Unit) {
    val repository = koinInject<ApiRepository>()
    val scope = rememberCoroutineScope()

    var students by remember { mutableStateOf<List<StudentDTO>>(emptyList()) }
    var studentCount by remember { mutableStateOf(0L) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<StudentDTO?>(null) }

    fun loadStudents() {
        scope.launch {
            isLoading = true
            error = null
            try {
                students = repository.fetchStudents()
                studentCount = repository.fetchStudentCount()
                println("✅ STUDENTS FETCHED: ${students.size} students, count=$studentCount")
            } catch (e: Exception) {
                error = e.message ?: "Unknown error"
                println("❌ ERROR fetching students: ${e.stackTraceToString()}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadStudents() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("← Back") }
            IconButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add student")
            }
        }

        Text("Students (Total: $studentCount)", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        when {
            isLoading -> CircularProgressIndicator()
            error != null -> {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
                Button(onClick = { loadStudents() }) { Text("Retry") }
            }
            students.isEmpty() -> Text("No students found")
            else -> LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(students) { student ->
                    StudentCard(
                        student = student,
                        onEdit = { editingStudent = it },
                        onDelete = {
                            scope.launch {
                                try {
                                    val success = repository.deleteStudent(it.id!!)
                                    if (success) {
                                        println("✅ DELETED student id=${it.id}")
                                        loadStudents()
                                    }
                                } catch (e: Exception) {
                                    error = "Delete failed: ${e.message}"
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        StudentDialog(
            title = "Create Student",
            onDismiss = { showCreateDialog = false },
            onConfirm = { newStudent ->
                scope.launch {
                    try {
                        val created = repository.createStudent(newStudent)
                        println("✅ CREATED student id=${created.id}")
                        showCreateDialog = false
                        loadStudents()
                    } catch (e: Exception) {
                        error = "Create failed: ${e.message}"
                    }
                }
            }
        )
    }

    if (editingStudent != null) {
        StudentDialog(
            title = "Edit Student",
            student = editingStudent,
            onDismiss = { editingStudent = null },
            onConfirm = { updatedStudent ->
                scope.launch {
                    try {
                        val updated = repository.updateStudent(editingStudent!!.id!!, updatedStudent)
                        println("✅ UPDATED student id=${updated.id}")
                        editingStudent = null
                        loadStudents()
                    } catch (e: Exception) {
                        error = "Update failed: ${e.message}"
                    }
                }
            }
        )
    }
}

@Composable
fun StudentCard(
    student: StudentDTO,
    onEdit: (StudentDTO) -> Unit,
    onDelete: (StudentDTO) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("ID: ${student.id}", style = MaterialTheme.typography.labelSmall)
                Text("${student.firstName} ${student.lastName}", style = MaterialTheme.typography.titleMedium)
                Text(student.email, style = MaterialTheme.typography.bodyMedium)
                student.studentId?.let { Text("Student ID: $it", style = MaterialTheme.typography.bodySmall) }
                student.studyYear?.let { Text("Year: $it", style = MaterialTheme.typography.bodySmall) }
                student.optionCode?.let { Text("Option: $it", style = MaterialTheme.typography.bodySmall) }
                Text("Created: ${student.createdAt?.take(10) ?: "N/A"}", style = MaterialTheme.typography.labelSmall)
            }
            Row {
                IconButton(onClick = { onEdit(student) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDelete(student) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun StudentDialog(
    title: String,
    student: StudentDTO? = null,
    onDismiss: () -> Unit,
    onConfirm: (StudentDTO) -> Unit
) {
    var firstName by remember { mutableStateOf(student?.firstName ?: "") }
    var lastName by remember { mutableStateOf(student?.lastName ?: "") }
    var email by remember { mutableStateOf(student?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf(student?.studentId ?: "") }
    var studyYear by remember { mutableStateOf(student?.studyYear ?: "") }
    var optionCode by remember { mutableStateOf(student?.optionCode ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (student == null) "Password*" else "Password (leave empty to keep)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text("Student ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = studyYear,
                    onValueChange = { studyYear = it },
                    label = { Text("Study Year (e.g., BA1, MA1)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = optionCode,
                    onValueChange = { optionCode = it },
                    label = { Text("Option Code (e.g., INFO, ELEC)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        StudentDTO(
                            id = student?.id,
                            studentId = studentId.takeIf { it.isNotBlank() },
                            firstName = firstName.takeIf { it.isNotBlank() },
                            lastName = lastName.takeIf { it.isNotBlank() },
                            email = email,
                            password = password.takeIf { it.isNotBlank() },
                            studyYear = studyYear.takeIf { it.isNotBlank() },
                            optionCode = optionCode.takeIf { it.isNotBlank() }
                        )
                    )
                },
                enabled = email.isNotBlank() && (student != null || password.isNotBlank())
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}