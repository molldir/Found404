package com.example.found404.models

data class AppUser(
    val id: Long,
    val name: String,
    val email: String,
    val password: String,
    val profilePhotoUri: String?
)



