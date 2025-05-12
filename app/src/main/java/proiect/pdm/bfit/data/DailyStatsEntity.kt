package proiect.pdm.bfit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey val date: String, // Format YYYY-MM-DD
    val steps: Int,
    val caloriesIntake: Int,
    val caloriesBurned: Int
)
