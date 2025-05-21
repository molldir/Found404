package com.example.found404

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.found404.databinding.FragmentAddBinding
import com.example.found404.models.ItemDTO
import com.example.found404.network.ApiService
import com.example.found404.network.RetrofitInstance
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException

class AddItemFragment : Fragment(R.layout.fragment_add) {
    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private var selectedImageUri: Uri? = null
    private var currentPhotoUri: Uri? = null
    private var selectedLocation: String = "Library"
    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)

        setupApiService()
        setupLocationSpinner()
        initPermissionLauncher()
        initImagePicker()
        setupAnimations()
        resetUI()

        binding.btnUploadPhoto.setOnClickListener {
            animateButtonClick(it)
            requestPermissionsAndOpenCamera()
        }

        binding.btnSubmit.setOnClickListener {
            animateButtonClick(it)
            submitItem()
        }

        return binding.root
    }

    private fun setupApiService() {
        apiService = RetrofitInstance.api
    }

    private fun resetUI() {
        binding.etTitle.setText("")
        binding.etDescription.setText("")
        binding.spinnerLocation.setSelection(0)
        binding.radioLost.isChecked = true
        binding.imagePreview.setImageResource(android.R.color.transparent)
        binding.imagePreview.visibility = View.GONE
        selectedImageUri = null
    }

    private fun setupLocationSpinner() {
        val locations = listOf("Library", "Main Building", "Bayzak", "Sport Complex", "Other")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, locations)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerLocation.adapter = adapter
        binding.spinnerLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                selectedLocation = locations[pos]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun initPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) openCamera()
            else showSnackbar("Permissions are needed to take a photo", true)
        }
    }

    private fun requestPermissionsAndOpenCamera() {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun openCamera() {
        val imageFile = createImageFile()
        val photoUri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.provider", imageFile
        )
        currentPhotoUri = photoUri

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        imagePickerLauncher.launch(cameraIntent)
    }

    private fun createImageFile(): File {
        val storageDir = requireContext().cacheDir
        return File.createTempFile("IMG_${System.currentTimeMillis()}", ".jpg", storageDir)
    }

    private fun initImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: currentPhotoUri
                uri?.let {
                    selectedImageUri = it
                    binding.imagePreview.apply {
                        setImageURI(it)
                        visibility = View.VISIBLE
                        startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up))
                    }
                }
            }
        }
    }

    // Внутри AddItemFragment
    private fun submitItem() {
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getLong("user_id", 0L)
        val reward = binding.etReward.text.toString()
        if (userId == 0L) {
            showSnackbar("Please login first", true)
            return
        }

        val title = binding.etTitle.text.toString().ifBlank {
            showSnackbar("Title is required", true)
            showShakeAnimation(binding.etTitle)
            return
        }

        val description = binding.etDescription.text.toString()
        val status = if (binding.radioLost.isChecked) "LOST" else "FOUND"
        val category = "General"

        // Если есть картинка — загружаем, если нет — просто переходим
        selectedImageUri?.let { uri ->
            uploadItem(userId, title, description, status, category, "IITU $selectedLocation", reward, uri)
        } ?: run {
            showSnackbar("Item saved without image", false)
            findNavController().navigate(R.id.action_addItemFragment_to_postsFragment)
            (requireActivity() as MainActivity).startSearchService(status)  // Вызов метода запуска foreground сервиса
        }
    }

    private fun uploadItem(
        userId: Long, title: String, description: String, status: String, category: String,
        location: String, reward: String, imageUri: Uri
    ) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val imageBytes = inputStream?.readBytes() ?: throw IOException("Failed to read image")
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), imageBytes)
            val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestFile)

            apiService.addItem(
                title.toRequestBody(), description.toRequestBody(), status.toRequestBody(),
                category.toRequestBody(), location.toRequestBody(), userId.toString().toRequestBody(),
                reward?.toRequestBody(), imagePart
            ).enqueue(object : Callback<ItemDTO> {
                override fun onResponse(call: Call<ItemDTO>, response: Response<ItemDTO>) {
                    if (response.isSuccessful) {
                        showSuccessAnimation()
                        showSnackbar("Item added successfully", false)
                        findNavController().navigate(R.id.action_addItemFragment_to_postsFragment)
                    } else {
                        showErrorAnimation()
                        showSnackbar("Error: ${response.message()}", true)
                    }
                }

                override fun onFailure(call: Call<ItemDTO>, t: Throwable) {
                    showErrorAnimation()
                    showSnackbar("Network error: ${t.message}", true)
                }
            })
        } catch (e: IOException) {
            showSnackbar("Failed to read image file", true)
        }
    }

    private fun showSnackbar(message: String, isError: Boolean) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).apply {
            view.setBackgroundColor(
                if (isError) resources.getColor(R.color.error_color)
                else resources.getColor(R.color.green_500)
            )
            setTextColor(resources.getColor(R.color.white))
            show()
        }
    }

    private fun showShakeAnimation(view: View) {
        val shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        view.startAnimation(shake)
    }

    private fun showSuccessAnimation() {
        val scaleUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
        binding.formContainer.startAnimation(scaleUp)
    }

    private fun showErrorAnimation() {
        val shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        binding.formContainer.startAnimation(shake)
    }

    private fun setupAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        binding.root.startAnimation(fadeIn)
        binding.formContainer.startAnimation(slideUp)
    }

    private fun animateButtonClick(view: View) {
        val bounceAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        view.startAnimation(bounceAnim)
    }

    private fun String.toRequestBody() =
        RequestBody.create("text/plain".toMediaTypeOrNull(), this)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
