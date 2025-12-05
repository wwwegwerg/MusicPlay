package com.example.musicplay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.musicplay.auth.AuthApi
import com.example.musicplay.auth.AuthRepository
import com.example.musicplay.auth.AuthResponseDto
import com.example.musicplay.auth.SessionManager
import com.example.musicplay.databinding.ActivityAuthBinding
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val sessionManager by lazy { SessionManager(this) }
    private val authRepository by lazy { AuthRepository(AuthApi.createService()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.loginButton.setOnClickListener { submitCredentials(isRegister = false) }
        binding.registerButton.setOnClickListener { submitCredentials(isRegister = true) }
        if (!authRepository.isConfigured) {
            setInputsEnabled(false)
            binding.statusText.text = getString(R.string.auth_config_missing)
            return
        }
        sessionManager.getToken()?.let { existing ->
            if (existing.isNotBlank()) {
                verifyExistingSession(existing)
            }
        }
    }

    private fun submitCredentials(isRegister: Boolean) {
        val username = binding.usernameInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString()?.trim().orEmpty()
        if (username.length < 3) {
            binding.statusText.text = getString(R.string.auth_validation_username)
            return
        }
        if (password.length < 6) {
            binding.statusText.text = getString(R.string.auth_validation_password)
            return
        }
        showLoading(true, getString(R.string.auth_status_loading))
        lifecycleScope.launch {
            val result = if (isRegister) {
                authRepository.register(username, password)
            } else {
                authRepository.login(username, password)
            }
            result.onSuccess { handleSuccess(it) }
                .onFailure { showError(it) }
        }
    }

    private fun verifyExistingSession(token: String) {
        showLoading(true, getString(R.string.auth_checking_session))
        lifecycleScope.launch {
            val result = authRepository.fetchProfile(token)
            result.onSuccess { user ->
                sessionManager.saveUser(user)
                binding.statusText.text = getString(R.string.auth_status_success)
                openMain()
            }.onFailure { throwable ->
                sessionManager.clearSession()
                val message = throwable.message?.ifBlank { null }
                    ?: getString(R.string.auth_error_unknown)
                showLoading(false, getString(R.string.auth_status_error, message))
            }
        }
    }

    private fun handleSuccess(response: AuthResponseDto) {
        sessionManager.saveAuth(response)
        showLoading(false, getString(R.string.auth_status_success))
        binding.root.postDelayed({ openMain() }, 250)
    }

    private fun showError(throwable: Throwable) {
        val message = throwable.message?.ifBlank { null }
            ?: getString(R.string.auth_error_unknown)
        showLoading(false, getString(R.string.auth_status_error, message))
    }

    private fun showLoading(isLoading: Boolean, statusMessage: String? = null) {
        binding.authProgress.isVisible = isLoading
        setInputsEnabled(!isLoading)
        statusMessage?.let { binding.statusText.text = it }
    }

    private fun setInputsEnabled(enabled: Boolean) {
        binding.usernameInput.isEnabled = enabled
        binding.passwordInput.isEnabled = enabled
        binding.loginButton.isEnabled = enabled
        binding.registerButton.isEnabled = enabled
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, AuthActivity::class.java)
    }
}
