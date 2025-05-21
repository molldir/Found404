package com.example.found404.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.found404.R
import com.example.found404.models.ItemDTO
import com.example.found404.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log

class CustomAdapter(
    private var items: List<ItemDTO>,
    private val userId: Long = 0L,
    private var searchQuery: String? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_LOST = 0
        private const val TYPE_FOUND = 1
        private const val TYPE_MY_POST = 2
    }

    fun updateItems(newItems: List<ItemDTO>, query: String? = null) {
        this.items = newItems
        this.searchQuery = query
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].status.lowercase()) {
            "lost" -> TYPE_LOST
            "found" -> TYPE_FOUND
            else -> TYPE_MY_POST
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_card, parent, false)

        return when (viewType) {
            TYPE_LOST -> {
                val container = view.findViewById<LinearLayout>(R.id.container)
                inflater.inflate(R.layout.item_lost, container, true)
                LostViewHolder(view)
            }
            TYPE_FOUND -> {
                val container = view.findViewById<LinearLayout>(R.id.container)
                inflater.inflate(R.layout.item_found, container, true)
                FoundViewHolder(view)
            }
            TYPE_MY_POST -> {
                val container = view.findViewById<LinearLayout>(R.id.container)
                inflater.inflate(R.layout.item_my_posts, container, true)
                MyPostViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        Log.d("CustomAdapter", "Binding item: $item")
        when (holder) {
            is LostViewHolder -> holder.bind(item, searchQuery)
            is FoundViewHolder -> holder.bind(item, searchQuery)
            is MyPostViewHolder -> holder.bind(item, userId)
        }
    }

    override fun getItemCount(): Int = items.size

    // Highlight text
    private fun highlightText(text: String, query: String?): SpannableString {
        val spannable = SpannableString(text)
        if (!query.isNullOrEmpty()) {
            val startIndex = text.indexOf(query, ignoreCase = true)
            if (startIndex >= 0) {
                spannable.setSpan(
                    ForegroundColorSpan(Color.RED),
                    startIndex,
                    startIndex + query.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return spannable
    }

    // ViewHolder classes
    inner class LostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.lost_title)
        private val description: TextView = itemView.findViewById(R.id.lost_description)
        private val date: TextView = itemView.findViewById(R.id.lost_date)
        private val location: TextView = itemView.findViewById(R.id.lost_location)
        private val image: ImageView = itemView.findViewById(R.id.lost_image)

        fun bind(item: ItemDTO, query: String?) {
            title.text = highlightText(item.title, query)
            description.text = highlightText(item.description, query)
            date.text = highlightText(item.dateCreated ?: "", query)
            location.text = highlightText(item.location, query)
            loadImage(item.imageUrl)
        }

        private fun loadImage(imageUrl: String?) {
            if (!imageUrl.isNullOrEmpty()) {
                val fullUrl = "http://172.20.10.13:8081$imageUrl"
                Glide.with(itemView)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_person)
                    .into(image)
                image.visibility = View.VISIBLE
            } else {
                image.visibility = View.GONE
            }
        }
    }

    inner class FoundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.found_title)
        private val description: TextView = itemView.findViewById(R.id.found_description)
        private val date: TextView = itemView.findViewById(R.id.found_date)
        private val location: TextView = itemView.findViewById(R.id.found_location)
        private val image: ImageView = itemView.findViewById(R.id.found_image)

        fun bind(item: ItemDTO, query: String?) {
            title.text = highlightText(item.title, query)
            description.text = highlightText(item.description, query)
            date.text = highlightText(item.dateCreated ?: "", query)
            location.text = highlightText(item.location, query)
            loadImage(item.imageUrl)
        }

        private fun loadImage(imageUrl: String?) {
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(itemView)
                    .load(imageUrl)
                    .into(image)
                image.visibility = View.VISIBLE
            } else {
                image.visibility = View.GONE
            }
        }
    }

    inner class MyPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.post_title)
        private val deleteButton: TextView = itemView.findViewById(R.id.btn_delete)

        fun bind(item: ItemDTO, userId: Long) {
            title.text = item.title
            deleteButton.setOnClickListener {
                if (item.id != null) {
                    showDeleteDialog(item, userId)
                } else {
                    Toast.makeText(itemView.context, "Item ID is missing", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun showDeleteDialog(item: ItemDTO, userId: Long) {
            val context = itemView.context
            AlertDialog.Builder(context)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteItem(item, userId)
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
        }

        private fun deleteItem(item: ItemDTO, userId: Long) {
            val itemId = item.id ?: return
            RetrofitInstance.api.deleteItem(itemId, userId).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        (itemView.parent as? RecyclerView)?.adapter?.notifyDataSetChanged()
                        Toast.makeText(itemView.context, "Post deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(itemView.context, "Delete failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(itemView.context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}