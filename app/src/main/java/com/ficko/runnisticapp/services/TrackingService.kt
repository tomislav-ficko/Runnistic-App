package com.ficko.runnisticapp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.ficko.runnisticapp.R
import com.ficko.runnisticapp.other.Constants.ACTION_PAUSE_SERVICE
import com.ficko.runnisticapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.ficko.runnisticapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.ficko.runnisticapp.other.Constants.ACTION_STOP_SERVICE
import com.ficko.runnisticapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.ficko.runnisticapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.ficko.runnisticapp.other.Constants.NOTIFICATION_ID
import com.ficko.runnisticapp.ui.MainActivity
import timber.log.Timber

// By extending the LifecycleService class, we can tell the LiveData "observe()" function in which lifecycle state our TrackingService currently is
class TrackingService : LifecycleService() {

    var isFirstRun = true

    // Gets called whenever we send a command (Intent with action attached) to the service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Service paused")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Service stopped")
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) // NOTIFICATION_SERVICE is a system service which is needed whenever we want to show a notification
                    as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)   // Prevent notification disappearing if the user clicks on the it
            .setOngoing(true)       // Notification cannot be swiped away
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.initial_time))
            .setContentIntent(getPendingIntentForMainActivity())
            .build()
    }

    private fun getPendingIntentForMainActivity() =
        PendingIntent.getActivity(
            this,
            0, // Set to zero because we don't need it afterwards
            Intent(this, MainActivity::class.java).also {
                it.action = ACTION_SHOW_TRACKING_FRAGMENT
            },
            FLAG_UPDATE_CURRENT // Means that whenever we launch the PendingIntent and it already exists, it will update it instead of re-creating it
        )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW // Must be set to low, so that we avoid making notification sounds (since the notification will be updated each second)
        )
        notificationManager.createNotificationChannel(channel)
    }
}