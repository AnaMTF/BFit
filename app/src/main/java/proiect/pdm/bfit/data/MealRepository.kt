package proiect.pdm.bfit.data

class MealRepository(val mealDao: MealDao) {
    suspend fun addMeal(meal: MealEntity) = mealDao.insert(meal)
    suspend fun getMealsByDate(date: String) = mealDao.getMealsByDate(date)
}