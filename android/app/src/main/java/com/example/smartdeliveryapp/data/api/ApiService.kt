package com.example.smartdeliveryapp.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    
    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: Map<String, String>): Response<Map<String, String>>

    @POST("auth/login")
    suspend fun login(@Body request: Map<String, String>): Response<Map<String, String>>

    // Orders
    @POST("orders")
    suspend fun createOrder(@Body request: Map<String, String>): Response<Map<String, String>>
    
    @GET("orders/myorders")
    suspend fun getMyOrders(): Response<List<Map<String, String>>>
    
    @PUT("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: String, 
        @Body request: Map<String, String>
    ): Response<Map<String, String>>
}
