package com.example.found404.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitInstance {

    private lateinit var context: Context

    // Метод чтобы передать контекст один раз в начале
    fun init(context: Context) {
        this.context = context.applicationContext
    }

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val sharedPreferences =
                    context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("jwt_token", null)

                val requestBuilder: Request.Builder = chain.request().newBuilder()
                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    private fun getToken(): String? {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", null)
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.1.4:8081/") // Ваш IP и порт
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
