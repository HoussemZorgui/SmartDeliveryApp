package com.example.smartdeliveryapp.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.smartdeliveryapp.R
import com.example.smartdeliveryapp.databinding.ActivityRegisterBinding
import com.example.smartdeliveryapp.presentation.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    // Default selected role
    private var selectedRole = "client"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Role card selection logic
        binding.cardClient.setOnClickListener { selectRole("client") }
        binding.cardDriver.setOnClickListener { selectRole("driver") }

        // Sign Up button
        binding.btnSignUp.setOnClickListener {
            val username = binding.usernameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            when {
                username.isEmpty() -> showError("Veuillez saisir votre nom d'utilisateur")
                email.isEmpty() -> showError("Veuillez saisir votre email")
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    showError("Email invalide")
                password.length < 6 -> showError("Le mot de passe doit contenir au moins 6 caractères")
                else -> viewModel.register(username, email, password, selectedRole)
            }
        }

        // Back to Login
        binding.btnBackToLogin.setOnClickListener { finish() }

        // Observe auth state
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSignUp.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "✅ Compte créé avec succès !", Toast.LENGTH_SHORT).show()
                    // If driver, redirect to KYC first
                    if (selectedRole == "driver") {
                        startActivity(Intent(this, com.example.smartdeliveryapp.presentation.kyc.KycActivity::class.java))
                    } else {
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                    finishAffinity()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSignUp.isEnabled = true
                    showError(state.message)
                }
                else -> {}
            }
        }
    }

    private fun selectRole(role: String) {
        selectedRole = role
        if (role == "client") {
            binding.cardClient.background = ContextCompat.getDrawable(this, R.drawable.bg_role_selected)
            binding.tvClientLabel.setTextColor(ContextCompat.getColor(this, R.color.accent_volt))
            binding.cardDriver.background = ContextCompat.getDrawable(this, R.drawable.bg_role_unselected)
            binding.tvDriverLabel.setTextColor(ContextCompat.getColor(this, R.color.text_hint))
        } else {
            binding.cardDriver.background = ContextCompat.getDrawable(this, R.drawable.bg_role_selected)
            binding.tvDriverLabel.setTextColor(ContextCompat.getColor(this, R.color.accent_volt))
            binding.cardClient.background = ContextCompat.getDrawable(this, R.drawable.bg_role_unselected)
            binding.tvClientLabel.setTextColor(ContextCompat.getColor(this, R.color.text_hint))
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, "❌ $message", Toast.LENGTH_LONG).show()
    }
}
