package com.example.runtracker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.runtracker.Constants.ACTION_PAUSE_SERVICE
import com.example.runtracker.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runtracker.Constants.ACTION_STOP_SERVICE
import com.example.runtracker.Constants.MAX_ZOOM
import com.example.runtracker.Constants.POLYLINE_COLOR
import com.example.runtracker.Constants.POLYLINE_WIDTH
import com.example.runtracker.R
import com.example.runtracker.TrackingUtility
import com.example.runtracker.databinding.FragmentTrackingBinding
import com.example.runtracker.db.Run
import com.example.runtracker.services.Polyline
import com.example.runtracker.services.TrackingService
import com.example.runtracker.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment:Fragment(R.layout.fragment_tracking) {

    private val viewModel : MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private lateinit var binding: FragmentTrackingBinding
    private var currTimeInMillis = 0L

    private var menu : Menu? = null

    @set:Inject
     var weight = 80f

    private var map :GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentTrackingBinding.inflate(layoutInflater)
        binding.mapView.onCreate(savedInstanceState)
        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSave()
        }
        binding.btnToggleRun.setOnClickListener{
            toggleRun()
        }
        binding.mapView.getMapAsync{
            map = it
            addAllPolylines()
        }
        subscribeToObservers()
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(currTimeInMillis > 0){
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    private fun showCancelDialog(){
        val dialog = MaterialAlertDialogBuilder(requireContext(),R.style.AlertDialogTheme)
            .setTitle("Cancel The Run")
            .setMessage("Are you sure to cancel the run")
            .setIcon(R.drawable.ic_cancel)
            .setPositiveButton("Yes"){_,_ ->
                stopRun()

            }
            .setNegativeButton("No"){dialog,_ ->
                dialog.cancel()
            }

        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.cancelTracking -> {
                showCancelDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun stopRun(){
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)

    }

    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currTimeInMillis,true)
            binding.tvTimer.text = formattedTime
        })
    }
    private fun toggleRun(){

        if(isTracking){
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking : Boolean){
        this.isTracking = isTracking
        if(!isTracking){
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        }
        else{
            binding.btnToggleRun.text = "Stop"
            binding.btnFinishRun.visibility = View.GONE
            menu?.getItem(0)?.isVisible = true

        }
    }

    private fun moveCameraToUser(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAX_ZOOM
                )
            )
        }
    }

    private fun addAllPolylines(){
        for(polyline in pathPoints) {
            val polyLineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)

            map?.addPolyline(polyLineOptions)
        }
    }

    private fun addLatestPolyline(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1){
            val preLastLatLng = pathPoints.last()[pathPoints.last().size-2]
            val lastLatLng = pathPoints.last().last()

            val polyline = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polyline)
        }
    }
    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()
        for(polyline in pathPoints){
            for(pos in polyline){
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSave(){
        map?.snapshot {
            bmp ->
            var distanceInMetres = 0
            for(polyline in pathPoints){
                distanceInMetres += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }

            val avgSpeed = round((distanceInMetres / 1000f) / (currTimeInMillis / 1000f / 60 / 60) * 10) /10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurnt = ((distanceInMetres / 1000f) * weight).toInt()
            val run = Run(bmp,dateTimeStamp,avgSpeed,distanceInMetres, currTimeInMillis,caloriesBurnt)
            viewModel.insertRun(run)
            stopRun()
        }
    }

    private fun sendCommandToService(action:String) =
        Intent(requireContext(),TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }



    override fun onResume() {
        super.onResume()
        binding.mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView?.onLowMemory()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView?.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView?.onSaveInstanceState(outState)
    }

}