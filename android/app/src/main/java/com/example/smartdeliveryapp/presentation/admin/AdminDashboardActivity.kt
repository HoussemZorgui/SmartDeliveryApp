package com.example.smartdeliveryapp.presentation.admin

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartdeliveryapp.databinding.ActivityAdminDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import javax.inject.Inject

@AndroidEntryPoint
class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding

    @Inject lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        fetchStats()
    }

    private fun setupNavigation() {
        binding.btnManageUsers.setOnClickListener {
            startActivity(Intent(this, AdminUserListActivity::class.java))
        }

        binding.btnManageOrders.setOnClickListener {
            Toast.makeText(this, "Module Commandes en chargement...", Toast.LENGTH_SHORT).show()
        }

        binding.btnManageRestaurants.setOnClickListener {
             Toast.makeText(this, "Module Restaurants en chargement...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchStats() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = sharedPreferences.getString("jwt_token", "") ?: ""
                val baseUrl = "http://192.168.100.6:5001"

                val client = OkHttpClient()
                
                // Fetch Users Count
                val userRequest = Request.Builder()
                    .url("$baseUrl/api/admin/users")
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                
                val userResponse = client.newCall(userRequest).execute()
                val usersCount = if (userResponse.isSuccessful) {
                    JSONArray(userResponse.body?.string()).length()
                } else 0

                // Fetch Orders Count
                val orderRequest = Request.Builder()
                    .url("$baseUrl/api/admin/orders")
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                
                val orderResponse = client.newCall(orderRequest).execute()
                val ordersCount = if (orderResponse.isSuccessful) {
                    JSONArray(orderResponse.body?.string()).length()
                } else 0

                withContext(Dispatchers.Main) {
                    binding.tvTotalUsers.text = usersCount.toString()
                    binding.tvTotalOrders.text = ordersCount.toString()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Fail silently or show error
                }
            }
        }
    }
}
