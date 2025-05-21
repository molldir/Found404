package com.example.found404.util

import androidx.recyclerview.widget.DiffUtil
import com.example.found404.models.ItemDTO

class ItemDiffCallback : DiffUtil.ItemCallback<ItemDTO>() {
    override fun areItemsTheSame(oldItem: ItemDTO, newItem: ItemDTO) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ItemDTO, newItem: ItemDTO) = oldItem == newItem
}
