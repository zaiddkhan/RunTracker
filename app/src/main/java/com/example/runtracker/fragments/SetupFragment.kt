package com.example.runtracker.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runtracker.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runtracker.Constants.KEY_NAME
import com.example.runtracker.Constants.KEY_WEIGHT
import com.example.runtracker.R
import com.example.runtracker.databinding.FragmentRunBinding
import com.example.runtracker.databinding.FragmentSetupBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint

class SetupFragment:Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPref:SharedPreferences

    @set:Inject
    var isFirstAppOpen = true



    private lateinit var binding: FragmentSetupBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetupBinding.inflate(layoutInflater)


        if(!isFirstAppOpen){
            val navOption = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment,true)
                .build()
            findNavController().navigate(R.id.action_setupFragment_to_runFragment,
            savedInstanceState,navOption)
        }
        binding.tvContinue.setOnClickListener {
            val success = writeDataToSharedPrefs()
            if(success){
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            }
        }
        return binding.root
    }
    private fun writeDataToSharedPrefs():Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if(name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME,name)
            .putFloat(KEY_WEIGHT,weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE,false)
            .apply()


        val toolbarText = "Let's go,$name"
        return true
    }


}