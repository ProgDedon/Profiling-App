package com.example.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.unit.dp
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "greymmc - Executive Dossier Studio (A4 Landscape)",
        state = WindowState(width = 1280.dp, height = 820.dp)
    ) {
        window.minimumSize = Dimension(900, 600)
        DesktopAppUI()
    }
}
