package com.example.habitquest.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id")
    fun habits(): Flow<List<Habit>>

    @Query("SELECT * FROM completions")
    fun completions(): Flow<List<Completion>>

    @Insert
    suspend fun insertHabit(habit: Habit): Long

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(c: Completion)

    @Query("DELETE FROM completions WHERE habitId = :habitId AND epochDay = :day")
    suspend fun deleteCompletion(habitId: Long, day: Long)
}

@Database(entities = [Habit::class, Completion::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun dao(): HabitDao

    companion object {
        @Volatile private var instance: AppDb? = null
        fun get(context: Context): AppDb = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, AppDb::class.java, "habitquest.db"
            ).build().also { instance = it }
        }
    }
}
