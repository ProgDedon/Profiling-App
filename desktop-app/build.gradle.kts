plugins {
    kotlin("multiplatform") version "1.9.23"
    id("org.jetbrains.compose") version "1.6.2"
}

group = "com.aistudio.dossierpdf"
version = "1.0.0"

kotlin {
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                // SQLite for pure desktop JVM data persistence
                implementation("org.xerial:sqlite-jdbc:3.45.2.0")
                // Apache PDFBox for pure desktop JVM A4 Landscape PDF rendering
                implementation("org.apache.pdfbox:pdfbox:3.0.2")
                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.example.desktop.MainKt"
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi
            )
            packageName = "greymmc"
            packageVersion = "1.0.0"
            description = "greymmc Executive Dossier Studio - A4 Landscape PDF Workstation"
            vendor = "greymmc"
            copyright = "© 2026 greymmc. All rights reserved."

            windows {
                menu = true
                shortcut = true
                dirChooser = true
                perUserInstall = true
                menuGroup = "greymmc"
                upgradeUuid = "68c5b058-00a4-49c5-8422-777e49eb72c4"
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
            }
        }
    }
}
