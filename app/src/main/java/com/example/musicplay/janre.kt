package com.example.musicplay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplay.databinding.ActivityJanreBinding  // Добавьте этот импорт

class Janre : AppCompatActivity() {

    private lateinit var binding: ActivityJanreBinding  // Исправлено!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJanreBinding.inflate(layoutInflater)  // Исправлено!
        setContentView(binding.root)

        setupButtons()
    }
    private fun setupButtons() {
        // Кнопка 1
        binding.button3.setOnClickListener {
            navigateToResult("Вы выбрали: Опция 1 ✅")
        }

        // Кнопка 2
        binding.button.setOnClickListener {
            navigateToResult("Вы выбрали: Опция 2 🔥")
        }

        // Кнопка 3
        binding.button5.setOnClickListener {
            navigateToResult("Вы выбрали: Опция 3 ⭐")
        }

        // Кнопка 4
        binding.button4.setOnClickListener {
            navigateToResult("Вы выбрали: Опция 4 🎯")
        }
    }

    private fun navigateToResult(buttonText: String) {
        val intent = Intent(this, MainWindow::class.java).apply {
            putExtra("SELECTED_TEXT", buttonText)
        }
        startActivity(intent)
    }
}