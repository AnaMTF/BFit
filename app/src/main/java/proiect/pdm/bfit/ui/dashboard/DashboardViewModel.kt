package proiect.pdm.bfit.ui.dashboard

import android.app.Application
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import proiect.pdm.bfit.data.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val workoutRepository: WorkoutRepository,
    private val mealRepository: MealRepository,
    private val foodRepository: FoodRepository
) : ViewModel() {

//    private val statsDao = BFitDatabase.getDatabase(statsRepository).statsDao()

    private val _dailyStats = MutableLiveData<DailyStatsEntity?>()
    val dailyStats: LiveData<DailyStatsEntity?> = _dailyStats

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    init {
        _selectedDate.value = getTodayDate()
        loadStatsForDate(_selectedDate.value!!)
        initializeFoodDatabase()
    }

    private fun initializeFoodDatabase() {
        viewModelScope.launch {
            // Only initialize if the database is empty
            if (foodRepository.getFoodCount() == 0) {
                val foods = listOf(
                    FoodEntity(name = "Apple", calories = 52f, protein = 0.3f, carbs = 14f, fat = 0.2f),
                    FoodEntity(name = "Chicken breast", calories = 165f, protein = 31f, carbs = 0f, fat = 3.6f),
                    FoodEntity(name = "Banana", calories = 89f, protein = 1.1f, carbs = 23f, fat = 0.3f),
                   
                )
                foodRepository.addAllFoods(foods)
            }
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        loadStatsForDate(date)
    }

    private fun loadStatsForDate(date: String) {
        viewModelScope.launch {
            // Get daily stats
            val stats = statsRepository.getDailyStatsByDate(date)
            
            // Get workouts for the date
            val workouts = workoutRepository.getWorkoutsByDate(date)
            
            // Get meals for the date
            val meals = mealRepository.getMealsByDate(date)
            
            // Calculate total calories burned from workouts
            val totalCaloriesBurned = workouts.sumOf { it.caloriesBurned }
            
            // Calculate total calories consumed from meals
            val totalCaloriesConsumed = meals.sumOf { meal ->
                // Convert quantity to grams if needed
                val quantityInGrams = if (meal.unit == "oz") {
                    meal.quantity * 28.3495f
                } else {
                    meal.quantity
                }
                // Get food data from database
                val food = foodRepository.getFoodByName(meal.name)
                // Calculate calories based on 100g serving
                (quantityInGrams / 100f * (food?.calories ?: 0f)).toInt()
            }
            
            // Update or create daily stats
            val updatedStats = stats?.copy(
                caloriesBurned = totalCaloriesBurned,
                caloriesIntake = totalCaloriesConsumed,
                workouts = workouts.size
            ) ?: DailyStatsEntity(
                date = date,
                steps = 0,
                caloriesIntake = totalCaloriesConsumed,
                caloriesBurned = totalCaloriesBurned,
                waterIntake = 0,
                workouts = workouts.size
            )
            
            // Save the updated stats
            statsRepository.addDailyStats(updatedStats)
            _dailyStats.value = updatedStats
        }
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun refreshStats() {
        loadStatsForDate(_selectedDate.value!!)
    }

    fun addWaterIntake(amount: Int) {
        viewModelScope.launch {
            val currentStats = _dailyStats.value ?: createNewDailyStats()
            val updatedStats = currentStats.copy(
                waterIntake = currentStats.waterIntake + amount
            )
            statsRepository.addDailyStats(updatedStats)
            _dailyStats.value = updatedStats
        }
    }

    fun addSteps(amount: Int) {
        viewModelScope.launch {
            val currentStats = _dailyStats.value ?: createNewDailyStats()
            val updatedStats = currentStats.copy(
                steps = currentStats.steps + amount
            )
            statsRepository.addDailyStats(updatedStats)
            _dailyStats.value = updatedStats
        }
    }

    fun addWorkout() {
        viewModelScope.launch {
            val currentStats = _dailyStats.value ?: createNewDailyStats()
            val updatedStats = currentStats.copy(
                workouts = currentStats.workouts + 1
            )
            statsRepository.addDailyStats(updatedStats)
            _dailyStats.value = updatedStats
        }
    }

    private fun createNewDailyStats(): DailyStatsEntity {
        return DailyStatsEntity(
            date = _selectedDate.value!!,
            steps = 0,
            caloriesIntake = 0,
            caloriesBurned = 0,
            waterIntake = 0,
            workouts = 0
        )
    }
}
