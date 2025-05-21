package com.example.found404.data

import com.example.found404.models.FoundItem
import com.example.found404.models.LostItem
import com.example.found404.models.MyPost

object ItemsRepository {
    val lostItems = mutableListOf<LostItem>()
    val myPosts = mutableListOf<MyPost>()
    val foundItems=mutableListOf<FoundItem>()
}
