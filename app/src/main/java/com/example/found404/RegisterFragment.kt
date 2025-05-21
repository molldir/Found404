package com.example.found404

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.found404.databinding.FragmentRegisterBinding
import com.example.found404.models.AppUser
import com.example.found404.network.RetrofitInstance
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Анимация при открытии фрагмента
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)

        binding.root.startAnimation(fadeIn)
        binding.btnRegister.startAnimation(slideUp)
        binding.goToLoginText.startAnimation(slideUp)

        binding.btnRegister.setOnClickListener {
            // Анимация нажатия кнопки
            val bounceAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
            binding.btnRegister.startAnimation(bounceAnim)

            val name = binding.registerName.editText?.text.toString().trim()
            val email = binding.registerEmail.editText?.text.toString().trim()
            val password = binding.registerPassword.editText?.text.toString().trim()
            val confirmPassword = binding.registerConfirmPassword.editText?.text.toString().trim()

            // Проверка на заполненность полей
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showShakeAnimation()
                showSnackbar("Пожалуйста, заполните все поля", isError = true)
                return@setOnClickListener
            }

            // Проверка на совпадение паролей
            if (password != confirmPassword) {
                showShakeAnimation()
                showSnackbar("Пароли не совпадают", isError = true)
                return@setOnClickListener
            }

            // Дополнительная проверка для валидности email и пароля
            if (!isValidEmail(email)) {
                showShakeAnimation()
                showSnackbar("Неверный формат email", isError = true)
                return@setOnClickListener
            }

            if (password.length < 6) {
                showShakeAnimation()
                showSnackbar("Пароль должен быть не менее 6 символов", isError = true)
                return@setOnClickListener
            }

            // Создание пользователя
            val user = AppUser(id = 0L, name = name, email = email, password = password, profilePhotoUri=null)

            // Отправка данных на сервер
            RetrofitInstance.api.registerUser(user).enqueue(object : Callback<AppUser> {
                override fun onResponse(call: Call<AppUser>, response: Response<AppUser>) {
                    if (response.isSuccessful) {
                        // Сохраняем данные пользователя в SharedPreferences
                        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
                        sharedPreferences.edit().apply {
                            putLong("user_id", response.body()?.id ?: 0)
                            putString("user_email", user.email)
                            putString("user_password", user.password)
                            putBoolean("is_logged_in", true)
                            apply()
                        }

                        showSuccessAnimation()
                        showSnackbar("Регистрация прошла успешно!", isError = false)

                        // Переходим на экран логина
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    } else {
                        showErrorAnimation()
                        showSnackbar("Ошибка регистрации: ${response.message()}", isError = true)
                    }
                }

                override fun onFailure(call: Call<AppUser>, t: Throwable) {
                    showErrorAnimation()
                    showSnackbar("Ошибка подключения: ${t.localizedMessage}", isError = true)
                }
            })
        }

        binding.goToLoginText.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    // Функция для проверки email
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Функция для анимации потряхивания
    private fun showShakeAnimation() {
        val shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        binding.registerName.startAnimation(shake)
        binding.registerEmail.startAnimation(shake)
        binding.registerPassword.startAnimation(shake)
        binding.registerConfirmPassword.startAnimation(shake)
    }

    // Функция для анимации успешной регистрации
    private fun showSuccessAnimation() {
        val scaleUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
        binding.root.startAnimation(scaleUp)
    }

    // Функция для анимации ошибки
    private fun showErrorAnimation() {
        val shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        binding.root.startAnimation(shake)
    }

    // Функция для показа Snackbar с сообщением
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
