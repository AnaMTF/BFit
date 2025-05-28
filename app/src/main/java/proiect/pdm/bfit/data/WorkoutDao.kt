package proiect.pdm.bfit.data

import androidx.room.*

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insert(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts WHERE dayDate = :date AND userId = :userId")
    suspend fun getWorkoutsByDate(date: String, userId: Long): List<WorkoutEntity>

    // 🔹 Workout-uri pentru un anumit user
    @Query("SELECT * FROM workouts WHERE userId = :userId")
    suspend fun getWorkoutsByUser(userId: Long): List<WorkoutEntity>

    // 🔹 Workout-uri pentru un user într-o anumită zi
    @Query("SELECT * FROM workouts WHERE userId = :userId AND dayDate = :date")
    suspend fun getWorkoutsByUserAndDate(userId: Long, date: String): List<WorkoutEntity>

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)
}
