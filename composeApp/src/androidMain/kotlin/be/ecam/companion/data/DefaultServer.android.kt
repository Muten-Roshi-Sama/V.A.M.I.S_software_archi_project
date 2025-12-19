package be.ecam.companion.data

import be.ecam.common.SERVER_PORT

actual fun defaultServerBaseUrl(): String = "http://10.0.2.2:$SERVER_PORT"
