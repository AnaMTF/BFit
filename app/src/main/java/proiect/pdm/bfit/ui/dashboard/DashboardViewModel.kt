package proiect.pdm.bfit.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import proiect.pdm.bfit.data.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@HiltViewModel
class DashboardViewModel @Inject constructor(
    application: Application,
    private val statsRepository: StatsRepository,
    private val workoutRepository: WorkoutRepository,
    private val mealRepository: MealRepository,
    private val foodRepository: FoodRepository
) : AndroidViewModel(application) {

    private var userId: Long = 0L

    private val _dailyStats = MutableLiveData<DailyStatsEntity?>()
    val dailyStats: LiveData<DailyStatsEntity?> = _dailyStats

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    init {
        _selectedDate.value = getTodayDate()
        loadStatsForDate(_selectedDate.value!!)
        initializeFoodDatabase()
    }

    fun setUserId(id: Long) {
        userId = id
    }

    private fun initializeFoodDatabase() {
        viewModelScope.launch {
            if (foodRepository.getFoodCount() == 0) {
                val context = getApplication<Application>().applicationContext
                val inputStream = context.assets.open("foods.json")
                val json = inputStream.bufferedReader().use { it.readText() }

                val gson = Gson()
                val foodListType = object : TypeToken<List<FoodEntity>>() {}.type
                val foods: List<FoodEntity> = gson.fromJson(json, foodListType)

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
            val stats = statsRepository.getDailyStatsByDate(date)
            val workouts = workoutRepository.getWorkoutsByDate(date, userId)
            val meals = mealRepository.getMealsByDate(date, userId)

            val totalCaloriesBurned = workouts.sumOf { it.caloriesBurned }
            val totalCaloriesConsumed = meals.sumOf { meal ->
                val quantityInGrams = if (meal.unit == "oz") {
                    meal.quantity * 28.3495f
                } else {
                    meal.quantity
                }
                val food = foodRepository.getFoodByName(meal.name)
                (quantityInGrams / 100f * (food?.calories ?: 0f)).toInt()
            }

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

            statsRepository.addDailyStats(updatedStats)
            _dailyStats.value = updatedStats
        }
    }

    fun refreshStats() {
        loadStatsForDate(_selectedDate.value!!)
    }

    fun addWaterIntake(amount: Int) {
        viewModelScope.launch {
            val currentStats = _dailyStats.value ?: createNewDailyStats()
            val updatedStats = currentStats.copy(waterIntake = currentStats.waterIntake + amount)
            statsRepository.addDailyStats(updatedStats)
            _dailyStats.value = updatedStats
        }
    }

    fun addSteps(amount: Int) {
        viewModelScope.launch {
            val currentStats = _dailyStats.value ?: createNewDailyStats()
            val updatedStats = currentStats.copy(steps = currentStats.steps + amount)
            statsRepository.addDailyStats(updatedStats)
            _dailyStats.value = updatedStats
        }
    }

    fun addWorkout(workoutType: String, duration: Int, calories: Int) {
        viewModelScope.launch {
            val currentStats = _dailyStats.value ?: createNewDailyStats()
            val workout = WorkoutEntity(
                dayDate = _selectedDate.value!!,
                name = workoutType,
                durationMinutes = duration,
                caloriesBurned = calories,
                userId = userId
            )
            workoutRepository.addWorkout(workout)

            val updatedStats = currentStats.copy(
                workouts = currentStats.workouts + 1,
                caloriesBurned = currentStats.caloriesBurned + calories
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

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
