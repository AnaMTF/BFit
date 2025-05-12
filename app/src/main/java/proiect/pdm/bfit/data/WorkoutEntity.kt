package proiect.pdm.bfit.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "workouts",
    foreignKeys = [ForeignKey(
        entity = DailyStatsEntity::class,
        parentColumns = ["date"],
        childColumns = ["dayDate"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayDate: String,
    val name: String,
    val durationMinutes: Int,
    val caloriesBurned: Int
)
