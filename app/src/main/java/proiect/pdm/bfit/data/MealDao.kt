package proiect.pdm.bfit.data

import androidx.room.*

@Dao
interface MealDao {
    @Insert
    suspend fun insert(meal: MealEntity)

    @Query("SELECT * FROM meals WHERE dayDate = :date")
    suspend fun getMealsByDate(date: String): List<MealEntity>
}
