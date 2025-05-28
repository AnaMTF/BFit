package proiect.pdm.bfit.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proiect.pdm.bfit.data.BFitDatabase
import proiect.pdm.bfit.data.MealDao
import proiect.pdm.bfit.data.UserRepository
import proiect.pdm.bfit.data.StatsRepository
import proiect.pdm.bfit.data.MealRepository
import proiect.pdm.bfit.data.WorkoutRepository
import proiect.pdm.bfit.data.StatsDao
import proiect.pdm.bfit.data.UserDao
import proiect.pdm.bfit.data.WorkoutDao
import proiect.pdm.bfit.data.FoodDao
import proiect.pdm.bfit.data.FoodRepository
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(SingletonComponent::class)
object DatabaseProvider {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): BFitDatabase =
        BFitDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideUserDao(database: BFitDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideStatsDao(database: BFitDatabase): StatsDao {
        return database.statsDao()
    }

    // Add other DAOs as needed
    @Provides
    @Singleton
    fun provideMealDao(database: BFitDatabase): MealDao {
        return database.mealDao()
    }

    @Provides
    @Singleton
    fun provideWorkoutDao(database: BFitDatabase): WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    @Singleton
    fun provideFoodDao(database: BFitDatabase): FoodDao {
        return database.foodDao()
    }

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository {
        return UserRepository(userDao)
    }

    @Provides
    @Singleton
    fun provideStatsRepository(statsDao: StatsDao): StatsRepository {
        return StatsRepository(statsDao)
    }

    @Provides
    @Singleton
    fun provideMealRepository(mealDao: MealDao): MealRepository {
        return MealRepository(mealDao)
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(workoutDao: WorkoutDao): WorkoutRepository {
        return WorkoutRepository(workoutDao)
    }

    @Provides
    @Singleton
    fun provideFoodRepository(foodDao: FoodDao): FoodRepository {
        return FoodRepository(foodDao)
    }
}