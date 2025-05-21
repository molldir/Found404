package com.example.found404.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.found404.models.ItemDTO
import com.example.found404.network.ApiService
import com.example.found404.network.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostsViewModel : ViewModel() {
    private val api: ApiService = RetrofitInstance.api

    private val _items = MutableLiveData<List<ItemDTO>>()
    val items: LiveData<List<ItemDTO>> = _items

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var postType: String = "lost"
    private var searchQuery: String = ""
    private var locationFilter: String = ""
    private var dateFilter: String = ""

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            try {
                val call = when (postType) {
                    "lost" -> api.getLostItems()
                    "found" -> api.getFoundItems()
                    else -> api.getLostItems()
                }

                call.enqueue(object : Callback<List<ItemDTO>> {
                    override fun onResponse(
                        call: Call<List<ItemDTO>>,
                        response: Response<List<ItemDTO>>
                    ) {
                        if (response.isSuccessful) {
                            val filteredItems = response.body()?.filter { item ->
                                (dateFilter.isEmpty() || item.dateCreated?.contains(dateFilter) == true) &&
                                        (locationFilter.isEmpty() || item.location.contains(
                                            locationFilter,
                                            true
                                        )) &&
                                        (searchQuery.isEmpty() ||
                                                item.title.contains(searchQuery, true) ||
                                                item.description.contains(searchQuery, true))
                            }
                            _items.value = filteredItems ?: emptyList()
                        } else {
                            _error.value = "Error: ${response.code()}"
                        }
                    }

                    override fun onFailure(call: Call<List<ItemDTO>>, t: Throwable) {
                        _error.value = "Network error: ${t.message}"
                    }
                })
            } catch (e: Exception) {
                _error.value = "Exception: ${e.message}"
            }
        }
    }

    fun setPostType(type: String) {
        postType = type
        loadItems()
    }

    fun setSearchQuery(query: String) {
        searchQuery = query
        loadItems()
    }

    fun setLocationFilter(location: String) {
        locationFilter = location
        loadItems()
    }
}
