package proiect.pdm.bfit.data

import androidx.room.*

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(food: FoodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foods: List<FoodEntity>)

    @Query("SELECT * FROM foods WHERE name = :name LIMIT 1")
    suspend fun getFoodByName(name: String): FoodEntity?

    @Query("SELECT * FROM foods ORDER BY name ASC")
    suspend fun getAllFoods(): List<FoodEntity>

    @Query("SELECT COUNT(*) FROM foods")
    suspend fun getFoodCount(): Int
} 