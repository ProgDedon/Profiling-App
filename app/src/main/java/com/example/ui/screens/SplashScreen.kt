package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BookmanFontFamily
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "splashAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2200)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("splash_screen")
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF102E56),
                        Color(0xFF081225)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alphaAnim)
        ) {
            // Brand Monogram Badge
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0x33FFFFFF),
                border = BorderStroke(1.5.dp, Color(0x66FFFFFF)),
                modifier = Modifier
                    .size(96.dp)
                    .testTag("splash_logo_badge")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "MMC",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = BookmanFontFamily,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "greymmc",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BookmanFontFamily,
                letterSpacing = 1.2.sp
            )

            Text(
                text = "Profile Dossier & Document Management",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Prominent Announcement Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x2238BDF8),
                border = BorderStroke(1.dp, Color(0x8038BDF8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .testTag("powered_by_card")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Contact Phone",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Powered by Grey Communication @ 08101379193",
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                color = Color(0xFF38BDF8),
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            TextButton(
                onClick = onSplashFinished,
                modifier = Modifier.testTag("splash_skip_button"),
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF94A3B8))
            ) {
                Text("Tap to continue", fontSize = 12.sp)
            }
        }
    }
}
