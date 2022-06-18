package com.example.runtracker.db

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run:Run)

    @Delete
    suspend fun deleteRun(run:Run)

    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate():LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeedInKMPH DESC")
    fun getAllRunsSortedByAvgSpeed():LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeInMillis():LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY caloriesBurnt DESC")
    fun getAllRunsSortedByCaloriesBurnt():LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distanceInMetres DESC")
    fun getAllRunsSortedByDistance():LiveData<List<Run>>



    @Query("SELECT SUM(timeInMillis) FROM running_table")
    fun getTotalTimeInMillis():LiveData<Long>

    @Query("SELECT SUM(distanceInMetres) FROM running_table")
    fun getTotalDistanceInMetres():LiveData<Int>

    @Query("SELECT AVG(avgSpeedInKMPH) FROM running_table")
    fun getTotalAvgSpeed():LiveData<Float>

    @Query("SELECT SUM(caloriesBurnt) FROM running_table")
    fun getTotalCaloriesBurnt():LiveData<Int>


}