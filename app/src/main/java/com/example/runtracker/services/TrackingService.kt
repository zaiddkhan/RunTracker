package com.example.runtracker.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.readPendingIntentOrNullFromParcel
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.provider.CallLog
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runtracker.Constants.ACTION_PAUSE_SERVICE
import com.example.runtracker.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runtracker.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runtracker.Constants.ACTION_STOP_SERVICE
import com.example.runtracker.Constants.FASTEST_LOCATION_INTERVAL
import com.example.runtracker.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runtracker.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runtracker.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runtracker.Constants.NOTIFICATION_ID
import com.example.runtracker.Constants.TIMER_UPDATE_INTERVAL
import com.example.runtracker.R
import com.example.runtracker.TrackingUtility
import com.example.runtracker.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var serviceKilled = false
    var isFirstRun = true

    lateinit var currNotificationBuilder : NotificationCompat.Builder
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    @Inject
    lateinit var baseNotfication : NotificationCompat.Builder

    private val timeRunInSeconds = MutableLiveData<Long>()


    companion object{
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun postInitialValue(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValue()
        currNotificationBuilder = baseNotfication
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    }?: pathPoints.postValue(mutableListOf(mutableListOf()))

    val locationCallBack = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
           if(isTracking.value!!){
               result?.locations?.let {
                   for(location in it){
                       addPathPoint(location)
                   }
               }
           }
        }
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L


    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.IO).launch {
            while (isTracking.value!!){
                lapTime = System.currentTimeMillis() - timeStarted

                timeRunInMillis.postValue(timeRun + lapTime)
                if(timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }

    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }


    private fun updateNotificationTrackingState(isTracking:Boolean){
        val notificationActionText = if(isTracking) "Pause" else "resume"
        val pendingIntent = if(isTracking){
            val pauseIntent = Intent(this,TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this,1,pauseIntent, FLAG_UPDATE_CURRENT)
        }else{
            val resumeIntent = Intent(this,TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this,2,resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currNotificationBuilder,ArrayList<NotificationCompat.Action>())
        }
        if(!serviceKilled){
            currNotificationBuilder = baseNotfication
                .addAction(R.drawable.ic_run,notificationActionText, pendingIntent)
            notificationManager.notify (NOTIFICATION_ID,currNotificationBuilder.build())
        }

    }
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking:Boolean){
        if(isTracking){
            if(TrackingUtility.hasLocationEnabled(this)){
                val request = com.google.android.gms.location.LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval =  FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }

                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallBack,
                    Looper.getMainLooper()
                )
            }
            else{
                fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
            }
        }
    }

    private fun addPathPoint(location:Location?){
        location?.let {
            val position = LatLng(location.latitude,location.longitude)
            pathPoints.value?.apply {
                last().add(position)
                pathPoints.postValue(this)
            }
        }
    }

    private fun killService(){
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValue()
        stopForeground(true)
        stopSelf()

    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    if(isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    }else{
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    killService()
                }

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }



    private fun startForegroundService(){
        startTimer()
        isTracking.postValue(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }


        startForeground(NOTIFICATION_ID,baseNotfication.build())
        if(!serviceKilled){
            timeRunInSeconds.observe(this, Observer {
                val sendNotification = currNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it + 1000L))

                notificationManager.notify(NOTIFICATION_ID, sendNotification.build())

            })
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)
    }



}