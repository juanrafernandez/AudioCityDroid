package com.jrlabs.audiocity.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * AudioCity Design System - Colors
 * Basado en el Theme.swift de iOS
 * Color primario: Coral (#FF5757) - Transmite energía, aventura, calidez
 */

// MARK: - Brand Colors (Primary)

/// Color primario - Coral vibrante
/// Transmite: energía, aventura, emoción, calidez
/// Uso: CTAs principales, elementos destacados, iconos activos
val ACPrimary = Color(0xFFFF5757)
val ACPrimaryDark = Color(0xFFE04545)
val ACPrimaryLight = Color(0xFFFFE5E5)
val ACPrimarySurface = Color(0xFFFFF5F5)

// MARK: - Secondary Colors

/// Turquesa - Complementario
/// Transmite: frescura, confianza, tecnología
val ACSecondary = Color(0xFF00BFA6)
val ACSecondaryDark = Color(0xFF00A08A)
val ACSecondaryLight = Color(0xFFE0FBF7)

// MARK: - Accent Colors

/// Amarillo dorado - Puntos y recompensas
val ACGold = Color(0xFFFFB800)
val ACGoldLight = Color(0xFFFFF4CC)

/// Azul información
val ACInfo = Color(0xFF2196F3)
val ACInfoLight = Color(0xFFE3F2FD)

// MARK: - Semantic Colors

/// Verde éxito
val ACSuccess = Color(0xFF4CAF50)
val ACSuccessLight = Color(0xFFE8F5E9)

/// Naranja advertencia
val ACWarning = Color(0xFFFF9800)
val ACWarningLight = Color(0xFFFFF3E0)

/// Rojo error (diferente al primario para evitar confusión)
val ACError = Color(0xFFD32F2F)
val ACErrorLight = Color(0xFFFFEBEE)

// MARK: - Neutral Colors (Light Mode)

/// Fondo principal
val ACBackground = Color(0xFFFAFAFA)

/// Fondo de superficie (cards, sheets)
val ACSurface = Color.White

/// Texto primario - Casi negro para mejor legibilidad
val ACTextPrimary = Color(0xFF1A1A1A)

/// Texto secundario
val ACTextSecondary = Color(0xFF6B6B6B)

/// Texto terciario / placeholder
val ACTextTertiary = Color(0xFF9E9E9E)

/// Texto invertido (sobre fondos oscuros)
val ACTextInverted = Color.White

/// Bordes y divisores
val ACBorder = Color(0xFFE5E5E5)
val ACBorderLight = Color(0xFFF0F0F0)

/// Separadores sutiles
val ACDivider = Color(0xFFEEEEEE)

// MARK: - Dark Mode Colors

val ACBackgroundDark = Color(0xFF121212)
val ACSurfaceDark = Color(0xFF1E1E1E)
val ACSurfaceElevatedDark = Color(0xFF2C2C2C)
val ACTextPrimaryDark = Color(0xFFF5F5F5)
val ACTextSecondaryDark = Color(0xFFB0B0B0)
val ACTextTertiaryDark = Color(0xFF757575)
val ACBorderDark = Color(0xFF3D3D3D)
val ACDividerDark = Color(0xFF2D2D2D)
val ACPrimaryDarkMode = Color(0xFFFF6B6B)
val ACPrimarySurfaceDark = Color(0xFF2A1F1F)

// MARK: - Map Colors

val ACMapStopPin = Color(0xFFFF5757)
val ACMapStopVisited = Color(0xFF4CAF50)
val ACMapStopSelected = Color(0xFF2196F3)

// MARK: - Level Colors (Gamificación)

val ACLevelExplorer = Color(0xFF9E9E9E)    // Gris - Inicio
val ACLevelTraveler = Color(0xFF2196F3)    // Azul - Progreso
val ACLevelLocalGuide = Color(0xFF4CAF50)  // Verde - Intermedio
val ACLevelExpert = Color(0xFF9C27B0)      // Púrpura - Avanzado
val ACLevelMaster = Color(0xFFFF5757)      // Coral - Maestro

// MARK: - Legacy colors (para compatibilidad)
// Estos se mantendrán para evitar romper código existente

val BrandBlue = ACPrimary  // Ahora apunta a Coral
val BrandBlueDark = ACPrimaryDark
val BrandBlueLight = ACPrimaryLight

// Status Colors
val StopPending = ACWarning
val StopPlaying = ACPrimary
val StopVisited = ACSuccess

// Neutrals
val White = Color.White
val Black = Color.Black
val Gray100 = Color(0xFFF5F5F5)
val Gray200 = Color(0xFFEEEEEE)
val Gray300 = Color(0xFFE0E0E0)
val Gray500 = ACTextTertiary
val Gray700 = Color(0xFF616161)
val Gray900 = ACTextPrimary
