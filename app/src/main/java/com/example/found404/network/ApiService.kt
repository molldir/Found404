package com.example.found404.network

import com.example.found404.models.AppUser
import com.example.found404.models.ItemDTO
import com.example.found404.models.LoginRequest
import com.example.found404.models.ProfileUpdateDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // Регистрация пользователя
    @POST("/api/users/register")
    fun registerUser(@Body user: AppUser): Call<AppUser>

    // Логин пользователя
    @POST("/api/auth/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<String>

    // Получить все объявления пользователя
    @GET("/api/items/user/{userId}")
    fun getItemsByUser(@Path("userId") userId: Long): Call<List<ItemDTO>>

    @GET("/api/items/{id}")
    fun getItemById(@Path("id") id: Long): Call<ItemDTO>


    // Добавить новое объявление
    @Multipart
    @POST("/api/items/add")
    fun addItem(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("status") status: RequestBody,
        @Part("category") category: RequestBody,
        @Part("location") location: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("reward") reward: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Call<ItemDTO>

    // Получить все потерянные предметы
    @GET("/api/items/losts")
    fun getLostItems(): Call<List<ItemDTO>>

    // Получить все найденные предметы
    @GET("/api/items/found")
    fun getFoundItems(): Call<List<ItemDTO>>

    // Получить все предметы с фильтрацией по статусу, категории и местоположению
    @GET("/api/items")
    fun getItems(
        @Query("status") status: String? = null,
        @Query("category") category: String? = null,
        @Query("location") location: String? = null
    ): Call<List<ItemDTO>>

    // Удаление объявления
    @DELETE("api/items/{id}")
    fun deleteItem(
        @Path("id") itemId: Long,
        @Query("userId") userId: Long
    ): Call<Void>

    // Получить пользователя по email
    @GET("/api/users/byEmail")
    fun getUserByEmail(@Query("email") email: String): Call<AppUser>


    @GET("/api/items/user/{userId}")
    fun getItemsByUser(
        @Path("userId") userId: Long,
        @Query("location") location: String? = null,
        @Query("date") date: String? = null
    ): Call<List<ItemDTO>>

    // Обновить объявление
    @Multipart
    @PUT("/api/items/{id}")
    fun updateItem(
        @Path("id") id: Long,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("status") status: RequestBody,
        @Part("category") category: RequestBody,
        @Part("location") location: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("reward") reward: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Call<ItemDTO>

    @DELETE("items/{id}")
    fun deleteItem(@Path("id") itemId: Long): Call<Void>

    @GET("/api/items")
    fun getItems(@Query("status") status: String): Call<List<ItemDTO>>

    @GET("/api/items")
    fun getItemsByStatusAndDate(
        @Query("status") status: String,
        @Query("date") date: String
    ): Call<List<ItemDTO>>

    @GET("/api/items")
    fun getItemsByStatusAndLocation(
        @Query("status") status: String,
        @Query("location") location: String
    ): Call<List<ItemDTO>>

    @GET("/api/items/search")
    fun searchItems(@Query("text") text: String): Call<List<ItemDTO>>

    // Получить все предметы (с фильтрацией по статусу)
    @GET("/api/items/all")
    fun getAllItems(): Call<List<ItemDTO>>

    @GET("/api/profile")
    fun getProfile(): Call<AppUser>

    // Обновить профиль пользователя (PUT запрос)
    @PUT("/api/profile")
    fun updateProfile(@Body updateDto: ProfileUpdateDto): Call<AppUser>
}
