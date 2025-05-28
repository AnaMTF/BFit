package proiect.pdm.bfit.data

class FoodRepository(private val foodDao: FoodDao) {
    suspend fun addFood(food: FoodEntity) = foodDao.insert(food)
    suspend fun addAllFoods(foods: List<FoodEntity>) = foodDao.insertAll(foods)
    suspend fun getFoodByName(name: String) = foodDao.getFoodByName(name)
    suspend fun getAllFoods() = foodDao.getAllFoods()
    suspend fun getFoodCount() = foodDao.getFoodCount()
} 