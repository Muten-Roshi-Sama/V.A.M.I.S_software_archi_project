package be.ecam.companion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class IspCourseUi(
    val courseName: String,
    val courseCode: String,
    val program: String,
    val year: Int,
    val teacher: String,
    val ects: Int,
    val hours: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IspListScreen(
    courses: List<IspCourseUi>,
    onAddCourse: () -> Unit,
    onBack: () -> Unit,
    onOpenHome: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val tableWidth = 1100.dp
    val hScroll = rememberScrollState()

    AppDrawer(
        drawerState = drawerState,
        scope = scope,
        onOpenCalendar = onOpenCalendar,
        onOpenSettings = onOpenSettings,
        onOpenHome = onOpenHome
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            IconButton(
                onClick = { scope.launch { drawerState.open() } }
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Spacer(Modifier.width(10.dp))
                Text("ISP", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Button(onClick = onAddCourse) { Text("Add a course") }
            }

            Spacer(Modifier.height(16.dp))

            // ✅ Centré comme mockup + scroll horizontal si besoin
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                Box(modifier = Modifier.horizontalScroll(hScroll)) {
                    IspListTable(
                        courses = courses,
                        modifier = Modifier.width(tableWidth)
                    )
                }
            }

            // slider optionnel
            if (hScroll.maxValue > 0) {
                Spacer(Modifier.height(10.dp))
                Slider(
                    value = hScroll.value.toFloat(),
                    onValueChange = { v -> scope.launch { hScroll.scrollTo(v.toInt()) } },
                    valueRange = 0f..hScroll.maxValue.toFloat()
                )
            }
        }
    }
}

@Composable
private fun IspListTable(
    courses: List<IspCourseUi>,
    modifier: Modifier = Modifier
) {
    val line = MaterialTheme.colorScheme.outline
    val rowH = 44.dp

    // hauteur du body = nb de lignes (max 8 visibles, sinon scroll)
    val maxVisible = 8
    val visible = if (courses.isEmpty()) 1 else minOf(courses.size, maxVisible)
    val bodyHeight = rowH * visible.toFloat()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .border(1.dp, line, RoundedCornerShape(12.dp))
    ) {
        // header compact (pas giant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowH)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderCell("Course name", 0.40f, TextAlign.Start)
            VLine(line)
            HeaderCell("Course code", 0.14f, TextAlign.Center)
            VLine(line)
            HeaderCell("Program", 0.10f, TextAlign.Center)
            VLine(line)
            HeaderCell("Year", 0.08f, TextAlign.Center)
            VLine(line)
            HeaderCell("Teacher", 0.10f, TextAlign.Center)
            VLine(line)
            HeaderCell("ECTS", 0.08f, TextAlign.Center)
            VLine(line)
            HeaderCell("Hours", 0.10f, TextAlign.Center)
        }
        HorizontalDivider(color = line)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(bodyHeight) // ✅ empêche le tableau de prendre tout l’écran
        ) {
            if (courses.isEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowH),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BodyCell("No courses yet", 1f, TextAlign.Center)
                    }
                }
            } else {
                items(courses) { c ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowH),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BodyCell(c.courseName, 0.40f, TextAlign.Start)
                        VLine(line)
                        BodyCell(c.courseCode, 0.14f, TextAlign.Center)
                        VLine(line)
                        BodyCell(c.program, 0.10f, TextAlign.Center)
                        VLine(line)
                        BodyCell(c.year.toString(), 0.08f, TextAlign.Center)
                        VLine(line)
                        BodyCell(c.teacher, 0.10f, TextAlign.Center)
                        VLine(line)
                        BodyCell(c.ects.toString(), 0.08f, TextAlign.Center)
                        VLine(line)
                        BodyCell(c.hours.toString(), 0.10f, TextAlign.Center)
                    }
                    HorizontalDivider(color = line)
                }
            }
        }
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, w: Float, align: TextAlign) {
    Text(
        text = text,
        modifier = Modifier.weight(w).padding(horizontal = 10.dp),
        textAlign = align,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        maxLines = 1
    )
}

@Composable
private fun RowScope.BodyCell(text: String, w: Float, align: TextAlign) {
    Text(
        text = text,
        modifier = Modifier.weight(w).padding(horizontal = 10.dp),
        textAlign = align,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1
    )
}

@Composable
private fun VLine(color: Color) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(color)
    )
}

@Composable
private fun NavDrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (pressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
