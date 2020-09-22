package com.ficko.runnisticapp.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ficko.runnisticapp.R
import com.ficko.runnisticapp.ui.viewmodels.MainViewModel
import com.ficko.runnisticapp.ui.viewmodels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: StatisticsViewModel by viewModels()
}