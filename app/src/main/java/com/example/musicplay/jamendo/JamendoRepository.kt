package com.example.musicplay.jamendo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JamendoRepository(
    private val service: JamendoService,
    private val clientId: String
) {
    suspend fun fetchTrackForGenre(genre: String): Result<JamendoTrack> =
        withContext(Dispatchers.IO) {
            if (clientId.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("Jamendo client id is missing")
                )
            }
            if (genre.isBlank()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Genre is not specified")
                )
            }
            return@withContext try {
                val response = service.getTracks(
                    clientId = clientId,
                    genre = genre
                )
                val track = response.results.randomOrNull()
                    ?: throw IllegalStateException("Jamendo returned empty result")
                Result.success(track)
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }
}
