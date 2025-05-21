package com.example.found404.models

data class FoundItem(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val status: String,
    val category: String,
    val location: String,
    val date: String,
    val userId: Long?
)
