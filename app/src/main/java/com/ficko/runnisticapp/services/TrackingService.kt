package com.ficko.runnisticapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.ficko.runnisticapp.R
import com.ficko.runnisticapp.other.Constants.ACTION_PAUSE_SERVICE
import com.ficko.runnisticapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.ficko.runnisticapp.other.Constants.ACTION_STOP_SERVICE
import com.ficko.runnisticapp.other.Constants.FASTEST_LOCATION_INTERVAL
import com.ficko.runnisticapp.other.Constants.LOCATION_UPDATE_INTERVAL
import com.ficko.runnisticapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.ficko.runnisticapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.ficko.runnisticapp.other.Constants.NOTIFICATION_ID
import com.ficko.runnisticapp.other.Constants.TIMER_UPDATE_INTERVAL
import com.ficko.runnisticapp.other.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng> // A single polyline i.e. series of coordinates which form a single connected line on the map

typealias Polylines = MutableList<Polyline> // List of poly-lines
// E.g. tracking is stopped and we continue moving. We don't want to take into account the coordinates that changed when we weren't tracking.
// This is why once tracking starts again, we don't want the new poly-line to be connected to the previous one. The solution is a list of polylines


// By extending the LifecycleService class, we can tell the LiveData "observe()" function in which lifecycle state our TrackingService currently is
@AndroidEntryPoint // Needed in order to properly inject dependencies into the class
class TrackingService : LifecycleService() {

    var isFirstRun = true

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    // Needed because the notification will change from the baseNotificationBuilder as we change the time and active buttons
    lateinit var currentNotificationBuilder: NotificationCompat.Builder

    private val runTimeInSeconds =
        MutableLiveData<Long>() // LiveData for updating the time in the notification (doesn't need as frequent updates as in the app itself)

    companion object { // A new value will be posted inside this object whenever the tracking state changes
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
        val runTimeInMillis =
            MutableLiveData<Long>() // LiveData for updating the time in the application
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)

            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location) // Whenever we retrieve a new location, we add it to the end of our last polyline
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun postInitialValues() { // Initial values must be set, because a NullPointerException could otherwise happen
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        runTimeInMillis.postValue(0L)
        runTimeInSeconds.postValue(0L)
    }

    private fun addEmptyPolyline() = // Add new (empty) polyline when tracking is resumed
        pathPoints.value?.apply {
            add(mutableListOf())
            pathPoints.postValue(this) // We want to post the change we made, so our Fragment is notified about it
        }
            ?: pathPoints.postValue(mutableListOf(mutableListOf())) // We need to take care of the case when "pathPoints.value" is null, just create a list of polylines and add a single empty polyline

    private fun addPathPoint(location: Location?) {
        location?.let {
            val position = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(position)
                pathPoints.postValue(this)
            }
        }
    }

    private var isTimerEnabled = false
    private var lapTime = 0L // Time that will be reset each time when the run is paused and started
    private var totalRunTime = 0L // Total run time, all the lap times added together
    private var timeStarted = 0L // Timestamp when the timer was started
    private var lastSecondTimestamp =
        0L // Last recorded time, in seconds (needed inside the startTimer() while loop)

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime =
                    System.currentTimeMillis() - timeStarted // Time difference between the current time and starting time of the run
                runTimeInMillis.postValue(totalRunTime + lapTime)

                if (runTimeInMillis.value!! >= lastSecondTimestamp + 1000L) { // Checks if a whole second has passed between the current time, and last recorded time in seconds
                    runTimeInSeconds.postValue(runTimeInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }

                delay(TIMER_UPDATE_INTERVAL) // We don't want to update the observers all the time, only every TIMER_UPDATE_INTERVAL (in ms)
            }

            totalRunTime += lapTime // Since we reached this point, we are outside the while loop and stopped tracking the run
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // This will remove the previous notification text so that we replace it, instead of appending the new text
        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            // Set the mActions field to an empty list, clearing its value
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        currentNotificationBuilder = baseNotificationBuilder
            .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
        notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
    }

    @SuppressLint("MissingPermission") // Suppress the error because we've already checked for the permissions inside the if statement
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) { // When tracking is started
            if (TrackingUtility.hasLocationPermissions(this)) { // Request location updates if we are tracking the user and we have the permissions
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else { // When tracking is stopped
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onCreate() {
        super.onCreate()

        currentNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it) // Start observing isTracking for changes (when tracking is started or stopped)
            updateNotificationTrackingState(it)
        })
    }

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
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Service paused")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Service stopped")
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() { // Executed only when first starting the application
        startTimer()
        isTracking.postValue(true) // Start tracking

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) // NOTIFICATION_SERVICE is a system service which is needed whenever we want to show a notification
                    as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        runTimeInSeconds.observe(this, Observer {
            val notification = currentNotificationBuilder
                .setContentText(TrackingUtility.getFormattedStopwatchTime(it * 1000L)) // Multiplication because we want the time in seconds
            notificationManager.notify(NOTIFICATION_ID, notification.build())
        })
    }

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