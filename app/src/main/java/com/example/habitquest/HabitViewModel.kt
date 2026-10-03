package com.example.habitquest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitquest.data.AppDb
import com.example.habitquest.data.Completion
import com.example.habitquest.data.Habit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitUi(
    val habit: Habit,
    val doneToday: Boolean,
    val streak: Int,
    val week: List<Boolean>
)

data class UiState(
    val habits: List<HabitUi> = emptyList(),
    val xp: Int = 0,
    val level: Int = 1,
    val xpInLevel: Int = 0,
    val doneCount: Int = 0
) {
    val allDone get() = habits.isNotEmpty() && doneCount == habits.size
}

const val XP_PER_COMPLETION = 10
const val XP_PER_LEVEL = 100

class HabitViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDb.get(app).dao()

    val state: StateFlow<UiState> = combine(dao.habits(), dao.completions()) { habits, comps ->
        val today = LocalDate.now().toEpochDay()
        val byHabit = comps.groupBy({ it.habitId }, { it.epochDay }).mapValues { it.value.toSet() }
        val items = habits.map { h ->
            val days = byHabit[h.id].orEmpty()
            HabitUi(
                habit = h,
                doneToday = today in days,
                streak = streak(days, today),
                week = (6 downTo 0).map { (today - it) in days }
            )
        }
        val xp = comps.size * XP_PER_COMPLETION
        UiState(
            habits = items,
            xp = xp,
            level = xp / XP_PER_LEVEL + 1,
            xpInLevel = xp % XP_PER_LEVEL,
            doneCount = items.count { it.doneToday }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    private fun streak(days: Set<Long>, today: Long): Int {
        var d = if (today in days) today else today - 1
        var c = 0
        while (d in days) { c++; d-- }
        return c
    }

    fun toggle(item: HabitUi) = viewModelScope.launch {
        val today = LocalDate.now().toEpochDay()
        if (item.doneToday) dao.deleteCompletion(item.habit.id, today)
        else dao.insertCompletion(Completion(habitId = item.habit.id, epochDay = today))
    }

    fun add(name: String, emoji: String, colorIndex: Int) = viewModelScope.launch {
        dao.insertHabit(Habit(name = name.trim(), emoji = emoji, colorIndex = colorIndex))
    }

    fun delete(habit: Habit) = viewModelScope.launch { dao.deleteHabit(habit) }
}
