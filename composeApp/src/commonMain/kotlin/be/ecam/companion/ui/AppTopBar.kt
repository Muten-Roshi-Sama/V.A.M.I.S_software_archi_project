//package be.ecam.companion.ui
//
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Menu
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.foundation.layout.height
//import androidx.compose.ui.unit.dp
//
//@Composable
//fun AppTopBar(title: String, onMenuClick: () -> Unit) {
//    TopAppBar(
//        title = { Text(title) },
//        navigationIcon = {
//            IconButton(onClick = onMenuClick) {
//                Icon(Icons.Filled.Menu, contentDescription = "Open drawer")
//            }
//        },
//        modifier = Modifier.height(56.dp)
//    )
//}