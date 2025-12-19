package be.ecam.companion.ui.admin
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.ecam.common.api.Evaluation

@Composable
fun EditGradeDialog(
    evaluation: Evaluation,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var newScore by remember { mutableStateOf(evaluation.score.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    newScore.toIntOrNull()?.let { onConfirm(it) }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Edit grade") },
        text = {
            Column {
                Text("${evaluation.activityName} (${evaluation.session})")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newScore,
                    onValueChange = { newScore = it },
                    label = { Text("Score") }
                )
            }
        }
    )
}
