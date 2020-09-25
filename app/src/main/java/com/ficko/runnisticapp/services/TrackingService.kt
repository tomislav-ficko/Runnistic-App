package com.ficko.runnisticapp.services

import android.content.Intent
import androidx.lifecycle.LifecycleService
import com.ficko.runnisticapp.other.Constants.ACTION_PAUSE_SERVICE
import com.ficko.runnisticapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.ficko.runnisticapp.other.Constants.ACTION_STOP_SERVICE
import timber.log.Timber

// By extending the LifecycleService class, we can tell the LiveData "observe()" function in which lifecycle state our TrackingService currently is
class TrackingService : LifecycleService() {

    // Gets called whenever we send a command (Intent with action attached) to the service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("Started or resumed service")
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }
}