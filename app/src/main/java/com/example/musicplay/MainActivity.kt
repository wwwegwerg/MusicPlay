package com.example.musicplay

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.musicplay.auth.SessionManager

class MainActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ensureAuthorized()) {
            return
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val button = findViewById<Button>(R.id.btnGoToSecond)
        val button2 = findViewById<Button>(R.id.exit)
        button.setOnClickListener {
            val intent = Intent(this, Janre::class.java)
            startActivity(intent)
        }
        button2.setOnClickListener {
            sessionManager.clearSession()
            finishAffinity()
        }
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
