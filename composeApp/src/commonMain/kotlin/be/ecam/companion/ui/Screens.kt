package be.ecam.companion.ui

sealed class Screen(val key: String) {
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object Settings : Screen("settings")
    object ListAdmins : Screen("admins")
//    Object SettingsScreen
}