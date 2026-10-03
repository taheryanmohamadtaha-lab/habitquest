package com.example.habitquest.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Bg = Color(0xFF14121F)
val CardBg = Color(0xFF1F1C30)
val Accent = Color(0xFF8B5CF6)
val Accent2 = Color(0xFFEC4899)

val HabitColors = listOf(
    Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF22C55E),
    Color(0xFFF59E0B), Color(0xFF06B6D4), Color(0xFFEF4444)
)

val HabitEmojis = listOf("💧", "📚", "🏃", "🧘", "💪", "🥗", "😴", "✍️", "🎯", "🎸", "🧹", "🙏")

val LevelTitles = listOf("تازه‌کار", "جوینده", "کوشا", "پرتلاش", "قهرمان", "استاد", "افسانه")

private val Scheme = darkColorScheme(
    primary = Accent,
    background = Bg,
    surface = CardBg,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun HabitQuestTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
