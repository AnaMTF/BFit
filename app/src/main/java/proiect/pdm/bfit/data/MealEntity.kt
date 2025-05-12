package proiect.pdm.bfit.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "meals",
    foreignKeys = [ForeignKey(
        entity = DailyStatsEntity::class,
        parentColumns = ["date"],
        childColumns = ["dayDate"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayDate: String,
    val mealType: String, // ex: "Mic dejun", "Pranz", "Cina"
    val description: String,
    val calories: Int
)
