package com.example.found404.models

data class LostItem(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val status: String,
    val category: String,
    val location: String,
    val date: String,
    val userId: Long?,
    val reward: String?
)