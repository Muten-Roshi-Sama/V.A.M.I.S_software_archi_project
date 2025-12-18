package be.ecam.companion.ui

sealed class Screen(val key: String) {
    object Login : Screen("Login")
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object Settings : Screen("settings")
    object ListAdmins : Screen("admins")
    object DataStudents : Screen("dataStudents")
    object Teachers : Screen("teachers")
    object Bible : Screen("bible")
    object Grades : Screen("grades")
    object IspList : Screen("isp_list")
    object IspEdit : Screen("isp_edit")

}