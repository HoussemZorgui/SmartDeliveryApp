package com.example.smartdeliveryapp.presentation.order

import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.smartdeliveryapp.R
import com.example.smartdeliveryapp.data.api.ApiService
import com.example.smartdeliveryapp.databinding.ActivityCreateOrderBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import javax.inject.Inject
import kotlin.math.sqrt

@AndroidEntryPoint
class CreateOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateOrderBinding
    private lateinit var map: MapView

    @Inject lateinit var sharedPreferences: SharedPreferences

    private var pickupPoint: GeoPoint? = null
    private var dropoffPoint: GeoPoint? = null
    private var pickupMarker: Marker? = null
    private var dropoffMarker: Marker? = null

    // Step 0 = choose pickup, Step 1 = choose dropoff
    private var step = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        binding = ActivityCreateOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMap()
        setupMapTap()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnConfirmOrder.setOnClickListener {
            confirmOrder()
        }
    }

    private fun setupMap() {
        map = binding.orderMap
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(14.0)
        // Default center: Tunis
        map.controller.setCenter(GeoPoint(36.8189, 10.1658))
    }

    private fun setupMapTap() {
        val eventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                handleMapTap(p)
                return true
            }
            override fun longPressHelper(p: GeoPoint): Boolean = false
        }
        map.overlays.add(0, MapEventsOverlay(eventsReceiver))
    }

    private fun handleMapTap(point: GeoPoint) {
        if (step == 0) {
            // Set Pickup
            pickupPoint = point
            addOrMoveMarker(isPickup = true, point = point)
            val display = "%.4f, %.4f".format(point.latitude, point.longitude)
            binding.tvPickupAddress.text = display
            binding.tvPickupAddress.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            binding.tvMapInstruction.text = "🔴  Appuyez pour choisir le point D'ARRIVÉE"
            step = 1
        } else {
            // Set Dropoff
            dropoffPoint = point
            addOrMoveMarker(isPickup = false, point = point)
            val display = "%.4f, %.4f".format(point.latitude, point.longitude)
            binding.tvDropoffAddress.text = display
            binding.tvDropoffAddress.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            binding.tvMapInstruction.text = "✅  Points définis ! Confirmez la course."
            step = 2
            // Calculate price estimate
            calculateAndShowPrice()
        }
    }

    private fun addOrMoveMarker(isPickup: Boolean, point: GeoPoint) {
        if (isPickup) {
            pickupMarker?.let { map.overlays.remove(it) }
            pickupMarker = Marker(map).apply {
                position = point
                title = "Départ"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = ContextCompat.getDrawable(this@CreateOrderActivity, R.drawable.ic_location_green)
            }
            map.overlays.add(pickupMarker)
        } else {
            dropoffMarker?.let { map.overlays.remove(it) }
            dropoffMarker = Marker(map).apply {
                position = point
                title = "Arrivée"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = ContextCompat.getDrawable(this@CreateOrderActivity, R.drawable.ic_location_red)
            }
            map.overlays.add(dropoffMarker)
        }
        map.invalidate()
    }

    private fun calculateAndShowPrice() {
        val pickup = pickupPoint ?: return
        val dropoff = dropoffPoint ?: return

        // Approximate distance in km using Haversine formula
        val lat1 = Math.toRadians(pickup.latitude)
        val lat2 = Math.toRadians(dropoff.latitude)
        val dLat = Math.toRadians(dropoff.latitude - pickup.latitude)
        val dLon = Math.toRadians(dropoff.longitude - pickup.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distanceKm = 6371 * c // Earth radius = 6371 km

        val pricePerKm = 2.5 // 2.5 DT / km
        val baseFare = 3.0
        val estimatedPrice = baseFare + (distanceKm * pricePerKm)
        val etaMinutes = (distanceKm / 30.0 * 60).toInt() + 5 // avg 30km/h

        binding.priceRow.visibility = View.VISIBLE
        binding.tvPriceEstimate.text = "${"%.1f".format(estimatedPrice)} DT  •  ~${etaMinutes} min"

        // Enable confirm button
        binding.btnConfirmOrder.isEnabled = true
        binding.btnConfirmOrder.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.btnConfirmOrder.backgroundTintList = ContextCompat.getColorStateList(this, R.color.accent_volt)
    }

    private fun confirmOrder() {
        val pickup = pickupPoint ?: return
        val dropoff = dropoffPoint ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.btnConfirmOrder.isEnabled = false

        val priceText = binding.tvPriceEstimate.text.toString()
        val priceValue = priceText.split(" ")[0].toDoubleOrNull() ?: 5.0

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = sharedPreferences.getString("jwt_token", "") ?: ""
                val baseUrl = "http://192.168.100.6:5001"

                val body = JSONObject().apply {
                    put("pickupLocation", JSONObject().apply {
                        put("type", "Point")
                        put("coordinates", org.json.JSONArray().apply {
                            put(pickup.longitude)
                            put(pickup.latitude)
                        })
                        put("address", "${pickup.latitude}, ${pickup.longitude}")
                    })
                    put("dropoffLocation", JSONObject().apply {
                        put("type", "Point")
                        put("coordinates", org.json.JSONArray().apply {
                            put(dropoff.longitude)
                            put(dropoff.latitude)
                        })
                        put("address", "${dropoff.latitude}, ${dropoff.longitude}")
                    })
                    put("price", priceValue)
                    put("eta", 15)
                }.toString()

                val request = Request.Builder()
                    .url("$baseUrl/api/orders")
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .post(body.toRequestBody("application/json".toMediaTypeOrNull()))
                    .build()

                val response = OkHttpClient().newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        showSuccess()
                    } else {
                        binding.btnConfirmOrder.isEnabled = true
                        val msg = JSONObject(responseBody ?: "{}").optString("message", "Erreur serveur")
                        Toast.makeText(this@CreateOrderActivity, "❌ $msg", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnConfirmOrder.isEnabled = true
                    Toast.makeText(this@CreateOrderActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showSuccess() {
        binding.tvMapInstruction.text = "🎉  Course créée ! En recherche de livreur..."
        binding.btnConfirmOrder.text = "Course envoyée ✓"
        binding.btnConfirmOrder.isEnabled = false
        Toast.makeText(this, "✅ Course créée ! Un livreur va être assigné.", Toast.LENGTH_LONG).show()

        // Auto close after 2 seconds
        binding.root.postDelayed({ finish() }, 2500)
    }

    override fun onResume() { super.onResume(); map.onResume() }
    override fun onPause() { super.onPause(); map.onPause() }
}
