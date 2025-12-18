package be.ecam.companion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun IspEditScreen(
    onConfirm: (IspCourseUi) -> Unit,
    onCancel: () -> Unit
) {
    val tableWidth = 1100.dp
    val hScroll = rememberScrollState()
    val scope = rememberCoroutineScope()

    var courseName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var program by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var ects by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }

    // ✅ MOCK CATALOG (à remplacer par backend plus tard)
    val catalog = remember {
        listOf(
            IspCourseUi("Web Development", "WD4P", "COM", 4, "DBS", 5, 27),
            IspCourseUi("Advanced Database", "AD3T", "COM", 3, "SRZ", 5, 14),
            IspCourseUi("Accounting", "AC4T", "COM", 4, "VMN", 5, 20),
            IspCourseUi("Algorithm Complexity", "AL4T", "COM", 4, "RSS", 5, 24),
            IspCourseUi("Artificial Intelligence", "AI4P", "COM", 4, "RCH", 5, 22),
            IspCourseUi("Project Management", "PM4T", "COM", 4, "ROM", 5, 22),
            IspCourseUi("Networks", "NW2T", "ELEC", 2, "KLM", 4, 30),
            IspCourseUi("Signals & Systems", "SS3T", "ELEC", 3, "HJK", 5, 28),
        )
    }

    // ✅ Liste filtrée selon les champs (vide => on n’affiche rien)
    val matches = remember(courseName, courseCode, program, year, teacher, ects, hours) {
        filterCatalog(
            catalog = catalog,
            courseName = courseName,
            courseCode = courseCode,
            program = program,
            year = year,
            teacher = teacher,
            ects = ects,
            hours = hours
        )
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        // Top bar (back)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(10.dp))
            Text("ISP", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(Modifier.height(10.dp))

        // ✅ Zone centrale scroll horizontale (table + résultats)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Box(modifier = Modifier.horizontalScroll(hScroll)) {
                Column(modifier = Modifier.width(tableWidth)) {

                    // Table header + 1 ligne d’inputs (ZDT)
                    IspEditTopTable(
                        courseName = courseName, onCourseName = { courseName = it },
                        courseCode = courseCode, onCourseCode = { courseCode = it },
                        program = program, onProgram = { program = it },
                        year = year, onYear = { year = it },
                        teacher = teacher, onTeacher = { teacher = it },
                        ects = ects, onEcts = { ects = it },
                        hours = hours, onHours = { hours = it },
                    )

                    Spacer(Modifier.height(16.dp))

                    // ✅ CADRE = LISTE DES COURS CORRESPONDANTS (PLUS DE GROS TEXTE)
                    MatchesBox(
                        matches = matches,
                        onPick = { picked ->
                            courseName = picked.courseName
                            courseCode = picked.courseCode
                            program = picked.program
                            year = picked.year.toString()
                            teacher = picked.teacher
                            ects = picked.ects.toString()
                            hours = picked.hours.toString()
                        }
                    )
                }
            }
        }

        // slider optionnel (si overflow horizontal)
        if (hScroll.maxValue > 0) {
            Spacer(Modifier.height(10.dp))
            Slider(
                value = hScroll.value.toFloat(),
                onValueChange = { v -> scope.launch { hScroll.scrollTo(v.toInt()) } },
                valueRange = 0f..hScroll.maxValue.toFloat()
            )
        }

        Spacer(Modifier.height(18.dp))

        // Confirm / Cancel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = {
                    onConfirm(
                        IspCourseUi(
                            courseName = courseName,
                            courseCode = courseCode,
                            program = program,
                            year = year.toIntOrNull() ?: 0,
                            teacher = teacher,
                            ects = ects.toIntOrNull() ?: 0,
                            hours = hours.toIntOrNull() ?: 0
                        )
                    )
                }
            ) { Text("Confirm") }

            Spacer(Modifier.width(12.dp))

            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@Composable
