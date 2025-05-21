package com.example.found404

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.found404.R
import com.example.found404.databinding.FragmentProfileBinding
import com.example.found404.models.ProfileUpdateDto
import com.example.found404.network.ApiService
import com.example.found404.network.RetrofitInstance
import com.google.android.material.snackbar.Snackbar
import android.view.animation.AnimationUtils
import com.example.found404.models.AppUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.Manifest
import java.io.File
import androidx.core.content.FileProvider

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private val apiService: ApiService by lazy {
        RetrofitInstance.api
    }
    private var selectedPhotoPath: String? = null // Локальный путь к фото
    private var profilePhotoUri: String? = null  // URI от сервера (если есть)
    private var photoUri: Uri? = null // URI для фото с камеры

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация обработчика разрешений
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                openCamera()
            } else {
                showSnackbar("Permission required to access camera", isError = true)
            }
        }

        // Инициализация запуска камеры
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleCameraResult(result.resultCode, result.data)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        // Убираем hardcoded src из XML
        binding.imgProfilePhoto.setImageResource(0)

        // Анимация
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        binding.root.startAnimation(fadeIn)
        binding.profileCard.startAnimation(slideUp)

        // Загрузка данных
        loadUserData()

        // Клик по фото
        binding.imgProfilePhoto.setOnClickListener {
            val bounceAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
            it.startAnimation(bounceAnim)
            onPhotoClick()
        }

        // Сохранение профиля
        binding.btnSaveProfile.setOnClickListener {
            saveProfileToServer()
        }

        // Выход
        binding.btnLogout.setOnClickListener {
            logoutUser()
        }

        // Запрет изменения email
        binding.etEmail.setOnClickListener {
            Log.d("ProfileFragment", "User tried to click on the email field (which cannot be modified).")
            shakeAnimation()
            showSnackbar("Email cannot be changed.", isError = true)
        }
    }

    // 🔁 Загрузка данных профиля
    private fun loadUserData() {
        apiService.getProfile().enqueue(object : Callback<AppUser> {
            override fun onResponse(call: Call<AppUser>, response: Response<AppUser>) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    binding.etFirstName.setText(profile?.name)
                    binding.etEmail.setText(profile?.email)
                    binding.etEmail.isEnabled = false
                    profilePhotoUri = profile?.profilePhotoUri

                    // Пытаемся загрузить фото (сначала локальное, потом с сервера)
                    val localPhotoPath = context?.getSharedPreferences("user_data", Context.MODE_PRIVATE)
                        ?.getString("profile_photo_path", null)

                    if (!localPhotoPath.isNullOrEmpty()) {
                        // Загружаем локальное фото
                        Glide.with(requireContext())
                            .load(localPhotoPath)
                            .placeholder(R.drawable.ic_person)
                            .into(binding.imgProfilePhoto)
                    } else if (!profilePhotoUri.isNullOrEmpty()) {
                        // Загружаем фото с сервера
                        Glide.with(requireContext())
                            .load(profilePhotoUri)
                            .placeholder(R.drawable.ic_person)
                            .into(binding.imgProfilePhoto)
                    } else {
                        binding.imgProfilePhoto.setImageResource(R.drawable.ic_person)
                    }
                } else {
                    Toast.makeText(requireContext(), "Error loading user data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AppUser>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 📷 Открытие камеры
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoUri = createImageFileUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        cameraLauncher.launch(intent)
    }

    // 📁 Создание файла для фото
    private fun createImageFileUri(): Uri {
        val fileName = "profile_${System.currentTimeMillis()}"
        val file = File(requireContext().filesDir, "$fileName.jpg")
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().applicationContext.packageName}.fileprovider",
            file
        )
    }

    // 🔍 Обработка нажатия на фото
    private fun onPhotoClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }
    }

    // 📸 Обработка результата камеры
    private fun handleCameraResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            photoUri?.let { uri ->
                // Сохраняем фото локально
                val filePath = saveImageToLocalStorage(uri)
                selectedPhotoPath = filePath
                // Обновляем ImageView
                Glide.with(requireContext())
                    .load(filePath)
                    .into(binding.imgProfilePhoto)
                // Сохраняем путь в SharedPreferences
                context?.getSharedPreferences("user_data", Context.MODE_PRIVATE)?.edit()
                    ?.putString("profile_photo_path", filePath)
                    ?.apply()
                Toast.makeText(requireContext(), "Photo saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 💾 Локальное сохранение фото
    private fun saveImageToLocalStorage(uri: Uri): String {
        val resolver = requireContext().contentResolver
        val inputStream = resolver.openInputStream(uri) ?: return ""
        val file = File(requireContext().filesDir, "profile_${System.currentTimeMillis()}.jpg")
        val outputStream = requireContext().openFileOutput(file.name, Context.MODE_PRIVATE)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return file.absolutePath
    }

    // 🚪 Выход
    private fun logoutUser() {
        requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE).edit()
            .putBoolean("is_logged_in", false)
            .apply()
        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }

    // ✅ Сохранение имени на сервере
    private fun saveProfileToServer() {
        val username = binding.etFirstName.text.toString()
        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter valid details", Toast.LENGTH_SHORT).show()
            return
        }

        val updateDto = ProfileUpdateDto(name = username)
        apiService.updateProfile(updateDto).enqueue(object : Callback<AppUser> {
            override fun onResponse(call: Call<AppUser>, response: Response<AppUser>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error saving profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AppUser>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to save profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 🎯 Запрос разрешения на камеру
    private fun requestCameraPermission() {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // 🎨 Вспомогательные методы
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showSnackbar(message: String, isError: Boolean = false) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).apply {
            view.setBackgroundColor(
                if (isError) ContextCompat.getColor(requireContext(), R.color.error_color)
                else ContextCompat.getColor(requireContext(), R.color.green_500)
            )
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            show()
        }
    }

    private fun shakeAnimation() {
        val shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        binding.etEmail.startAnimation(shake)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}