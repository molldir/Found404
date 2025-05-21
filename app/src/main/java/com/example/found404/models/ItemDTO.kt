package com.example.found404.models

import android.net.Uri
import com.google.gson.annotations.SerializedName

data class ItemDTO(
    val id: Long?,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val status: String, // "lost" или "found"
    val location: String,
    val reward: String?,
    val dateCreated: String?,
    val userId: Long
)
