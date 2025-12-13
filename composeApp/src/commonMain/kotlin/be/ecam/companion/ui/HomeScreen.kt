package be.ecam.companion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import be.ecam.companion.viewmodel.HomeViewModel
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenAdmins: () -> Unit,
    onOpenStudents: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTeachers: () -> Unit,
    onOpenBible: () -> Unit
) {
    val vm = koinInject<HomeViewModel>()
    LaunchedEffect(Unit) { vm.load() }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Drawer partiel avec fond lumineux et scrim sombre
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

                DrawerItem("Calendar", Icons.Filled.CalendarMonth) {
                    scope.launch { drawerState.close() }
                    onOpenCalendar()
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Open menu")
            }

            Spacer(Modifier.height(16.dp))

            Text("Home", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            if (vm.lastErrorMessage.isNotEmpty()) {
                Text(vm.lastErrorMessage, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(onClick = onOpenAdmins) {
                Text("Open Admins List")
            }

            Spacer(Modifier.height(12.dp))

            Button(onClick = onOpenStudents) {
                Text("See all student reports")
            }

            Spacer(Modifier.height(12.dp))

            Button(onClick = onOpenTeachers) {
                Text("Voir les enseignants")
            }

            Spacer(Modifier.height(12.dp))

            Button(onClick = onOpenBible) {
                Text("\ud83d\udcd2 Bible des Programmes")
            }

            Spacer(Modifier.height(20.dp))

            Text(vm.helloMessage)
        }
    }
}

@Composable
fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null // tu peux ajouter une ripple si tu veux
            ) {
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
