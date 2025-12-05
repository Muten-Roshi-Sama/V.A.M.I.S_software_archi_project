package be.ecam.companion.ui

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
import be.ecam.common.api.AdminDTO
import be.ecam.companion.data.ApiRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ListAdmins(onBack: () -> Unit) {
    val repository = koinInject<ApiRepository>()
    val scope = rememberCoroutineScope()

    var admins by remember { mutableStateOf<List<AdminDTO>>(emptyList()) }
    var adminCount by remember { mutableStateOf(0L) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingAdmin by remember { mutableStateOf<AdminDTO?>(null) }

    fun loadAdmins() {
        scope.launch {
            isLoading = true
            error = null
            try {
                admins = repository.fetchAdmins()
                adminCount = repository.fetchAdminCount()
                println("✅ ADMINS FETCHED: ${admins.size} admins, count=$adminCount")
            } catch (e: Exception) {
                error = e.message ?: "Unknown error"
                println("❌ ERROR fetching admins: ${e.stackTraceToString()}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadAdmins() }

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
                Icon(Icons.Filled.Add, contentDescription = "Add admin")
            }
        }

        Text("Admins (Total: $adminCount)", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        when {
            isLoading -> CircularProgressIndicator()
            error != null -> {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
                Button(onClick = { loadAdmins() }) { Text("Retry") }
            }
            admins.isEmpty() -> Text("No admins found")
            else -> LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(admins) { admin ->
                    AdminCard(
                        admin = admin,
                        onEdit = { editingAdmin = it },
                        onDelete = {
                            scope.launch {
                                try {
                                    val success = repository.deleteAdmin(it.id!!)
                                    if (success) {
                                        println("✅ DELETED admin id=${it.id}")
                                        loadAdmins()
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
        AdminDialog(
            title = "Create Admin",
            onDismiss = { showCreateDialog = false },
            onConfirm = { newAdmin ->
                scope.launch {
                    try {
                        val created = repository.createAdmin(newAdmin)
                        println("✅ CREATED admin id=${created.id}")
                        showCreateDialog = false
                        loadAdmins()
                    } catch (e: Exception) {
                        error = "Create failed: ${e.message}"
                    }
                }
            }
        )
    }

    if (editingAdmin != null) {
        AdminDialog(
            title = "Edit Admin",
            admin = editingAdmin,
            onDismiss = { editingAdmin = null },
            onConfirm = { updatedAdmin ->
                scope.launch {
                    try {
                        val updated = repository.updateAdmin(editingAdmin!!.id!!, updatedAdmin)
                        println("✅ UPDATED admin id=${updated.id}")
                        editingAdmin = null
                        loadAdmins()
                    } catch (e: Exception) {
                        error = "Update failed: ${e.message}"
                    }
                }
            }
        )
    }
}

@Composable
fun AdminCard(
    admin: AdminDTO,
    onEdit: (AdminDTO) -> Unit,
    onDelete: (AdminDTO) -> Unit
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
                Text("ID: ${admin.id}", style = MaterialTheme.typography.labelSmall)
                Text("${admin.firstName} ${admin.lastName}", style = MaterialTheme.typography.titleMedium)
                Text(admin.email, style = MaterialTheme.typography.bodyMedium)
                Text("Created: ${admin.createdAt?.take(10) ?: "N/A"}", style = MaterialTheme.typography.labelSmall)
            }
            Row {
                IconButton(onClick = { onEdit(admin) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDelete(admin) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AdminDialog(
    title: String,
    admin: AdminDTO? = null,
    onDismiss: () -> Unit,
    onConfirm: (AdminDTO) -> Unit
) {
    var firstName by remember { mutableStateOf(admin?.firstName ?: "") }
    var lastName by remember { mutableStateOf(admin?.lastName ?: "") }
    var email by remember { mutableStateOf(admin?.email ?: "") }
    var password by remember { mutableStateOf("") }

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
                    label = { Text(if (admin == null) "Password*" else "Password (leave empty to keep)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        AdminDTO(
                            id = admin?.id,
                            firstName = firstName.takeIf { it.isNotBlank() },
                            lastName = lastName.takeIf { it.isNotBlank() },
                            email = email,
                            password = password.takeIf { it.isNotBlank() }
                        )
                    )
                },
                enabled = email.isNotBlank() && (admin != null || password.isNotBlank())
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}