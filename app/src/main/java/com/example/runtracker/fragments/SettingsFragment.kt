package com.example.runtracker.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.runtracker.Constants.KEY_NAME
import com.example.runtracker.Constants.KEY_WEIGHT
import com.example.runtracker.R
import com.example.runtracker.databinding.FragmentSettingsBinding
import com.example.runtracker.databinding.FragmentTrackingBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment: Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: FragmentSettingsBinding

    private fun loadFieldsFromSharedPref(){
        val name = sharedPreferences.getString(KEY_NAME,"")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT,80f)
        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        binding.btnApplyChanges.setOnClickListener {
            loadFieldsFromSharedPref()
            val success = applyChangesToSharedPreferences()
            if(success){
                Toast.makeText(requireContext(), "changes saved", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }
    private fun applyChangesToSharedPreferences():Boolean{
        val nameText = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if(nameText.isEmpty() || weight.isEmpty()){
            return false
        }
            sharedPreferences.edit()
                .putString(KEY_NAME,nameText)
                .putFloat(KEY_WEIGHT,weight.toFloat())
                .apply()
        return true
    }
}