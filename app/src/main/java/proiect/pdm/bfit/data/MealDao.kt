package proiect.pdm.bfit.data

import androidx.room.*

@Dao
interface MealDao {
    @Insert
    suspend fun insert(meal: MealEntity)

    @Query("SELECT * FROM meals WHERE dayDate = :date AND userId = :userId")
    suspend fun getMealsByDate(date: String, userId: Long): List<MealEntity>

    @Delete
    suspend fun deleteMeal(meal: MealEntity)
}
