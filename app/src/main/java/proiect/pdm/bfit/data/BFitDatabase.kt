package proiect.pdm.bfit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, DailyStatsEntity::class, MealEntity::class, WorkoutEntity::class],
    version = 4
)
abstract class BFitDatabase : RoomDatabase(){
    abstract fun userDao(): UserDao
    abstract fun statsDao(): StatsDao
    abstract fun mealDao(): MealDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile private var INSTANCE: BFitDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): BFitDatabase {
            if (INSTANCE == null){
                INSTANCE = Room.databaseBuilder(
                    context,
                    BFitDatabase::class.java,
                    "bfit_database"
                ).fallbackToDestructiveMigration(true).build()
            }
            return INSTANCE!!
        }

    }
}