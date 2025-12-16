package be.ecam.companion.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

object AppImages {
    const val ECAM_LOGO_URL: String =
        "https://www.ecam.be/wp-content/uploads/2023/12/logo_ECAM_entier_sansfond-2.png"

    const val ECAM_LOGIN_SIDE_URL: String =
        "https://www.cerdecam.be/image/ecam.jpg"

    const val ECAM_LOGIN_BACKGROUND_URL: String =
        "https://www.kotplanet.be/wp-content/uploads/2023/03/2022-09-12-ECAM_A.-Delsoir-Photo_4874Web-1024x683.jpg"
}

@Composable
fun EcamLogo(
    modifier: Modifier = Modifier,
    contentDescription: String? = "ECAM logo",
    contentScale: ContentScale = ContentScale.Fit,
) {
    RemoteImage(
        url = AppImages.ECAM_LOGO_URL,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    )
}

@Composable
expect fun RemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
)
