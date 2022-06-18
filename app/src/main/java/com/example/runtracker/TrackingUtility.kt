package com.example.runtracker

import android.Manifest
import android.content.Context
import android.icu.util.TimeUnit
import android.location.Location
import android.os.Build
import com.example.runtracker.services.Polyline
import pub.devrel.easypermissions.EasyPermissions
import java.sql.Time
import kotlin.time.DurationUnit

object TrackingUtility {

    fun hasLocationEnabled(context:Context) =
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }else{
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }

    fun calculatePolylineLength(polyline: Polyline) : Float{
        var distance = 0f
        for(i in 0..polyline.size-2){
            val pos = polyline[i]
            val pos2 = polyline[i+1]

            val result = FloatArray(1)
            Location.distanceBetween(
                pos.latitude,
                pos.longitude,
                pos2.latitude,
                pos2.longitude,
                result
            )
            distance += result[0]
        }
          return distance
    }
    fun getFormattedStopWatchTime(ms:Long,includeMillis :Boolean = false) :String{

        var milliseconds = ms
        val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= java.util.concurrent.TimeUnit.HOURS.toMillis(hours)
        val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= java.util.concurrent.TimeUnit.MINUTES.toMillis(minutes)
        val second = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        if(!includeMillis){
            return "${if(hours < 10 ) "0" else ""}$hours:" +
                    "${if(minutes < 10 )"0" else ""}$minutes:"+
                    "${if(second<10)"0" else ""}$second:"
        }

        milliseconds -= java.util.concurrent.TimeUnit.SECONDS.toMillis(second)
        milliseconds /= 10
        return "${if(hours < 10 ) "0" else ""}$hours:" +
                "${if(minutes < 10 )"0" else ""}$minutes:"+
                "${if(second<10)"0" else ""}$second:" +
                "${if(milliseconds<10)"0" else ""}$milliseconds"


    }
}