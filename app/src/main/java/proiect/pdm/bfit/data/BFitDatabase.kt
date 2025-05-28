package proiect.pdm.bfit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [UserEntity::class, DailyStatsEntity::class, MealEntity::class, WorkoutEntity::class, FoodEntity::class],
    version = 6
)
abstract class BFitDatabase : RoomDatabase(){
    abstract fun userDao(): UserDao
    abstract fun statsDao(): StatsDao
    abstract fun mealDao(): MealDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun foodDao(): FoodDao

    companion object {
        @Volatile private var INSTANCE: BFitDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE daily_stats ADD COLUMN waterIntake INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE daily_stats ADD COLUMN workouts INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS foods (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        calories REAL NOT NULL,
                        protein REAL NOT NULL,
                        carbs REAL NOT NULL,
                        fat REAL NOT NULL
                    )
                """)
            }
        }

        @Synchronized
        fun getDatabase(context: Context): BFitDatabase {
            if (INSTANCE == null){
                INSTANCE = Room.databaseBuilder(
                    context,
                    BFitDatabase::class.java,
                    "bfit_database"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                .build()
            }
            return INSTANCE!!
        }
    }
}