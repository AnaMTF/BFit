package proiect.pdm.bfit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // Format YYYY-MM-DD
    val steps: Int,
    val caloriesIntake: Int,
    val caloriesBurned: Int,
    val waterIntake: Int = 0,
    val workouts: Int = 0
)
