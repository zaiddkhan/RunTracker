package com.example.runtracker.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runtracker.SortType
import com.example.runtracker.db.Run
import com.example.runtracker.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
  val repository: MainRepository
):ViewModel(){

  private val runSortedByDate = repository.getAllRunSortedByDate()
  private val runSortedByCaloriesBurnt = repository.getAllRunSortedByCaloriesBurnt()
  private val runSortedByTimeInMills = repository.getAllRunSortedByTimeInMillis()
  private val runSortedByAvgSpeed = repository.getAllRunSortedByAvgSpeed()
  private val runSortedByDistance = repository.getAllRunSortedByDistance()

  val runs = MediatorLiveData<List<Run>>()

  var sortType = SortType.DATE

  init {
     runs.addSource(runSortedByDate) { result ->
       if(sortType == SortType.DATE){
         result?.let {
           runs.value = it
         }
       }
     }
    runs.addSource(runSortedByTimeInMills) { result ->
      if(sortType == SortType.RUNNING_TIME){
        result?.let {
          runs.value = it
        }
      }
    }
    runs.addSource(runSortedByAvgSpeed) { result ->
      if(sortType == SortType.AVG_SPEED){
        result?.let {
          runs.value = it
        }
      }
    }
    runs.addSource(runSortedByCaloriesBurnt) { result ->
      if(sortType == SortType.CALORIES_BURNED){
        result?.let {
          runs.value = it
        }
      }
    }
    runs.addSource(runSortedByDistance) { result ->
      if(sortType == SortType.DISTANCE){
        result?.let {
          runs.value = it
        }
      }
    }
  }


  fun sortRuns(sortType: SortType)= when(sortType){
    SortType.DATE -> runSortedByDate.value?.let { runs.value = it }
    SortType.RUNNING_TIME -> runSortedByTimeInMills.value?.let { runs.value = it }
    SortType.AVG_SPEED -> runSortedByDistance.value?.let { runs.value= it }
    SortType.DISTANCE -> runSortedByDistance.value?.let { runs.value = it }
    SortType.CALORIES_BURNED -> runSortedByCaloriesBurnt.value?.let { runs.value = it }
  }.also {
    this.sortType = sortType
  }

  fun insertRun(run : Run)= viewModelScope.launch {
    repository.insertRun(run)
  }
}









