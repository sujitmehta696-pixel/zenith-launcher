package com.example.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.ProBadge
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LocalLauncherTokens
import com.example.ui.theme.PremiumGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class AuthMode(val label: String) {
    SIGN_IN("Login"),
    REGISTER("Register")
}

@Composable
fun AuthScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onGoogleAuthSubmit: (mode: AuthMode, displayName: String, handle: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalLauncherTokens.current
    val density = LocalDensity.current
    val isRobolectric = remember { Build.FINGERPRINT.contains("robolectric", ignoreCase = true) }
    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var displayName by remember { mutableStateOf("") }
    var studioHandle by remember { mutableStateOf("") }
    var legalDialogType by remember { mutableStateOf<String?>(null) }

    val transition = rememberInfiniteTransition(label = "auth3dOrbit")
    val orbitDeg by if (isRobolectric || tokens.reduceMotion) {
        remember { mutableStateOf(25f) }
    } else {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "orbitDeg"
        )
    }

    val cardBreath by if (isRobolectric || tokens.reduceMotion) {
        remember { mutableStateOf(0f) }
    } else {
        transition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cardBreath"
        )
    }

    val neonBlue = Color(0xFF168BFF)
    val royalPurple = Color(0xFF713BFF)
    val softViolet = Color(0xFF9D50FF)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080B18)),
        contentAlignment = Alignment.Center
    ) {
        // Luxury Architectural Backdrop
        Image(
            painter = painterResource(id = R.drawable.img_auth_backdrop),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.42f)
        )

        // Atmospheric 3D horizon glow + deep midnight vignette
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(royalPurple.copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(size.width * 0.22f, size.height * 0.16f),
                    radius = size.maxDimension * 0.55f
                ),
                radius = size.maxDimension * 0.55f,
                center = Offset(size.width * 0.22f, size.height * 0.16f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ElectricCyan.copy(alpha = 0.20f), Color.Transparent),
                    center = Offset(size.width * 0.84f, size.height * 0.72f),
                    radius = size.maxDimension * 0.50f
                ),
                radius = size.maxDimension * 0.50f,
                center = Offset(size.width * 0.84f, size.height * 0.72f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 3D Gyroscopic Orbital Crest
            Box(
                modifier = Modifier
                    .size(134.dp)
                    .graphicsLayer {
                        rotationX = 6f + cardBreath * 3.5f
                        rotationY = cardBreath * 6f
                        cameraDistance = 14f * density.density
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val outerR = size.minDimension * 0.46f

                    // Deep floor shadow oval
                    drawOval(
                        brush = Brush.radialGradient(
                            listOf(ElectricCyan.copy(alpha = 0.25f), royalPurple.copy(alpha = 0.12f), Color.Transparent),
                            center = Offset(c.x, c.y + outerR * 0.92f),
                            radius = outerR * 0.95f
                        ),
                        topLeft = Offset(c.x - outerR * 0.75f, c.y + outerR * 0.72f),
                        size = Size(outerR * 1.50f, outerR * 0.36f)
                    )

                    // Primary 3D swept ring
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(ElectricCyan, neonBlue, royalPurple, Color.Transparent, PremiumGold, ElectricCyan)
                        ),
                        startAngle = orbitDeg - 45f,
                        sweepAngle = 290f,
                        useCenter = false,
                        topLeft = Offset(c.x - outerR, c.y - outerR),
                        size = Size(outerR * 2f, outerR * 2f),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Tilted 3D elliptical orbit plane
                    drawOval(
                        brush = Brush.linearGradient(listOf(softViolet, ElectricCyan)),
                        topLeft = Offset(c.x - outerR * 0.96f, c.y - outerR * 0.48f),
                        size = Size(outerR * 1.92f, outerR * 0.96f),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Counter-tilted 3D satellite node
                    val rad = (orbitDeg * PI.toFloat()) / 180f
                    val sx = c.x + cos(rad) * (outerR * 0.96f)
                    val sy = c.y + sin(rad) * (outerR * 0.48f)
                    drawCircle(color = ElectricCyan.copy(alpha = 0.40f), radius = 7.dp.toPx(), center = Offset(sx, sy))
                    drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(sx, sy))
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF070A12))
                        .border(
                            1.8.dp,
                            Brush.linearGradient(listOf(ElectricCyan, softViolet, PremiumGold)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_splash_crest),
                        contentDescription = "Android 3D Rolling launcher OS Emblem",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
            }

            // Title & Value Proposition
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ProBadge(label = "3D OS CLOUD VAULT")
                Text(
                    text = "Android 3D Rolling launcher OS",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "3D Rolling Apps • Gesture OS • Cloud Sync",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB8C4DC),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 360.dp)
                )
            }

            // Sleek 3-Pillar Highlight Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureChipPill(
                    icon = Icons.Rounded.VerifiedUser,
                    label = "Pro Sync",
                    tint = PremiumGold,
                    modifier = Modifier.weight(1f)
                )
                FeatureChipPill(
                    icon = Icons.Rounded.Palette,
                    label = "Full Theme",
                    tint = ElectricCyan,
                    modifier = Modifier.weight(1f)
                )
                FeatureChipPill(
                    icon = Icons.Rounded.Gesture,
                    label = "Gestures",
                    tint = softViolet,
                    modifier = Modifier.weight(1f)
                )
            }

            // Segmented Login / Register Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xCC0F1627))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AuthMode.entries.forEach { mode ->
                    val selected = authMode == mode
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selected) {
                                    Brush.horizontalGradient(
                                        listOf(
                                            royalPurple.copy(alpha = 0.42f),
                                            ElectricCyan.copy(alpha = 0.28f)
                                        )
                                    )
                                } else {
                                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .border(
                                width = if (selected) 1.dp else 0.dp,
                                color = if (selected) ElectricCyan.copy(alpha = 0.72f) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { authMode = mode }
                            .padding(vertical = 12.dp)
                            .testTag("auth_mode_${mode.name}"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (mode == AuthMode.SIGN_IN) Icons.AutoMirrored.Rounded.Login else Icons.Rounded.PersonAdd,
                            contentDescription = null,
                            tint = if (selected) ElectricCyan else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (selected) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // 3D Frosted Depth Login Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationX = cardBreath * 1.4f
                        rotationY = -cardBreath * 1.6f
                        cameraDistance = 20f * density.density
                        shadowElevation = 18f * density.density
                    }
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xEB131B32),
                                Color(0xEB0A0F1F)
                            )
                        )
                    )
                    .border(
                        width = 1.4.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                ElectricCyan.copy(alpha = 0.65f),
                                royalPurple.copy(alpha = 0.45f),
                                PremiumGold.copy(alpha = 0.55f)
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (authMode == AuthMode.SIGN_IN) {
                                "Welcome Back to 3D OS"
                            } else {
                                "Create Your 3D OS ID"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = if (authMode == AuthMode.SIGN_IN) {
                                "Restore your 3D presets, themes & Pro unlocks"
                            } else {
                                "Personalize your Cloud Vault before linking Google"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(visible = authMode == AuthMode.REGISTER) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val fieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.20f),
                            focusedLabelColor = ElectricCyan,
                            unfocusedLabelColor = Color(0xFF94A3B8),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLeadingIconColor = ElectricCyan,
                            unfocusedLeadingIconColor = softViolet
                        )
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it.take(60) },
                            label = { Text("Display Name") },
                            placeholder = { Text("e.g. Aarav Studio") },
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_register_name_input")
                        )
                        OutlinedTextField(
                            value = studioHandle,
                            onValueChange = { studioHandle = it.take(32) },
                            label = { Text("Studio Handle (optional)") },
                            placeholder = { Text("@aarav_3dos") },
                            leadingIcon = { Icon(Icons.Rounded.Badge, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_register_handle_input")
                        )
                    }
                }

                // Pro Vault & Sync Promise Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x990A0F1A))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccountBenefitRow(
                        icon = Icons.Rounded.VerifiedUser,
                        tint = PremiumGold,
                        text = "Pro unlocks sync automatically across devices"
                    )
                    AccountBenefitRow(
                        icon = Icons.Rounded.CloudDone,
                        tint = ElectricCyan,
                        text = "Cloud backup for themes & 3D presets"
                    )
                    AccountBenefitRow(
                        icon = Icons.Rounded.Gesture,
                        tint = softViolet,
                        text = "Restores gesture mappings on login"
                    )
                }

                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.16f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                }

                // 3D Luminous Gradient CTA
                Button(
                    onClick = { onGoogleAuthSubmit(authMode, displayName, studioHandle) },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = Color(0xFF040914),
                        disabledContainerColor = ElectricCyan.copy(alpha = 0.55f),
                        disabledContentColor = Color(0xFF040914)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("auth_google_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color(0xFF040914),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (authMode == AuthMode.SIGN_IN) Icons.AutoMirrored.Rounded.Login else Icons.Rounded.PersonAdd,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (authMode == AuthMode.SIGN_IN) {
                                "Sign in with Google"
                            } else {
                                "Register with Google"
                            },
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Secured via Android Credential Manager",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Pre-auth Legal Footer Links
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricCyan,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { legalDialogType = "privacy" }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("auth_privacy_link")
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF475569)
                )
                Text(
                    text = "Terms of Use",
                    style = MaterialTheme.typography.labelMedium,
                    color = PremiumGold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { legalDialogType = "terms" }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("auth_terms_link")
                )
            }
        }
    }

    legalDialogType?.let { docType ->
        AlertDialog(
            onDismissRequest = { legalDialogType = null },
            modifier = Modifier.testTag("auth_legal_dialog"),
            title = {
                Text(if (docType == "privacy") "Privacy Policy Overview" else "Terms & Conditions Overview")
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = if (docType == "privacy") {
                            "Android 3D Rolling launcher OS uses Google Sign-In via Credential Manager to sync your active theme, widget & icon settings, gesture mappings, and Pro entitlements in Cloud Firestore. Installed app lists remain strictly on-device and are never uploaded or sold."
                        } else {
                            "Free access includes 50 studio wallpapers, 50 dynamic themes, full-ecosystem widget & icon styling, gesture navigation, and the 3D All-Apps Rolling launcher. Pro unlocks tie to your Cloud Account for instant restoration on any login."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { legalDialogType = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun FeatureChipPill(
    icon: ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xB30F1627))
            .border(1.dp, tint.copy(alpha = 0.36f), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp
            ),
            color = Color(0xFFE2E8F0),
            maxLines = 1
        )
    }
}

@Composable
private fun AccountBenefitRow(
    icon: ImageVector,
    tint: Color,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(17.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFCBD5E1)
        )
    }
}
