package com.ficko.runnisticapp.repositories

import com.ficko.runnisticapp.db.Run
import com.ficko.runnisticapp.db.RunDAO
import javax.inject.Inject

// The job of the repository is to collect data from all our data sources (in this application there is only the Room database)
// If we would have communication with an API in our app, the functions for this communication would also be defined here
class MainRepository @Inject constructor(
    val runDao: RunDAO
) {
    // Must be a suspend function because it will be executed inside a coroutine (so that it is asynchronous)
    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.insertRun(run)

    // Not a suspend function because the runDao function returns a LiveData object, which is asynchronous by default
    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByAverageSpeed() = runDao.getAllRunsSortedByAverageSpeed()

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByDuration() = runDao.getAllRunsSortedByDuration()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalAverageSpeed() = runDao.getTotalAverageSpeed()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalDuration() = runDao.getTotalDuration()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()
}