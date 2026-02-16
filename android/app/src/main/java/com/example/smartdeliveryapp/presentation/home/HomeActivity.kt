package com.example.smartdeliveryapp.presentation.home

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smartdeliveryapp.R
import com.example.smartdeliveryapp.databinding.ActivityHomeBinding
import com.example.smartdeliveryapp.presentation.tracking.TrackingService
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var map: MapView
    
    @Inject lateinit var sharedPreferences: SharedPreferences
    @Inject lateinit var socket: Socket

    private var isOnline = false
    private var myLocationOverlay: MyLocationNewOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // OSMDroid configuration
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        
        // Default View (Paris)
        val mapController = map.controller
        mapController.setZoom(15.0)
        val startPoint = GeoPoint(48.8566, 2.3522)
        mapController.setCenter(startPoint)

        setupSocket()

        binding.btnAction.setOnClickListener {
            if (isOnline) {
                stopTracking()
                binding.btnAction.text = "Go Online"
                binding.tvStatus.text = "Status: Offline"
            } else {
                checkPermissionsAndStartTracking()
            }
            isOnline = !isOnline
        }
    }
    
    private fun setupSocket() {
        val userId = sharedPreferences.getString("user_id", "")
        if(!socket.connected()) socket.connect()
        socket.emit("join", userId)
        
        socket.on("newOrder") { args ->
            runOnUiThread {
                Toast.makeText(this, "New Order Received!", Toast.LENGTH_LONG).show()
                binding.tvStatus.text = "Status: New Order!"
            }
        }
    }

    private fun checkPermissionsAndStartTracking() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            startTracking()
        }
    }

    private fun startTracking() {
        // Start Service
        val intent = Intent(this, TrackingService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        binding.btnAction.text = "Go Offline"
        binding.tvStatus.text = "Status: Online (Tracking)"
        
        // Enable MyLocation on Map
        if (myLocationOverlay == null) {
            myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
            myLocationOverlay?.enableMyLocation()
            map.overlays.add(myLocationOverlay)
        }
        myLocationOverlay?.enableFollowLocation()
    }
    
    private fun stopTracking() {
        val intent = Intent(this, TrackingService::class.java)
        stopService(intent)
        
        myLocationOverlay?.disableMyLocation()
        myLocationOverlay?.disableFollowLocation()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTracking()
        }
    }
}
