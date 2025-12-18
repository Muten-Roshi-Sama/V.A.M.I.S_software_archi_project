package be.ecam.companion.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.foundation.border
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import kotlinx.datetime.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import be.ecam.companion.data.Course
import be.ecam.companion.data.ApiRepository
import org.koin.compose.koinInject
import kotlinx.coroutines.launch




const val SLIDE_DURATION_MS = 100
const val FADE_DURATION_MS = 100

data class EventItem(
    val assignedTo: String,
    val notes: String
)

@OptIn(ExperimentalTime::class, ExperimentalAnimationApi::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier.background(MaterialTheme.colorScheme.surface),
    initialMode: CalendarMode? = null,
    initialAnchorDate: LocalDate? = null,
    initialDialogDate: LocalDate? = null,
    scheduledByDate: Map<LocalDate, List<String>> = emptyMap(),
    onOpenHome: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenGrades: () -> Unit = {},
    onOpenIspList: () -> Unit = {},

) {
    var today by remember {
        mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }
    var anchorDate by remember { mutableStateOf(initialAnchorDate ?: today) }
    var mode by remember { mutableStateOf(initialMode ?: CalendarMode.Month) }
    var slideDirection by remember { mutableStateOf(0) }

    // Panneau d’ajout d’événement
    var showAddEventPanel by remember { mutableStateOf(false) }

    // Map mutable interne pour gérer les évènements créés depuis l’UI
    var events by remember {
        mutableStateOf(scheduledByDate.toMutableMap())
    }

    // Champs du formulaire
    var assignedTo by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

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
                    .verticalScroll(state = rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
        Row(modifier = modifier.fillMaxSize().padding(8.dp)) {
            // 🗓️ Partie gauche : calendrier
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- En-tête : titre + profil ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = { scope.launch { drawerState.open() } }
                    ) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                    }

                    // Partie droite : profil + nom/prénom
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Image de profil
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Gray, shape = CircleShape)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "NOM",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Prénom",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // --- En-tête (mois, navigation) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                        Text(
                            text = "${
                                anchorDate.month.name.lowercase().replaceFirstChar { it.titlecase() }
                            } ${anchorDate.year}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Today",
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { slideDirection = 0; anchorDate = today },
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            Icons.Filled.ChevronLeft,
                            contentDescription = "Previous",
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { slideDirection = 1; anchorDate = mode.prev(anchorDate) }
                        )
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = "Next",
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { slideDirection = -1; anchorDate = mode.next(anchorDate) }
                        )
                    }
                }


                Spacer(Modifier.height(8.dp))

                // --- Boutons Week / Month ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = mode == CalendarMode.Week,
                        onClick = { slideDirection = 0; mode = CalendarMode.Week },
                        label = { Text("Week") }
                    )
                    FilterChip(
                        selected = mode == CalendarMode.Month,
                        onClick = { slideDirection = 0; mode = CalendarMode.Month },
                        label = { Text("Month") }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // --- Jours de la semaine ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    for (d in days) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                d,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                var dialogDate by remember { mutableStateOf(initialDialogDate) }

                // --- Zone du calendrier ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(mode, anchorDate) {
                            var triggered = false
                            var totalDx = 0f
                            val threshold = 60f
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount ->
                                    if (triggered) return@detectHorizontalDragGestures
                                    totalDx += dragAmount
                                    if (totalDx <= -threshold) {
                                        slideDirection = -1
                                        anchorDate = mode.next(anchorDate)
                                        triggered = true
                                    } else if (totalDx >= threshold) {
                                        slideDirection = 1
                                        anchorDate = mode.prev(anchorDate)
                                        triggered = true
                                    }
                                },
                                onDragEnd = { triggered = false; totalDx = 0f },
                                onDragCancel = { triggered = false; totalDx = 0f }
                            )
                        }
                ) {
                    AnimatedContent(
                        targetState = Pair(mode, anchorDate),
                        transitionSpec = {
                            val dir = slideDirection
                            if (dir < 0) {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(SLIDE_DURATION_MS)
                                ) + fadeIn(animationSpec = tween(SLIDE_DURATION_MS)) togetherWith
                                        slideOutOfContainer(
                                            AnimatedContentTransitionScope.SlideDirection.Left,
                                            animationSpec = tween(SLIDE_DURATION_MS)
                                        ) + fadeOut(animationSpec = tween(SLIDE_DURATION_MS))
                            } else if (dir > 0) {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(SLIDE_DURATION_MS)
                                ) + fadeIn(animationSpec = tween(SLIDE_DURATION_MS)) togetherWith
                                        slideOutOfContainer(
                                            AnimatedContentTransitionScope.SlideDirection.Right,
                                            animationSpec = tween(SLIDE_DURATION_MS)
                                        ) + fadeOut(animationSpec = tween(SLIDE_DURATION_MS))
                            } else {
                                fadeIn(animationSpec = tween(FADE_DURATION_MS)) togetherWith fadeOut(
                                    animationSpec = tween(FADE_DURATION_MS)
                                )
                            }
                        }, label = "calendarPager"
                    ) { (calendarMode, aDate) ->
                        when (calendarMode) {
                            CalendarMode.Month -> MonthGrid(
                                anchorDate = aDate,
                                today = today,
                                scheduledByDate = events,
                                onDateClick = { dateClicked -> dialogDate = dateClicked }
                            )

                            CalendarMode.Week -> WeekRow(
                                anchorDate = aDate,
                                today = today,
                                scheduledByDate = events,
                                onDateClick = { dateClicked -> dialogDate = dateClicked }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Bouton "Ajouter un évènement" + Formulaire à sa droite
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    // 🟦 Bouton principal
                    Button(
                        onClick = { showAddEventPanel = !showAddEventPanel }
                    ) {
                        Text("Ajouter un évènement")
                    }

                    // 🧩 Formulaire animé à droite du bouton
                    AnimatedVisibility(
                        visible = showAddEventPanel,
                        enter = slideInHorizontally(animationSpec = tween(200)) + fadeIn(),
                        exit = slideOutHorizontally(animationSpec = tween(200)) + fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(16.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // États pour la sélection de cours (accessibles depuis tout le formulaire)
                                var assignedCourse by remember { mutableStateOf<Course?>(null) }
                                var expandedCourses by remember { mutableStateOf(false) }
                                var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
                                var isLoadingCourses by remember { mutableStateOf(true) }
                                var courseLoadError by remember { mutableStateOf<String?>(null) }

                                // Récupération des cours via ApiRepository injecté par Koin
                                val apiRepository: ApiRepository = koinInject()
                                LaunchedEffect(Unit) {
                                    try {
                                        isLoadingCourses = true
                                        //courses = apiRepository.fetchAllCourses()
                                    } catch (e: Exception) {
                                        courseLoadError = e.message
                                    } finally {
                                        isLoadingCourses = false
                                    }
                                }

                                // 🧍 Assigné à + 📅 Date côte à côte
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Assigné à", fontWeight = FontWeight.SemiBold)

                                        if (isLoadingCourses) {
                                            Text("Chargement des cours...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                        } else if (courseLoadError != null) {
                                            Text("Erreur : $courseLoadError", color = Color.Red)
                                        } else {
                                            Box {
                                                TextField(
                                                    value = assignedCourse?.name ?: "Sélectionnez un cours",
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    modifier = Modifier
                                                        .fillMaxWidth(),
                                                    trailingIcon = {
                                                        IconButton(onClick = { expandedCourses = !expandedCourses }) {
                                                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Ouvrir la liste")
                                                        }
                                                    }
                                                )

                                                DropdownMenu(
                                                    expanded = expandedCourses,
                                                    onDismissRequest = { expandedCourses = false },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    courses.forEach { course ->
                                                        DropdownMenuItem(
                                                            text = { Text("${course.name} (${course.code})") },
                                                            onClick = {
                                                                assignedCourse = course
                                                                expandedCourses = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }


                                    // --- Champ Date avec calendrier intégré ---
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Date", fontWeight = FontWeight.SemiBold)

                                        var showCalendar by remember { mutableStateOf(false) }
                                        var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

                                        // 📅 État du mois/année actuellement affichés
                                        var currentMonth by remember {
                                            mutableStateOf(
                                                Clock.System.now()
                                                    .toLocalDateTime(TimeZone.currentSystemDefault()).date.month
                                            )
                                        }
                                        var currentYear by remember {
                                            mutableStateOf(
                                                Clock.System.now()
                                                    .toLocalDateTime(TimeZone.currentSystemDefault()).date.year
                                            )
                                        }

                                        val dateText = selectedDate?.let {
                                            val year = it.year.toString().padStart(4, '0')
                                            val month = it.month.number.toString().padStart(2, '0')
                                            val day = it.day.toString().padStart(2, '0')
                                            "$year-$month-$day"
                                        } ?: "Choisir une date"

                                        // 🟦 Champ cliquable (style TextField)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.surface,
                                                    shape = MaterialTheme.shapes.extraSmall
                                                )
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline,
                                                    MaterialTheme.shapes.extraSmall
                                                )
                                                .clickable { showCalendar = !showCalendar }
                                                .padding(12.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                text = dateText,
                                                color = if (selectedDate == null)
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End // aligne tout à droite
                                        ) {
                                        }


                                        Spacer(Modifier.height(4.dp))

                                        // 🗓️ Calendrier complet
                                        AnimatedVisibility(showCalendar) {
                                            val firstOfMonth = LocalDate(currentYear, currentMonth, 1)
                                            val lastOfMonth = firstOfMonth
                                                .plus(1, DateTimeUnit.MONTH)
                                                .minus(1, DateTimeUnit.DAY)
                                            val daysInMonth = lastOfMonth.day

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        MaterialTheme.colorScheme.surfaceVariant,
                                                        shape = MaterialTheme.shapes.small
                                                    )
                                                    .padding(8.dp)
                                            ) {
                                                // --- En-tête mois / année avec flèches ---
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Button(onClick = {
                                                        if (currentMonth == Month.JANUARY) {
                                                            currentMonth = Month.DECEMBER
                                                            currentYear -= 1
                                                        } else {
                                                            currentMonth = Month(currentMonth.number - 1)
                                                        }
                                                    }) {
                                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                                                    }

                                                    Text(
                                                        text = "${
                                                            currentMonth.name.lowercase()
                                                                .replaceFirstChar { it.uppercase() }
                                                        } $currentYear",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        modifier = Modifier.align(Alignment.CenterVertically)
                                                    )

                                                    Button(onClick = {
                                                        if (currentMonth == Month.DECEMBER) {
                                                            currentMonth = Month.JANUARY
                                                            currentYear += 1
                                                        } else {
                                                            currentMonth = Month(currentMonth.number + 1)
                                                        }
                                                    }) {
                                                        Icon(
                                                            Icons.AutoMirrored.Filled.ArrowForward,
                                                            contentDescription = "Mois suivant"
                                                        )
                                                    }
                                                }

                                                Spacer(Modifier.height(4.dp))

                                                // --- Grille des jours ---
                                                val columns = 7
                                                for (weekStart in 1..daysInMonth step columns) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        for (day in weekStart until (weekStart + columns).coerceAtMost(
                                                            daysInMonth + 1
                                                        )) {
                                                            val thisDay = LocalDate(currentYear, currentMonth, day)
                                                            Text(
                                                                text = day.toString(),
                                                                modifier = Modifier
                                                                    .padding(4.dp)
                                                                    .clickable {
                                                                        selectedDate = thisDay
                                                                        date = thisDay.toString()
                                                                        showCalendar = false
                                                                    }
                                                                    .background(
                                                                        if (selectedDate == thisDay)
                                                                            MaterialTheme.colorScheme.primary
                                                                        else Color.Transparent,
                                                                        shape = CircleShape
                                                                    )
                                                                    .padding(8.dp),
                                                                color = if (selectedDate == thisDay)
                                                                    Color.White
                                                                else
                                                                    MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // 📝 Notes + bouton confirmer
                                Column {
                                    Text("Notes :", fontWeight = FontWeight.SemiBold)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        TextField(
                                            value = notes,
                                            onValueChange = { notes = it },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("Ajouter une note...") },
                                            maxLines = 3
                                        )
                                        Button(
                                            onClick = {
                                                val parsedDate = try {
                                                    LocalDate.parse(date)
                                                } catch (e: Exception) {
                                                    null
                                                }

                                                // Use the selected course (assignedCourse) instead of free-text assignedTo
                                                if (parsedDate != null && assignedCourse != null && notes.isNotBlank()) {
                                                    val text = "Assigné à: ${assignedCourse?.name ?: ""} — Note: $notes"

                                                    val updatedList = events[parsedDate]?.toMutableList() ?: mutableListOf()
                                                    updatedList.add(text)

                                                    events = events.toMutableMap().apply {
                                                        put(parsedDate, updatedList)
                                                    }
                                                }

                                                showAddEventPanel = false
                                                assignedCourse = null
                                                date = ""
                                                notes = ""
                                            }
                                        ) {
                                            Text("Confirmer")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


                Spacer(Modifier.height(12.dp))

                // --- Liste des événements du jour sélectionné ---
                if (dialogDate != null) {
                    val items = events[dialogDate] ?: emptyList()
                    Column(modifier = Modifier.fillMaxWidth()) {

                        val header = "${dialogDate!!.year}-${
                            dialogDate!!.month.number.toString().padStart(2, '0')
                        }-${dialogDate!!.day.toString().padStart(2, '0')}"

                        Text(
                            text = "Items on $header",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        if (items.isEmpty()) {
                            Text(
                                "No items",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items.forEach { t ->

                                    // On découpe "Assigné à: X — Note: Y"
                                    val parts = t.split(" — ")
                                    val assigned = parts.getOrNull(0)?.removePrefix("Assigné à: ") ?: ""
                                    val note = parts.getOrNull(1)?.removePrefix("Note: ") ?: ""

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                shape = MaterialTheme.shapes.small
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

                                            Text(
                                                text = "Assigné à :",
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(assigned)

                                            Text(
                                                text = "Note :",
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(note)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Autres fonctions inchangées ---
enum class CalendarMode {
    Week, Month;

    fun next(date: LocalDate): LocalDate = when (this) {
        Week -> date.plus(7, DateTimeUnit.DAY)
        Month -> date.plus(1, DateTimeUnit.MONTH)
    }

    fun prev(date: LocalDate): LocalDate = when (this) {
        Week -> date.minus(7, DateTimeUnit.DAY)
        Month -> date.minus(1, DateTimeUnit.MONTH)
    }
}

@Composable
private fun WeekRow(
    anchorDate: LocalDate,
    today: LocalDate,
    scheduledByDate: Map<LocalDate, List<String>>,
    onDateClick: (LocalDate) -> Unit
) {
    val start = anchorDate.startOfWeek()
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        for (i in 0 until 7) {
            val d = start.plus(i, DateTimeUnit.DAY)
            val has = scheduledByDate.containsKey(d)
            Box(modifier = Modifier.weight(1f)) {
                DayCell(
                    date = d,
                    isToday = d == today,
                    isOtherMonth = false,
                    hasItems = has,
                    onClick = { onDateClick(d) }
                )
            }
        }
    }
}

@Composable
private fun MonthGrid(
    anchorDate: LocalDate,
    today: LocalDate,
    scheduledByDate: Map<LocalDate, List<String>>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstOfMonth = LocalDate(anchorDate.year, anchorDate.month, 1)
    val start = firstOfMonth.startOfWeek()
    Column(modifier = Modifier.fillMaxWidth()) {
        for (row in 0 until 6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 0 until 7) {
                    val idx = row * 7 + col
                    val date = start.plus(idx, DateTimeUnit.DAY)
                    val isOther = date.month != anchorDate.month
                    Box(modifier = Modifier.weight(1f)) {
                        val has = scheduledByDate.containsKey(date)
                        DayCell(
                            date = date,
                            isToday = date == today,
                            isOtherMonth = isOther,
                            hasItems = has,
                            onClick = { onDateClick(date) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isToday: Boolean,
    isOtherMonth: Boolean,
    hasItems: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val backgroundColor =
        if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
    val textColor = when {
        isOtherMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    Column(
        modifier = Modifier
            .padding(2.dp)
            .background(backgroundColor)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = date.day.toString(), color = textColor)
        if (hasItems) {
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
            )
        }
    }
}

private fun LocalDate.startOfWeek(): LocalDate {
    val dayIndex = (this.dayOfWeek.isoDayNumber - 1)
    return this.minus(dayIndex, DateTimeUnit.DAY)
}

// --- Prévisualisations ---
@Preview
@Composable
private fun Preview_Calendar_Month() {
    CalendarScreen(initialMode = CalendarMode.Month, initialAnchorDate = LocalDate(2025, 9, 15))
}
