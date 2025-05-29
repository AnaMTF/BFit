package proiect.pdm.bfit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayDate: String,       // format: "yyyy-MM-dd"
    val userId: Long,          // ID-ul utilizatorului
    val mealType: String,      // ex: "Breakfast", "Lunch", "Dinner"
    val name: String,          // ex: "Oatmeal", "Chicken Breast"
    val quantity: Float,
    val unit: String,           // ex: "g" sau "oz"
    val calories: Int = 0
)
