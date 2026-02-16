package com.example.smartdeliveryapp.presentation.tracking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.smartdeliveryapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : Service() {

    @Inject lateinit var socket: Socket
    @Inject lateinit var sharedPreferences: SharedPreferences

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    sendLocationUpdate(location)
                }
            }
        }
        
        if(!socket.connected()) {
            socket.connect()
        }
    }

    private fun sendLocationUpdate(location: Location) {
        val userId = sharedPreferences.getString("user_id", null) ?: return
        
        val data = JSONObject().apply {
            put("driverId", userId)
            put("coordinates", JSONObject().apply {
                put("lat", location.latitude)
                put("lng", location.longitude)
            })
        }
        
        socket.emit("updateLocation", data)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        startLocationUpdates()
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "tracking_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Tracking Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Smart Delivery")
            .setContentText("Tracking is active...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(1, notification)
    }

    private fun startLocationUpdates() {
        // Permission check should be handled in Activity before starting service
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build()
                
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        socket.disconnect()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
