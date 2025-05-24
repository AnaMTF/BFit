package proiect.pdm.bfit.ui.dashboard

import android.app.Application
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import proiect.pdm.bfit.data.BFitDatabase
import proiect.pdm.bfit.data.DailyStatsEntity
import proiect.pdm.bfit.data.StatsRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {

//    private val statsDao = BFitDatabase.getDatabase(statsRepository).statsDao()

    private val _dailyStats = MutableLiveData<DailyStatsEntity?>()
    val dailyStats: LiveData<DailyStatsEntity?> = _dailyStats

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    init {
        _selectedDate.value = getTodayDate()
        loadStatsForDate(_selectedDate.value!!)
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        loadStatsForDate(date)
    }

    private fun loadStatsForDate(date: String) {
        viewModelScope.launch {
            _dailyStats.value = statsRepository.getDailyStatsByDate(date)
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
