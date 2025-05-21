package com.example.found404

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.found404.databinding.FragmentLoginBinding
import com.example.found404.models.LoginRequest
import com.example.found404.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Анимация появления
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)

        binding.root.startAnimation(fadeIn)
        binding.btnLogin.startAnimation(slideUp)
        binding.tvRegister.startAnimation(slideUp)

        binding.btnLogin.setOnClickListener {
            // Анимация нажатия кнопки
            val bounceAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
            binding.btnLogin.startAnimation(bounceAnim)

            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val loginRequest = LoginRequest(email, password)

                RetrofitInstance.api.loginUser(loginRequest)
                    .enqueue(object : Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.isSuccessful) {
                                val token = response.body()

                                if (!token.isNullOrEmpty()) {
                                    val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                    sharedPref.edit()
                                        .putString("jwt_token", token)
                                        .putString("email", email)
                                        .apply()

                                    val successAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
                                    binding.root.startAnimation(successAnim)

                                    RetrofitInstance.api.getUserByEmail(email)
                                        .enqueue(object : Callback<com.example.found404.models.AppUser> {
                                            override fun onResponse(
                                                call: Call<com.example.found404.models.AppUser>,
                                                response: Response<com.example.found404.models.AppUser>
                                            ) {
                                                if (response.isSuccessful) {
                                                    val user = response.body()
                                                    if (user != null) {
                                                        sharedPref.edit()
                                                            .putLong("user_id", user.id)
                                                            .putString("email", email)
                                                            .putString("jwt_token", token)
                                                            .apply()

                                                        if (findNavController().currentDestination?.id == R.id.loginFragment) {
                                                            findNavController().navigate(R.id.action_loginFragment_to_postsFragment)
                                                        }
                                                    } else {
                                                        showErrorAnimation()
                                                    }
                                                } else {
                                                    showErrorAnimation()
                                                }
                                            }

                                            override fun onFailure(call: Call<com.example.found404.models.AppUser>, t: Throwable) {
                                                showErrorAnimation()
                                            }
                                        })
                                } else {
                                    showErrorAnimation()
                                }
                            } else {
                                showErrorAnimation()
                            }
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                            showErrorAnimation()
                        }
                    })
            } else {
                // Анимация при пустых полях
                val shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
                if (email.isEmpty()) binding.emailEditText.startAnimation(shake)
                if (password.isEmpty()) binding.passwordEditText.startAnimation(shake)
            }
        }

        // Переход на экран регистрации
        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun showErrorAnimation() {
        val shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        binding.root.startAnimation(shake)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
