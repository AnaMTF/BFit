package proiect.pdm.bfit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayDate: String,            // format de tip "yyyy-MM-dd"
    val name: String,               // ex: "Cardio", "Yoga"
    val durationMinutes: Int,       // durata în minute
    val caloriesBurned: Int,        // calorii arse
    val userId: Long                // ID-ul utilizatorului
)
