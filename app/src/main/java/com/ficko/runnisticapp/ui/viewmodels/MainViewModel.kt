package com.ficko.runnisticapp.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.ficko.runnisticapp.repositories.MainRepository

// The job of the ViewModel class is to collect data from our repository and provide it to all fragments that will need this ViewModel
class MainViewModel @ViewModelInject constructor( // When using this annotation, Dagger Hilt will deal with injecting the ViewModelFactory
    val mainRepository: MainRepository
) : ViewModel() {
}