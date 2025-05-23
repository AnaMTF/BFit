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
class DashboardViewModel @Inject constructor(statsRepository: StatsRepository) : ViewModel() {

//    private val statsDao = BFitDatabase.getDatabase(statsRepository).statsDao()

//    private val _dailyStats = MutableLiveData<DailyStatsEntity?>()
//    val dailyStats: LiveData<DailyStatsEntity?> = _dailyStats
//
//    init {
//        loadTodayStats()
//    }
//
//    private fun loadTodayStats() {
//        viewModelScope.launch {
//            val today = getTodayDate()
//            _dailyStats.value = statsDao.getStatsByDate(today)
//        }
//    }
//
//    private fun getTodayDate(): String {
//        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        return sdf.format(Date())
//    }
}
