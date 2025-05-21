package com.example.found404.models

open class PostItem(
    open val title: String,
    open val description: String,
    open val date: String,
    open val location: String,
    open val imageUrl: String? = null,
    open val contact: String? = null
)
