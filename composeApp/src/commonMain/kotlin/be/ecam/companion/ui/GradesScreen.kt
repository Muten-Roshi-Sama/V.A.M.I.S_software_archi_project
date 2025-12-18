package be.ecam.companion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class CourseGradeUi(
    val courseName: String,
    val courseCode: String,
    val grade: Int?, // null => pas encore de note => NE PAS AFFICHER
    val ects: Int
)

@Composable
fun GradesScreen(
    grades: List<CourseGradeUi>,
    onOpenHome: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onOpenGrades: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenIspList: () -> Unit = {},

) {
    // règle demandée : si pas de note => pas affiché
    val visibleGrades = remember(grades) { grades.filter { it.grade != null } }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .width(240.dp)
                    .fillMaxHeight()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                    .shadow(8.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 24.dp, horizontal = 16.dp)
            ) {
                Text("Menu", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(24.dp))

                DrawerItem("Home", Icons.Filled.Home) {
                    scope.launch { drawerState.close() }
                    onOpenHome()
                }
                DrawerItem("Calendar", Icons.Filled.CalendarMonth) {
                    scope.launch { drawerState.close() }
                    onOpenCalendar()
                }

                DrawerItem("ISP", Icons.Filled.Bookmarks) {
                    scope.launch { drawerState.close() }
                    onOpenIspList()
                }

                DrawerItem("Grades", Icons.Filled.Check) {
                    scope.launch { drawerState.close() }
                    onOpenGrades()
                }
                DrawerItem("Settings", Icons.Filled.Settings) {
                    scope.launch { drawerState.close() }
                    onOpenSettings()
                }
            }
        },
        scrimColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.32f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header (menu + titre "Grades")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                }
                Spacer(Modifier.width(8.dp))
                Text("Grades", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            // Table container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // ✅ prend l’espace et laisse la bottom bar en bas
                    .border(2.dp, Color.Gray)
            ) {
                // Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5DDFF))
                        .border(1.dp, Color.Gray)
                        .padding(vertical = 10.dp)
                ) {
                    TableCell("Course name", weight = 0.55f, isHeader = true)
                    TableCell("Course code", weight = 0.20f, isHeader = true)
                    TableCell("Grades", weight = 0.15f, isHeader = true)
                    TableCell("ECTS", weight = 0.10f, isHeader = true)
                }

                // Body (scrollable)
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visibleGrades) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Gray)
                                .padding(vertical = 14.dp)
                        ) {
                            TableCell(item.courseName, weight = 0.55f)
                            TableCell(item.courseCode, weight = 0.20f)
                            TableCell(item.grade!!.toString(), weight = 0.15f, gradeColor = true)
                            TableCell(item.ects.toString(), weight = 0.10f)
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
