package com.example.musicplay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplay.databinding.ActivityJanreBinding
import com.example.musicplay.auth.SessionManager

class Janre : AppCompatActivity() {

    private lateinit var binding: ActivityJanreBinding
    private val sessionManager by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ensureAuthorized()) {
            return
        }
        binding = ActivityJanreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
    }

    private fun setupButtons() {
        binding.button3.setOnClickListener {
            navigateToResult(displayName = getString(R.string.genre_rock), genreQuery = "rock")
        }

        binding.button.setOnClickListener {
            navigateToResult(displayName = getString(R.string.genre_rap), genreQuery = "rap")
        }

        binding.button5.setOnClickListener {
            navigateToResult(displayName = getString(R.string.genre_metal), genreQuery = "metal")
        }

        binding.button4.setOnClickListener {
            navigateToResult(displayName = getString(R.string.genre_indie), genreQuery = "indie")
        }
    }

    private fun navigateToResult(displayName: String, genreQuery: String) {
        startActivity(
            MainWindow.createIntent(
                context = this,
                genreQuery = genreQuery,
                genreTitle = displayName
            )
        )
    }

    private fun ensureAuthorized(): Boolean {
        if (!sessionManager.hasSession()) {
            startActivity(AuthActivity.createIntent(this))
            finish()
            return false
        }
        return true
    }
}
