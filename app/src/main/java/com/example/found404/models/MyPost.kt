package com.example.found404.models

data class MyPost(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val status: String,
    val category: String,
    val location: String,
    val date: String,
    val userId: Long?,
    val type: String
)
