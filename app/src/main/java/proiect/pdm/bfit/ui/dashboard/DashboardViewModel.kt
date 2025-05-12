package proiect.pdm.bfit.ui.dashboard

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import proiect.pdm.bfit.data.BFitDatabase
import proiect.pdm.bfit.data.DailyStatsEntity
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val statsDao = BFitDatabase.getDatabase(application).statsDao()

    private val _dailyStats = MutableLiveData<DailyStatsEntity?>()
    val dailyStats: LiveData<DailyStatsEntity?> = _dailyStats

    init {
        loadTodayStats()
    }

    private fun loadTodayStats() {
        viewModelScope.launch {
            val today = getTodayDate()
            _dailyStats.value = statsDao.getStatsByDate(today)
        }
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