private fun MatchesBox(
    matches: List<IspCourseUi>,
    onPick: (IspCourseUi) -> Unit
) {
    val line = MaterialTheme.colorScheme.outline
    val rowH = 44.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, line, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        // ✅ Si aucun match : on n’affiche rien (cadre vide, comme tu veux)
        if (matches.isEmpty()) return@Box

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(matches) { c ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowH)
                        .clickable { onPick(c) }
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // rendu type "ligne de tableau" (mêmes poids que le header)
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

private fun filterCatalog(
    catalog: List<IspCourseUi>,
    courseName: String,
    courseCode: String,
    program: String,
    year: String,
    teacher: String,
    ects: String,
    hours: String
): List<IspCourseUi> {
    fun String.norm() = trim().lowercase()
    val nName = courseName.norm()
    val nCode = courseCode.norm()
    val nProg = program.norm()
    val nTeacher = teacher.norm()
    val nYear = year.trim()
    val nEcts = ects.trim()
    val nHours = hours.trim()

    val anyFilter =
        nName.isNotEmpty() || nCode.isNotEmpty() || nProg.isNotEmpty() ||
        nTeacher.isNotEmpty() || nYear.isNotEmpty() || nEcts.isNotEmpty() || nHours.isNotEmpty()

    if (!anyFilter) return emptyList() // ✅ si rien tapé : pas de liste

    return catalog.filter { c ->
        val okName = nName.isEmpty() || c.courseName.lowercase().contains(nName)
        val okCode = nCode.isEmpty() || c.courseCode.lowercase().contains(nCode)
        val okProg = nProg.isEmpty() || c.program.lowercase().contains(nProg)
        val okTeacher = nTeacher.isEmpty() || c.teacher.lowercase().contains(nTeacher)

        // ✅ pour les nombres : on autorise recherche progressive (startsWith)
        val okYear = nYear.isEmpty() || c.year.toString().startsWith(nYear)
        val okEcts = nEcts.isEmpty() || c.ects.toString().startsWith(nEcts)
        val okHours = nHours.isEmpty() || c.hours.toString().startsWith(nHours)

        okName && okCode && okProg && okTeacher && okYear && okEcts && okHours
    }.take(50)
}

@Composable
private fun IspEditTopTable(
    courseName: String, onCourseName: (String) -> Unit,
    courseCode: String, onCourseCode: (String) -> Unit,
    program: String, onProgram: (String) -> Unit,
    year: String, onYear: (String) -> Unit,
    teacher: String, onTeacher: (String) -> Unit,
    ects: String, onEcts: (String) -> Unit,
    hours: String, onHours: (String) -> Unit,
) {
    val line = MaterialTheme.colorScheme.outline
    val rowH = 44.dp

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .border(1.dp, line, RoundedCornerShape(12.dp))
    ) {
        // Header
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

        // Input row (ZDT)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowH),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InputCell(courseName, onCourseName, 0.40f, "ZDT")
            VLine(line)
            InputCell(courseCode, onCourseCode, 0.14f, "ZDT", TextAlign.Center)
            VLine(line)
            InputCell(program, onProgram, 0.10f, "ZDT", TextAlign.Center)
            VLine(line)
            InputCell(year, onYear, 0.08f, "ZDT", TextAlign.Center)
            VLine(line)
            InputCell(teacher, onTeacher, 0.10f, "ZDT", TextAlign.Center)
            VLine(line)
            InputCell(ects, onEcts, 0.08f, "ZDT", TextAlign.Center)
            VLine(line)
            InputCell(hours, onHours, 0.10f, "ZDT", TextAlign.Center)
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
private fun RowScope.InputCell(
    value: String,
    onValue: (String) -> Unit,
    w: Float,
    placeholder: String,
    align: TextAlign = TextAlign.Start
) {
    Box(
        modifier = Modifier
            .weight(w)
            .fillMaxHeight()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValue,
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = align
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
        if (value.isEmpty()) {
            Text(
                placeholder,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                textAlign = align,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
private fun VLine(color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(color)
    )
}
