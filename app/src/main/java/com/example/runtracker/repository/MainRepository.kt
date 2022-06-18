package com.example.runtracker.repository

import com.example.runtracker.db.Run
import com.example.runtracker.db.RunDao
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDao : RunDao
) {

    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunSortedByTimeInMillis() = runDao.getAllRunsSortedByTimeInMillis()

    fun getAllRunSortedByCaloriesBurnt() = runDao.getAllRunsSortedByCaloriesBurnt()

    fun getAllRunSortedByAvgSpeed() = runDao.getAllRunsSortedByAvgSpeed()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalDistance() = runDao.getTotalDistanceInMetres()

    fun getTotalCaloriesBurnt() = runDao.getTotalCaloriesBurnt()

    fun totalTimeInMillis() = runDao.getTotalTimeInMillis()

}