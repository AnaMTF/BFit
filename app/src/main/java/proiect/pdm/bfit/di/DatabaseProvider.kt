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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseProvider {
    @Provides
    @Singleton
    fun provideDatabase(context: Context): BFitDatabase {
        return BFitDatabase.getDatabase(context)
    }

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
}