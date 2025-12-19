package be.ecam.companion.ui.admin

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.ecam.common.api.Evaluation

@Composable
fun EvaluationRow(
    evaluation: Evaluation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray)
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        TableCell(evaluation.activityName, 0.55f)
        TableCell(evaluation.session, 0.15f)
        TableCell(evaluation.score.toString(), 0.15f, gradeColor = true)
        TableCell(evaluation.maxScore.toString(), 0.15f)
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
