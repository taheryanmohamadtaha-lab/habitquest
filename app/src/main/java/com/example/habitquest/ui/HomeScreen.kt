package com.example.habitquest.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habitquest.HabitUi
import com.example.habitquest.HabitViewModel
import com.example.habitquest.UiState
import com.example.habitquest.XP_PER_LEVEL

@Composable
fun HomeScreen(vm: HabitViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<HabitUi?>(null) }

    Scaffold(
        containerColor = Bg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = Accent,
                contentColor = Color.White,
                shape = CircleShape
            ) { Icon(Icons.Default.Add, contentDescription = "عادت جدید") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Header(state) }
            if (state.habits.isEmpty()) {
                item {
                    Text(
                        "هنوز عادتی نداری 🌱\nبا دکمه + اولین عادتت رو بساز و ماجراجویی رو شروع کن!",
                        color = Color.White.copy(alpha = .7f),
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp)
                    )
                }
            }
            items(state.habits, key = { it.habit.id }) { item ->
                HabitCard(item, onToggle = { vm.toggle(item) }, onLongPress = { toDelete = item })
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showAdd) {
        AddHabitDialog(
            onDismiss = { showAdd = false },
            onAdd = { name, emoji, color -> vm.add(name, emoji, color); showAdd = false }
        )
    }
    toDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("حذف عادت") },
            text = { Text("«${item.habit.name}» و تمام سابقه‌اش حذف بشه؟") },
            confirmButton = {
                TextButton(onClick = { vm.delete(item.habit); toDelete = null }) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("انصراف") } }
        )
    }
}

@Composable
private fun Header(state: UiState) {
    val title = LevelTitles[(state.level - 1).coerceAtMost(LevelTitles.lastIndex)]
    val progress by animateFloatAsState(state.xpInLevel / XP_PER_LEVEL.toFloat(), label = "xp")
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Accent, Accent2)))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("سطح ${state.level}", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(title, color = Color.White.copy(alpha = .85f), fontSize = 15.sp)
            }
            Text("⭐ ${state.xp} امتیاز", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(14.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
            color = Color.White,
            trackColor = Color.White.copy(alpha = .3f)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "${XP_PER_LEVEL - state.xpInLevel} امتیاز تا سطح بعدی",
            color = Color.White.copy(alpha = .85f), fontSize = 12.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            if (state.allDone) "🎉 همه عادت‌های امروز انجام شد!"
            else "امروز: ${state.doneCount} از ${state.habits.size}",
            color = Color.White, fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitCard(item: HabitUi, onToggle: () -> Unit, onLongPress: () -> Unit) {
    val color = HabitColors[item.habit.colorIndex % HabitColors.size]
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        if (item.doneToday) 1.15f else 1f,
        spring(Spring.DampingRatioMediumBouncy), label = "scale"
    )
    val fill by animateColorAsState(if (item.doneToday) color else Color.Transparent, label = "fill")

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardBg)
            .combinedClickable(onClick = {}, onLongClick = onLongPress)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(CircleShape).background(color.copy(alpha = .2f)),
                contentAlignment = Alignment.Center
            ) { Text(item.habit.emoji, fontSize = 26.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.habit.name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (item.streak > 0) "🔥 ${item.streak} روز پشت‌سرهم" else "امروز شروعش کن",
                    color = Color.White.copy(alpha = .65f), fontSize = 13.sp
                )
            }
            Box(
                Modifier
                    .size(48.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(fill)
                    .border(2.dp, color, CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (item.doneToday) Icon(Icons.Default.Check, null, tint = Color.White)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            item.week.forEach { done ->
                Box(
                    Modifier.size(12.dp).clip(CircleShape)
                        .background(if (done) color else color.copy(alpha = .2f))
                )
            }
        }
    }
}

@Composable
private fun AddHabitDialog(onDismiss: () -> Unit, onAdd: (String, String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(HabitEmojis.first()) }
    var colorIndex by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("عادت جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("اسم عادت، مثلاً «نوشیدن آب»") },
                    singleLine = true
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(HabitEmojis) { e ->
                        Box(
                            Modifier.size(44.dp).clip(CircleShape)
                                .background(if (e == emoji) Accent.copy(alpha = .4f) else Color.Transparent)
                                .clickable { emoji = e },
                            contentAlignment = Alignment.Center
                        ) { Text(e, fontSize = 22.sp) }
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(HabitColors.size) { i ->
                        Box(
                            Modifier.size(34.dp).clip(CircleShape).background(HabitColors[i])
                                .border(if (i == colorIndex) 3.dp else 0.dp, Color.White, CircleShape)
                                .clickable { colorIndex = i }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(name, emoji, colorIndex) }, enabled = name.isNotBlank()) {
                Text("افزودن")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
