package be.ecam.companion.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

@Composable
actual fun RemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
) {
    // Fallback placeholder for Wasm/JS for now.
    Icon(
        imageVector = Icons.Filled.AccountCircle,
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
