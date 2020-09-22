package com.ficko.runnisticapp.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ficko.runnisticapp.R
import com.ficko.runnisticapp.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {

    // Since Dagger manages the ViewModelFactory behind the scenes, it will choose the appropriate ViewModel from "viewModels()" and assign it to the variable
    private val viewModel: MainViewModel by viewModels()
}