package com.example.smartdeliveryapp.data.repository

import android.content.SharedPreferences
import com.example.smartdeliveryapp.data.api.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sharedPreferences: SharedPreferences
) {

    suspend fun login(email: String, password: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = mapOf("email" to email, "password" to password)
            val response = apiService.login(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val token = body["token"] as String
                val role = body["role"] as String
                val userId = body["_id"] as String
                
                sharedPreferences.edit()
                    .putString("jwt_token", token)
                    .putString("user_role", role)
                    .putString("user_id", userId)
                    .putString("username", body["username"] as? String ?: "Utilisateur")
                    .apply()
                
                Result.success(true)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, email: String, password: String, role: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = mapOf(
                "username" to username,
                "email" to email, 
                "password" to password,
                "role" to role
            )
            val response = apiService.register(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val token = body["token"] as String
                val userRole = body["role"] as String
                val userId = body["_id"] as String

                sharedPreferences.edit()
                    .putString("jwt_token", token)
                    .putString("user_role", userRole)
                     .putString("user_id", userId)
                     .putString("username", username)
                    .apply()
                    
                Result.success(true)
            } else {
                Result.failure(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
