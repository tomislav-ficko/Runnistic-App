package com.ficko.runnisticapp.other

import android.Manifest
import android.content.Context
import android.os.Build
import pub.devrel.easypermissions.EasyPermissions
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

object TrackingUtility {

    fun hasLocationPermissions(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Device is not running Android 10, so the background location permission doesn't need to be requested
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }

    fun getFormattedStopwatchTime(ms: Long, includeMillis: Boolean = false): String {
        val f: NumberFormat = DecimalFormat("00")
        var milliseconds = ms // We are copying the value so that we can edit it

        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        if (!includeMillis) {
            return "${f.format(hours)}:${f.format(minutes)}:${f.format(seconds)}"
        }

        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10  // We are dividing it so that we get a two-digit number for the milliseconds

        return "${f.format(hours)}:${f.format(minutes)}:${f.format(seconds)}:${f.format(milliseconds)}"
    }
}