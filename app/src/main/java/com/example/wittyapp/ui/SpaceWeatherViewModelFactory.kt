package com.example.wittyapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wittyapp.net.SpaceWeatherApi

class SpaceWeatherViewModelFactory(
    private val api: SpaceWeatherApi
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpaceWeatherViewModel::class.java)) {
            return SpaceWeatherViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}