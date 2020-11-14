package com.ficko.runnisticapp.ui.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ficko.runnisticapp.R
import com.ficko.runnisticapp.db.Run
import com.ficko.runnisticapp.other.Constants.ACTION_PAUSE_SERVICE
import com.ficko.runnisticapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.ficko.runnisticapp.other.Constants.ACTION_STOP_SERVICE
import com.ficko.runnisticapp.other.Constants.MAP_ZOOM
import com.ficko.runnisticapp.other.Constants.POLYLINE_COLOR
import com.ficko.runnisticapp.other.Constants.POLYLINE_WIDTH
import com.ficko.runnisticapp.other.TrackingUtility
import com.ficko.runnisticapp.services.Polyline
import com.ficko.runnisticapp.services.TrackingService
import com.ficko.runnisticapp.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()
    private var isTracking = false      // Initially we are not tracking the users location
    private var pathPoints = mutableListOf<Polyline>()

    private var map: GoogleMap? = null

    private var currentTimeInMillis = 0L

    private var menu: Menu? = null

    private var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    // We need to worry about the lifecycle of our MapView, which is why we need to override the default methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView?.onCreate(savedInstanceState)

        mapView?.getMapAsync { // Only called when the fragment is created
            map = it
            addAllPolylines()
        }

        btnToggleRun.setOnClickListener { toggleRun() }
        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }

        subscribeToObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        // If the run was started, we want to show the menu item for canceling the run
        if (currentTimeInMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miCancelTracking ->
                showCancelTrackingDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle(R.string.cancel_dialog_title)
            .setMessage(R.string.cancel_dialog_message)
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                stopRun()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            focusCameraOnUser()
        })

        TrackingService.runTimeInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopwatchTime(currentTimeInMillis, true)
            tvTimer.text = formattedTime
        })

    }

    private fun toggleRun() {
        if (isTracking) {
            showCancelRunIcon()
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun showCancelRunIcon() {
        menu?.getItem(0)?.isVisible = true
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking

        if (!isTracking) {
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else {
            btnToggleRun.text = "Stop"
            showCancelRunIcon()
            btnFinishRun.visibility = View.GONE
        }
    }

    // Used when we observe a new polyline being added via LiveData
    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val secondLastIndex = pathPoints.last().size - 2
            val secondLastLatLng = pathPoints.last()[secondLastIndex]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(secondLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polylineOptions)
        }
    }

    // Used when the device is rotated, so that the path is fully drawn again
    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (position in polyline) {
                bounds.include(position)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                getMapPaddingValue()
            )
        )
    }

    private fun getMapPaddingValue(): Int {
        return (mapView.height * 0.05f).toInt()
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            val run = gatherDataAndCreateRunObject(bmp)

            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    private fun gatherDataAndCreateRunObject(bmp: Bitmap?): Run {
        var distanceInMeters = 0
        for (polyline in pathPoints) {
            distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
        }
        // Returns speed with a single decimal place
        val avgSpeed =
            round((distanceInMeters / 1000f) / (currentTimeInMillis / 1000 / 360) * 10) / 10f
        val dateTimestamp = Calendar.getInstance().timeInMillis
        val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
        val run = Run(
            bmp,
            dateTimestamp,
            avgSpeed,
            distanceInMeters,
            currentTimeInMillis,
            caloriesBurned
        )
        return run
    }

    private fun focusCameraOnUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}