package com.example.found404.models

data class ProfileDto(
    val id: Long,
    val name: String,
    val email: String,
    val profilePhotoUri: String? = null // Дополнительное поле для фото профиля
)