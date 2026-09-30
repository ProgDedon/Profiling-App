package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DossierProfile
import com.example.pdf.PdfRendererHelper
import com.example.pdf.PdfViewerHelper
import com.example.ui.components.DossierDocumentPreview
import java.io.File

@Composable
fun PreviewScreen(
    profile: DossierProfile,
    isGenerating: Boolean,
    lastPdfFile: File?,
    onGenerateAndOpen: () -> Unit,
    onSharePdf: () -> Unit,
    onSaveToDownloads: () -> Unit,
    onPrintPdf: () -> Unit,
    onNavigateBackToForm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vScrollState = rememberScrollState()
    val hScrollState = rememberScrollState()

    var showInAppViewer by remember { mutableStateOf(false) }
    var renderedPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var selectedPageIndex by remember { mutableIntStateOf(0) }
    var activeTab by remember { mutableIntStateOf(0) } // 0: Live Form Preview, 1: Rendered PDF Document

    // Load rendered pages whenever lastPdfFile changes
    LaunchedEffect(lastPdfFile) {
        if (lastPdfFile != null && lastPdfFile.exists()) {
            renderedPages = PdfRendererHelper.renderPdfPages(lastPdfFile, 1200)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE9EDF2))
    ) {
        // Top Action Header Bar
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBackToForm,
                            modifier = Modifier.testTag("preview_back_button")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back to Form")
                        }
                        Text(
                            text = "Document Preview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateBackToForm,
                        modifier = Modifier.testTag("edit_form_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Form", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Primary PDF Export Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (lastPdfFile != null && lastPdfFile.exists()) {
                                val opened = PdfViewerHelper.openPdf(context, lastPdfFile)
                                if (!opened) {
                                    showInAppViewer = true
                                }
                            } else {
                                onGenerateAndOpen()
                            }
                        },
                        enabled = !isGenerating,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("open_pdf_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generating...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open PDF", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = onSharePdf,
                        enabled = !isGenerating,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.testTag("share_pdf_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share PDF", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onSaveToDownloads,
                        enabled = !isGenerating,
                        modifier = Modifier.testTag("save_downloads_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Downloads", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onPrintPdf,
                        enabled = !isGenerating,
                        modifier = Modifier.testTag("print_pdf_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print", fontSize = 12.sp)
                    }

                    if (renderedPages.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { showInAppViewer = true },
                            modifier = Modifier.testTag("view_pages_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Pages (${renderedPages.size})", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Preview Mode Tabs
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Live Dossier Sheet") }
            )
            Tab(
                selected = activeTab == 1,
                onClick = {
                    activeTab = 1
                    if (renderedPages.isEmpty() && lastPdfFile == null && !isGenerating) {
                        onGenerateAndOpen()
                    }
                },
                text = {
                    Text(if (renderedPages.isNotEmpty()) "Generated PDF (${renderedPages.size} Page${if (renderedPages.size > 1) "s" else ""})" else "Generated PDF")
                }
            )
        }

        // Main Document Canvas Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(vScrollState)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (activeTab == 0) {
                    // Live Vector Compose Preview
                    Box(
                        modifier = Modifier
                            .widthIn(min = 340.dp, max = 760.dp)
                            .horizontalScroll(hScrollState)
                    ) {
                        DossierDocumentPreview(
                            profile = profile,
                            modifier = Modifier.width(640.dp)
                        )
                    }
                } else {
                    // Actual Rendered PDF Pages
                    if (renderedPages.isNotEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.widthIn(max = 760.dp)
                        ) {
                            if (renderedPages.size > 1) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    renderedPages.indices.forEach { index ->
                                        FilterChip(
                                            selected = selectedPageIndex == index,
                                            onClick = { selectedPageIndex = index },
                                            label = {
                                                Text(if (index == 0) "Page 1: Dossier" else "Page 2: Form Scan")
                                            }
                                        )
                                    }
                                }
                            }

                            Card(
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                            ) {
                                val pageBitmap = renderedPages.getOrNull(selectedPageIndex) ?: renderedPages.first()
                                Image(
                                    bitmap = pageBitmap.asImageBitmap(),
                                    contentDescription = "PDF Page ${selectedPageIndex + 1}",
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                        }
                    } else if (isGenerating) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Rendering PDF pages...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Button(onClick = onGenerateAndOpen) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate PDF Preview")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // In-App Full-Screen PDF Viewer Modal Dialog
    if (showInAppViewer && renderedPages.isNotEmpty()) {
        Dialog(
            onDismissRequest = { showInAppViewer = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xF0101827)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Modal Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PDF Document Viewer (${renderedPages.size} Page${if (renderedPages.size > 1) "s" else ""})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        IconButton(onClick = { showInAppViewer = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    // Pages container
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            renderedPages.forEachIndexed { idx, bmp ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Page ${idx + 1}: Profile Dossier",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Card(
                                        shape = RoundedCornerShape(2.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "PDF Page ${idx + 1}",
                                            modifier = Modifier.fillMaxWidth(),
                                            contentScale = ContentScale.FillWidth
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Modal Bottom Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onSharePdf,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share PDF")
                        }
                    }
                }
            }
        }
    }
}
