package com.example.found404.models

data class ProfileUpdateDto(
    val name: String,
    val profilePhotoUri: String? = null // Также можно передать URI фото, если оно обновляется
)
