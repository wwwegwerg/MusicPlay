package com.example.musicplay

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.musicplay.auth.SessionManager
import com.example.musicplay.databinding.ActivityMainWindowBinding
import com.example.musicplay.jamendo.JamendoApi
import com.example.musicplay.jamendo.JamendoRepository
import com.example.musicplay.jamendo.JamendoTrack
import kotlinx.coroutines.launch

class MainWindow : AppCompatActivity() {

    private lateinit var binding: ActivityMainWindowBinding
    private val sessionManager by lazy { SessionManager(this) }
    private val repository by lazy {
        JamendoRepository(JamendoApi.service, BuildConfig.JAMENDO_CLIENT_ID)
    }
    private var selectedGenre: String = ""
    private var currentTrack: JamendoTrack? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ensureAuthorized()) {
            return
        }
        enableEdgeToEdge()
        binding = ActivityMainWindowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        selectedGenre = intent.getStringExtra(EXTRA_GENRE_QUERY).orEmpty()
        val genreTitle =
            intent.getStringExtra(EXTRA_GENRE_TITLE) ?: getString(R.string.genre_unknown)
        binding.genreValue.text = genreTitle
        binding.nextTrackButton.setOnClickListener { fetchTrack() }
        binding.playIcon.setOnClickListener { handlePlayIconClick() }
        binding.checkAnswerButton.setOnClickListener { checkGuess() }
        loadCover(null)
        setPlaybackEnabled(false)
        binding.artistValue.text = getString(R.string.artist_unknown)
        fetchTrack()
    }

    private fun fetchTrack() {
        binding.trackProgress.isVisible = true
        binding.trackStatusText.text = getString(R.string.jamendo_loading)
        if (selectedGenre.isBlank()) {
            binding.trackProgress.isVisible = false
            binding.trackStatusText.text = getString(R.string.genre_unknown)
            return
        }
        if (BuildConfig.JAMENDO_CLIENT_ID.isBlank()) {
            binding.trackProgress.isVisible = false
            binding.trackStatusText.text = getString(R.string.jamendo_client_missing)
            return
        }
        lifecycleScope.launch {
            val result = repository.fetchTrackForGenre(selectedGenre)
            binding.trackProgress.isVisible = false
            result.onSuccess { track ->
                currentTrack = track
                binding.resultText.text = ""
                binding.guessInput.text?.clear()
                updateReadyStatus(track)
                loadCover(track.image)
                preparePlayer(track.audio)
            }.onFailure { throwable ->
                binding.trackStatusText.text = getString(
                    R.string.jamendo_error,
                    throwable.message ?: "unknown"
                )
                binding.artistValue.text = getString(R.string.artist_unknown)
                loadCover(null)
                releasePlayer()
            }
        }
    }

    private fun preparePlayer(audioUrl: String?) {
        releasePlayer()
        if (audioUrl.isNullOrBlank()) {
            setPlaybackEnabled(false)
            binding.trackStatusText.text = getString(R.string.jamendo_no_audio)
            return
        }
        setPlaybackEnabled(false)
        binding.trackStatusText.text = getString(R.string.preparing_snippet)
        val player = MediaPlayer()
        mediaPlayer = player
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        try {
            player.setDataSource(audioUrl)
        } catch (t: Throwable) {
            binding.trackStatusText.text = getString(
                R.string.jamendo_error,
                t.message ?: "player"
            )
            releasePlayer()
            return
        }
        player.setOnPreparedListener {
            setPlaybackEnabled(true)
        }
        player.setOnCompletionListener {
            updateReadyStatus()
        }
        player.setOnErrorListener { _, _, _ ->
            releasePlayer()
            true
        }
        player.prepareAsync()
    }

    private fun togglePlayback() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            binding.trackStatusText.text = getString(R.string.play_snippet)
        } else {
            player.start()
            binding.trackStatusText.text = getString(R.string.pause_snippet)
        }
    }

    private fun checkGuess() {
        val track = currentTrack ?: return
        val guess = binding.guessInput.text?.toString()?.trim().orEmpty()
        if (guess.isBlank()) {
            binding.resultText.text = getString(R.string.guess_required)
            return
        }
        val answer = track.name?.trim().orEmpty()
        if (guess.equals(answer, ignoreCase = true)) {
            binding.resultText.text = getString(R.string.guess_correct, answer)
        } else {
            binding.resultText.text =
                getString(R.string.guess_wrong, if (answer.isBlank()) "???" else answer)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        setPlaybackEnabled(false)
        binding.trackStatusText.text = getString(R.string.play_snippet)
    }

    private fun loadCover(imageUrl: String?) {
        val placeholder = R.drawable.icon
        if (imageUrl.isNullOrBlank()) {
            binding.coverImage.setImageResource(placeholder)
            return
        }
        Glide.with(this)
            .load(imageUrl)
            .placeholder(placeholder)
            .error(placeholder)
            .centerCrop()
            .into(binding.coverImage)
    }

    private fun setPlaybackEnabled(enabled: Boolean) {
        binding.playIcon.isEnabled = enabled
        binding.playIcon.alpha = if (enabled) 1f else 0.5f
    }

    private fun updateReadyStatus(track: JamendoTrack? = currentTrack) {
        val artistName =
            track?.artistName?.ifBlank { null } ?: getString(R.string.artist_unknown)
        binding.artistValue.text = artistName
        binding.trackStatusText.text = getString(R.string.jamendo_ready, artistName)
    }

    private fun handlePlayIconClick() {
        if (!binding.playIcon.isEnabled) return
        val player = mediaPlayer ?: return
        togglePlayback()
    }

    companion object {
        const val EXTRA_GENRE_QUERY = "EXTRA_GENRE_QUERY"
        const val EXTRA_GENRE_TITLE = "EXTRA_GENRE_TITLE"

        fun createIntent(context: Context, genreQuery: String, genreTitle: String): Intent =
            Intent(context, MainWindow::class.java).apply {
                putExtra(EXTRA_GENRE_QUERY, genreQuery)
                putExtra(EXTRA_GENRE_TITLE, genreTitle)
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
