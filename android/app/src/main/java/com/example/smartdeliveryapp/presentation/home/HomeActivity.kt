package com.example.smartdeliveryapp.presentation.home

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.example.smartdeliveryapp.R
import com.example.smartdeliveryapp.databinding.ActivityHomeBinding
import com.example.smartdeliveryapp.presentation.tracking.TrackingService
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
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
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMap()
        setupUI()
        setupNavigation()
        setupSocket()
    }

    private fun setupMap() {
        map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(15.0)
        map.controller.setCenter(GeoPoint(36.8189, 10.1658)) // Tunis
    }

    private fun setupUI() {
        // Search bar open drawer
        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Action button (Livreur)
        binding.btnAction.setOnClickListener {
            toggleOnlineStatus()
        }

        // New Order (Client)
        binding.btnNewOrder.setOnClickListener {
            startActivity(Intent(this, com.example.smartdeliveryapp.presentation.order.CreateOrderActivity::class.java))
        }

        // Update Drawer Header based on role
        val navHeader = binding.navigationView.getHeaderView(0)
        val tvName = navHeader.findViewById<TextView>(R.id.tvHeaderUsername)
        val tvRole = navHeader.findViewById<TextView>(R.id.tvHeaderRole)
        
        val username = sharedPreferences.getString("username", "Utilisateur")
        val role = sharedPreferences.getString("user_role", "client")
        
        tvName.text = username
        tvRole.text = "Rôle: ${role?.uppercase()}"

        // Show Admin option if admin
        if (role == "admin") {
            binding.navigationView.menu.findItem(R.id.drawer_admin).isVisible = true
        }

        // Hide "Go Online" if not a driver
        if (role != "driver") {
            binding.btnAction.visibility = android.view.View.GONE
        }
    }

    private fun setupNavigation() {
        // Side Drawer Navigation
        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.drawer_kyc -> startActivity(Intent(this, com.example.smartdeliveryapp.presentation.kyc.KycActivity::class.java))
                R.id.drawer_admin -> startActivity(Intent(this, com.example.smartdeliveryapp.presentation.admin.AdminDashboardActivity::class.java))
                R.id.drawer_logout -> {
                    sharedPreferences.edit().clear().apply()
                    startActivity(Intent(this, com.example.smartdeliveryapp.presentation.auth.LoginActivity::class.java))
                    finishAffinity()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Bottom Navigation
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { /* Already here */ }
                R.id.nav_orders -> Toast.makeText(this, "Mes Commandes (Bientôt)", Toast.LENGTH_SHORT).show()
                R.id.nav_profile -> binding.drawerLayout.openDrawer(GravityCompat.START)
            }
            true
        }
    }

    private fun toggleOnlineStatus() {
        if (isOnline) {
            stopTracking()
            binding.btnAction.text = "Passer en ligne"
            binding.tvStatus.text = "Prêt pour livrer ?"
        } else {
            checkPermissionsAndStartTracking()
        }
        isOnline = !isOnline
    }

    private fun setupSocket() {
        val userId = sharedPreferences.getString("user_id", "")
        if (!socket.connected()) socket.connect()
        socket.emit("join", userId)
        
        socket.on("newOrder") {
            runOnUiThread {
                Toast.makeText(this, "🔔 Nouvelle commande disponible !", Toast.LENGTH_LONG).show()
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
        val intent = Intent(this, TrackingService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        binding.btnAction.text = "Passer hors ligne"
        binding.tvStatus.text = "🟢 Vous êtes en ligne"

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

    override fun onResume() { super.onResume(); map.onResume() }
    override fun onPause() { super.onPause(); map.onPause() }
}
