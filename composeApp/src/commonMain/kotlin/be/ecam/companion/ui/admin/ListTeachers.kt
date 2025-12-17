package be.ecam.companion.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.ecam.companion.data.ApiRepository
import be.ecam.common.api.TeacherDTO
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListTeachers(onBack: () -> Unit) {
    val repository = koinInject<ApiRepository>()
    val scope = rememberCoroutineScope()
    var teachers by remember { mutableStateOf<List<TeacherDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingTeacher by remember { mutableStateOf<TeacherDTO?>(null) }

    fun loadTeachers() {
        scope.launch {
            try {
                teachers = repository.fetchTeachers()
                errorMessage = ""
            } catch (e: Exception) {
                errorMessage = "Error loading teachers: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadTeachers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teachers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Teacher")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(teachers) { teacher ->
                        TeacherCard(
                            teacher = teacher,
                            onEdit = { editingTeacher = teacher },
                            onDelete = {
                                scope.launch {
                                    try {
                                        repository.deleteTeacher(teacher.id!!)
                                        loadTeachers()
                                    } catch (e: Exception) {
                                        errorMessage = "Error deleting teacher: ${e.message}"
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        TeacherDialog(
            teacher = null,
            onSave = { newTeacher ->
                scope.launch {
                    try {
                        repository.createTeacher(newTeacher)
                        loadTeachers()
                        showCreateDialog = false
                    } catch (e: Exception) {
                        errorMessage = "Error creating teacher: ${e.message}"
                    }
                }
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    if (editingTeacher != null) {
        TeacherDialog(
            teacher = editingTeacher,
            onSave = { updatedTeacher ->
                scope.launch {
                    try {
                        repository.updateTeacher(editingTeacher!!.id!!, updatedTeacher)
                        loadTeachers()
                        editingTeacher = null
                    } catch (e: Exception) {
                        errorMessage = "Error updating teacher: ${e.message}"
                    }
                }
            },
            onDismiss = { editingTeacher = null }
        )
    }
}

@Composable
fun TeacherCard(
    teacher: TeacherDTO,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                Text("ID: ${teacher.id}", style = MaterialTheme.typography.labelSmall)
                Text("${teacher.firstName} ${teacher.lastName}", style = MaterialTheme.typography.titleMedium)
                Text(teacher.email, style = MaterialTheme.typography.bodySmall)
                if (!teacher.teacherId.isNullOrEmpty()) {
                    Text("Teacher ID: ${teacher.teacherId}", style = MaterialTheme.typography.labelSmall)
                }
                Text("Created: ${teacher.createdAt?.take(10) ?: "N/A"}", style = MaterialTheme.typography.labelSmall)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}




@Composable
fun TeacherDialog(
    teacher: TeacherDTO?,
    onSave: (TeacherDTO) -> Unit,
    onDismiss: () -> Unit
) {
    var firstName by remember { mutableStateOf(teacher?.firstName ?: "") }
    var lastName by remember { mutableStateOf(teacher?.lastName ?: "") }
    var email by remember { mutableStateOf(teacher?.email ?: "") }
    var password by remember { mutableStateOf(teacher?.password ?: "") }
    var teacherId by remember { mutableStateOf(teacher?.teacherId ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (teacher == null) "Create Teacher" else "Edit Teacher") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (teacher == null) {
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                TextField(
                    value = teacherId,
                    onValueChange = { teacherId = it },
                    label = { Text("Teacher ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        TeacherDTO(
                            id = teacher?.id,
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            password = if (teacher == null) password else null,
                            teacherId = teacherId.ifEmpty { null },
                            createdAt = teacher?.createdAt
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}