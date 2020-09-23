package com.ficko.runnisticapp.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.ficko.runnisticapp.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar) // Tells Android that our main toolbar is "toolbar"

        val navigationController = navHostFragment.findNavController()
        // Connecting bottomNavigationView to Navigation Components
        // Whenever an item is clicked on the bottomNavigationView, Android will navigate to the selected fragment
        bottomNavigationView.setupWithNavController(navigationController)

        // Hiding the bottomNavigationView when the SetupFragment and TrackingFragment are visible
        // The listener will be triggered whenever our destination has changed using Navigation Components
        navigationController.addOnDestinationChangedListener { _, destination, _ -> // If we won't use an argument, we can put an underscore instead of the argument name
            when (destination.id) {
                R.id.setupFragment, R.id.trackingFragment ->
                    bottomNavigationView.visibility = View.GONE
                else ->
                    bottomNavigationView.visibility = View.VISIBLE
            }
        }
    }
}
