package proiect.pdm.bfit.data

import androidx.room.*

@Dao
interface StatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: DailyStatsEntity)

    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    suspend fun getStatsByDate(date: String): DailyStatsEntity?

    @Query("SELECT * FROM daily_stats ORDER BY date DESC")
    suspend fun getAll(): List<DailyStatsEntity>
}
