package proiect.pdm.bfit.data

class WorkoutRepository(val workoutDao: WorkoutDao) {
    suspend fun addWorkout(workout: WorkoutEntity) = workoutDao.insert(workout)
    suspend fun getWorkoutsByDate(date: String) = workoutDao.getWorkoutsByDate(date)
    suspend fun getWorkoutsByUser(userId: Long) = workoutDao.getWorkoutsByUser(userId)
    suspend fun getWorkoutsByUserAndDate(userId: Long, date: String) = workoutDao.getWorkoutsByUserAndDate(userId, date)
}