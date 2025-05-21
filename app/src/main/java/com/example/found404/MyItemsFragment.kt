package com.example.found404

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.found404.adapter.CustomAdapter
import com.example.found404.databinding.FragmentMyBinding
import com.example.found404.models.ItemDTO
import com.example.found404.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyItemsFragment : Fragment(R.layout.fragment_my) {
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CustomAdapter
    private var userId: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyBinding.bind(view)

        // Получаем userId из SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = sharedPref.getLong("user_id", 0L)
        if (userId == 0L) {
            Toast.makeText(requireContext(), "User ID не найден", Toast.LENGTH_SHORT).show()
            return
        }

        // Инициализация адаптера
        adapter = CustomAdapter(emptyList(), userId)
        binding.recyclerMyItems.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMyItems.adapter = adapter
        binding.recyclerMyItems.itemAnimator = DefaultItemAnimator().apply {
            addDuration = 300
            removeDuration = 300
        }

        loadMyItems(userId)
    }

    private fun loadMyItems(userId: Long) {
        RetrofitInstance.api.getItemsByUser(userId).enqueue(object : Callback<List<ItemDTO>> {
            override fun onResponse(call: Call<List<ItemDTO>>, response: Response<List<ItemDTO>>) {
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    adapter.updateItems(items)
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки объявлений", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ItemDTO>>, t: Throwable) {
                Toast.makeText(requireContext(), "Сетевая ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}