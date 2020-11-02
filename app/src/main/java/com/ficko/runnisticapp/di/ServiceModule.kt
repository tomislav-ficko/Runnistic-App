package com.ficko.runnisticapp.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ficko.runnisticapp.R
import com.ficko.runnisticapp.other.Constants
import com.ficko.runnisticapp.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

// Module that holds dependencies for the TrackingService
@Module
@InstallIn(ServiceComponent::class) // It's scope is tied ot the scope of TrackingService (dependencies will be alive only as long as the TrackingService object is alive)
object ServiceModule {

    @ServiceScoped // Makes sure that there is only one instance per Service (similar to Singleton, but limited to the lifetime of the Service)
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ) = FusedLocationProviderClient(app)

    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext app: Context
    ) = PendingIntent.getActivity(
        app,
        0, // Set to zero because we don't need it afterwards
        Intent(app, MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_UPDATE_CURRENT // Means that whenever we launch the PendingIntent and it already exists, it will update it instead of re-creating it
    )

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)   // Prevent notification disappearing if the user clicks on the it
        .setOngoing(true)       // Notification cannot be swiped away
        .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
        .setContentTitle(app.getString(R.string.app_name))
        .setContentText(app.getString(R.string.initial_time))
        .setContentIntent(provideMainActivityPendingIntent(app))
        .build()
}