package com.example.runtracker.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.runtracker.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runtracker.R
import com.example.runtracker.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navHost:FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navHost = findViewById(R.id.navHostFragment)
        navigateToTrackingFrag(intent)
        setSupportActionBar(binding.toolbar)


        binding.bottomNavigationView.setupWithNavController(navHost.findNavController())
        navHost.findNavController().addOnDestinationChangedListener { _,destination, _ ->
            when(destination.id){
                R.id.settingsFragment,R.id.statisticsFragment,R.id.runFragment ->
                        binding.bottomNavigationView.visibility = View.VISIBLE

                else -> binding.bottomNavigationView.visibility = View.GONE
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFrag(intent)
    }
    private fun navigateToTrackingFrag(intent:Intent?){
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT){
            navHost.findNavController().navigate(R.id.action_global_tracking_fragment)

        }
    }
}