package com.autocare.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Alternative: Using system default SansSerif font family to avoid Google Fonts certificate issues
val PoppinsFontFamily = FontFamily.SansSerif

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
)
