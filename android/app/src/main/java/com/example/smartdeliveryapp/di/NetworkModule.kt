package com.example.smartdeliveryapp.di

import com.example.smartdeliveryapp.data.api.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // 10.0.2.2 is localhost for Android Emulator
    private const val BASE_URL = "http://192.168.100.6:5001/api/"
    private const val SOCKET_URL = "http://192.168.100.6:5001"

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: com.example.smartdeliveryapp.data.api.AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSocketIO(): Socket {
        return try {
            val opts = IO.Options().apply {
                forceNew = true
                reconnection = true
            }
            IO.socket(SOCKET_URL, opts)
        } catch (e: Exception) {
            throw RuntimeException("Socket init failed", e)
        }
    }
}
