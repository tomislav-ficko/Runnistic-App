package com.ficko.runnisticapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.ficko.runnisticapp.R
import com.ficko.runnisticapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Must be done because it could happen that the Activity was destroyed,
        // but the Service is still running and a notification click would bring us back here
        navigateToTrackingFragmentIfNeeded(intent)

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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            // If this is true, we know that this activity was launched by a notification click
            navHostFragment.findNavController().navigate(R.id.action_global_trackingFragment)
        }
    }
}