package com.ficko.runnisticapp.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ficko.runnisticapp.db.Run
import com.ficko.runnisticapp.repositories.MainRepository
import kotlinx.coroutines.launch

// The job of the ViewModel class is to collect data from our repository and provide it to all fragments that will need this ViewModel
class MainViewModel @ViewModelInject constructor( // When using this annotation, Dagger Hilt will deal with injecting the ViewModelFactory
    val mainRepository: MainRepository
) : ViewModel() {

    val runsSortedByDate = mainRepository.getAllRunsSortedByDate()

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}