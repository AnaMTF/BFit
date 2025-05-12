package proiect.pdm.bfit.data

import androidx.room.*

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insert(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts WHERE dayDate = :date")
    suspend fun getWorkoutsByDate(date: String): List<WorkoutEntity>
}
