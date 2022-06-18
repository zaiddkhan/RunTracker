package com.example.runtracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.runtracker.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val repository: MainRepository
):ViewModel() {

    val totalTimeRun = repository.totalTimeInMillis()
    val totalDistance = repository.getTotalCaloriesBurnt()
    val totalCaloriesBurnt = repository.getTotalCaloriesBurnt()
    val totalAvgSpeed = repository.getTotalAvgSpeed()

    val runsSortedByDate = repository.getAllRunSortedByDate()
}