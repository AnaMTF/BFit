package proiect.pdm.bfit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, DailyStatsEntity::class, MealEntity::class, WorkoutEntity::class],
    version = 4,
    exportSchema = false
)
abstract class BFitDatabase : RoomDatabase(){
    abstract fun userDao(): UserDao
    abstract fun statsDao(): StatsDao
    abstract fun mealDao(): MealDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile private var INSTANCE: BFitDatabase? = null

        fun getDatabase(context: Context): BFitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BFitDatabase::class.java,
                    "bfit_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }
}