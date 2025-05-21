package com.example.found404

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.found404.adapter.CustomAdapter
import com.example.found404.databinding.FragmentPostsBinding
import com.example.found404.models.ItemDTO
import com.example.found404.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class PostsFragment : Fragment(R.layout.fragment_posts) {
    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CustomAdapter
    private val allItems = mutableListOf<ItemDTO>()
    private var currentStatus = "LOST"
    private var selectedLocation: String? = null
    private var selectedDate: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPostsBinding.bind(view)

        // Инициализация адаптера
        adapter = CustomAdapter(emptyList())
        binding.recyclerPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPosts.adapter = adapter

        // Настройка фильтров
        setupSearchView()
        setupFilterButtons()

        // Загрузка данных
        fetchItemsFromServer(currentStatus)
    }

    // Переключение между LOST и FOUND
    private fun togglePostType() {
        currentStatus = if (currentStatus == "LOST") {
            binding.btnSwitchPostType.text = "Switch to Lost"
            binding.tvPostType.text = "Found Items"
            "FOUND"
        } else {
            binding.btnSwitchPostType.text = "Switch to Found"
            binding.tvPostType.text = "Lost Items"
            "LOST"
        }
        filterItems(null)
    }

    // Получение данных с сервера
    private fun fetchItemsFromServer(status: String) {
        RetrofitInstance.api.getItems(status).enqueue(object : Callback<List<ItemDTO>> {
            override fun onResponse(call: Call<List<ItemDTO>>, response: Response<List<ItemDTO>>) {
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    allItems.clear()
                    allItems.addAll(items)
                    filterItems(null) // Фильтруем по текущему статусу
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ItemDTO>>, t: Throwable) {
                Toast.makeText(requireContext(), "Сетевая ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Фильтрация по статусу и запросу
    private fun filterItems(query: String?) {
        val filtered = allItems.filter { item ->
            val matchesQuery = query.isNullOrEmpty() ||
                    item.title.contains(query!!, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true) ||
                    item.location.contains(query, ignoreCase = true)
            item.status.equals(currentStatus, ignoreCase = true) && matchesQuery
        }
        adapter.updateItems(filtered)
        Log.d("PostsFragment", "Отображено: ${filtered.size} постов")
    }

    // Настройка поиска
    private fun setupSearchView() {
        binding.searchField.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { filterItems(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterItems(it) }
                return true
            }
        })
    }

    // Настройка кнопок фильтрации
    private fun setupFilterButtons() {
        binding.btnDateFilter.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnLocationFilter.setOnClickListener {
            showLocationDialog()
        }

        binding.btnResetFilters.setOnClickListener {
            selectedLocation = null
            selectedDate = null
            binding.searchField.setQuery("", false)
            fetchItemsFromServer(currentStatus)
            Toast.makeText(requireContext(), "Фильтры сброшены", Toast.LENGTH_SHORT).show()
        }
    }

    // Диалог выбора даты
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = "$year-${month + 1}-$dayOfMonth" // yyyy-MM-dd
                filterItemsByDate(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    // Фильтрация по дате
    private fun filterItemsByDate(date: String) {
        selectedDate = date
        val filtered = allItems.filter { item ->
            item.dateCreated == date && item.status == currentStatus
        }
        adapter.updateItems(filtered)
        Log.d("PostsFragment", "Найдено по дате: $date — ${filtered.size} постов")
    }

    // Диалог выбора места
    private fun showLocationDialog() {
        val locations = arrayOf("Library", "Main Building", "Bayzak", "Sport Complex", "Other")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Выберите место")
        builder.setItems(locations) { _, which ->
            val selectedLocation = locations[which]
            filterItemsByLocation(selectedLocation)
        }
        builder.create().show()
    }

    // Фильтрация по месту
    private fun filterItemsByLocation(location: String) {
        selectedLocation = location
        val filtered = allItems.filter { item ->
            item.location == location && item.status == currentStatus
        }
        adapter.updateItems(filtered)
        Log.d("PostsFragment", "Найдено по месту: $location — ${filtered.size} постов")
    }

    // Сброс фильтров
    private fun resetFilters() {
        selectedLocation = null
        selectedDate = null
        binding.searchField.setQuery("", false)
        fetchItemsFromServer(currentStatus)
        Toast.makeText(requireContext(), "Фильтры сброшены", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}