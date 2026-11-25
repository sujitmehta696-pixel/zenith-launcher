package com.example.admin.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val AdminMidnight = Color(0xFF080B18)
val AdminSurface = Color(0xFF10152B)
val AdminSurfaceElevated = Color(0xFF181F3D)
val AdminRoyalPurple = Color(0xFF713BFF)
val AdminSoftViolet = Color(0xFF9D50FF)
val AdminElectricBlue = Color(0xFF168BFF)
val AdminNeonCyan = Color(0xFF00E5FF)
val AdminSuccess = Color(0xFF2CE59B)
val AdminWarning = Color(0xFFFFB74D)
val AdminDanger = Color(0xFFFF5C7A)
val AdminTextPrimary = Color(0xFFF6F7FC)
val AdminTextSecondary = Color(0xFFA9B2D0)
val AdminBorderSubtle = Color(0xFF28315A)

private val AdminDarkColorScheme = darkColorScheme(
    primary = AdminRoyalPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2A1D5E),
    onPrimaryContainer = Color(0xFFE7DEFF),
    secondary = AdminElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF12325C),
    onSecondaryContainer = Color(0xFFD6E9FF),
    tertiary = AdminNeonCyan,
    onTertiary = AdminMidnight,
    background = AdminMidnight,
    onBackground = AdminTextPrimary,
    surface = AdminSurface,
    onSurface = AdminTextPrimary,
    surfaceVariant = AdminSurfaceElevated,
    onSurfaceVariant = AdminTextSecondary,
    outline = AdminBorderSubtle,
    error = AdminDanger,
    onError = Color.White
)

private val AdminTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.3).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

private val AdminShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun AdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AdminDarkColorScheme,
        typography = AdminTypography,
        shapes = AdminShapes,
        content = content
    )
}
