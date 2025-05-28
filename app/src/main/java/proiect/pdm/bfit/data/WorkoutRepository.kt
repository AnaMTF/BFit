package proiect.pdm.bfit.data

class WorkoutRepository(private val workoutDao: WorkoutDao) {
    suspend fun addWorkout(workout: WorkoutEntity) = workoutDao.insert(workout)
    suspend fun getWorkoutsByDate(date: String, userId: Long) = workoutDao.getWorkoutsByDate(date, userId)
    suspend fun getWorkoutsByUser(userId: Long) = workoutDao.getWorkoutsByUser(userId)
    suspend fun getWorkoutsByUserAndDate(userId: Long, date: String) = workoutDao.getWorkoutsByUserAndDate(userId, date)
    suspend fun deleteWorkout(workout: WorkoutEntity) = workoutDao.deleteWorkout(workout)
}