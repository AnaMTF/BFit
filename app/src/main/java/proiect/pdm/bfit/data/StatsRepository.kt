package proiect.pdm.bfit.data

class StatsRepository(val statsDao: StatsDao) {
    suspend fun addDailyStats(dailyStats: DailyStatsEntity) = statsDao.insert(dailyStats)
    suspend fun getDailyStatsByDate(date: String) = statsDao.getStatsByDate(date)
    suspend fun getAllDailyStats() = statsDao.getAll()
}