//package be.ecam.companion.ui
//
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.CalendarMonth
//import androidx.compose.material.icons.filled.Home
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material3.Icon
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.Modifier
//
//@Composable
//fun AppBottomBar(selected: Any /* Screen or BottomItem */, onSelect: (Any) -> Unit) {
//    NavigationBar {
//        // We support two kinds of "selected" tokens: the old BottomItem enum or the new Screen sealed class.
//        val items = listOf(
//            Pair("home", Icons.Filled.Home),
//            Pair("calendar", Icons.Filled.CalendarMonth),
//            Pair("settings", Icons.Filled.Settings)
//        )
//
//        items.forEach { (key, icon) ->
//            NavigationBarItem(
//                selected = when (selected) {
//                    is Screen -> selected.key == key
//                    is BottomItem -> selected.name.lowercase() == key
//                    else -> false
//                },
//                onClick = {
//                    // Try to call back with the same type as selected
//                    when (selected) {
//                        is Screen -> {
//                            val target = when (key) {
//                                "home" -> Screen.Home
//                                "calendar" -> Screen.Calendar
//                                "settings" -> Screen.Settings
//                                else -> Screen.Home
//                            }
//                            onSelect(target)
//                        }
//                        is BottomItem -> {
//                            val target = when (key) {
//                                "home" -> BottomItem.HOME
//                                "calendar" -> BottomItem.CALENDAR
//                                "settings" -> BottomItem.SETTINGS
//                                else -> BottomItem.HOME
//                            }
//                            onSelect(target)
//                        }
//                        else -> {
//                            // fallback - return a key string
//                            onSelect(key)
//                        }
//                    }
//                },
//                icon = { Icon(icon, contentDescription = key) },
//                label = { Text(key.replaceFirstChar { it.uppercase() }) }
//            )
//        }
//    }
//}