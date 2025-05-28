package proiect.pdm.bfit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float
) 