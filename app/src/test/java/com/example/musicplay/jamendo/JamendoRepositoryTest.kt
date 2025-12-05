package com.example.musicplay.jamendo

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JamendoRepositoryTest {

    @Test
    fun fetchTrackReturnsResultFromService() = runTest {
        val fakeService = FakeJamendoService()
        val repository = JamendoRepository(fakeService, clientId = "client")

        val result = repository.fetchTrackForGenre("rock")

        assertTrue(result.isSuccess)
        assertEquals("rock", fakeService.lastGenre)
        assertEquals(fakeService.response.results.first(), result.getOrNull())
    }

    @Test
    fun fetchTrackFailsWhenClientIdMissing() = runTest {
        val repository = JamendoRepository(FakeJamendoService(), clientId = "")

        val result = repository.fetchTrackForGenre("rock")

        assertTrue(result.isFailure)
    }

    @Test
    fun fetchTrackFailsWhenGenreMissing() = runTest {
        val repository = JamendoRepository(FakeJamendoService(), clientId = "client")

        val result = repository.fetchTrackForGenre("")

        assertTrue(result.isFailure)
    }

    @Test
    fun fetchTrackFailsWhenServiceReturnsEmptyList() = runTest {
        val fakeService = FakeJamendoService().apply {
            response = JamendoTracksResponse(emptyList())
        }
        val repository = JamendoRepository(fakeService, clientId = "client")

        val result = repository.fetchTrackForGenre("indie")

        assertTrue(result.isFailure)
    }

    @Test
    fun fetchTrackPropagatesServiceErrors() = runTest {
        val fakeService = FakeJamendoService().apply {
            error = IllegalStateException("boom")
        }
        val repository = JamendoRepository(fakeService, clientId = "client")

        val result = repository.fetchTrackForGenre("metal")

        assertTrue(result.isFailure)
        assertEquals("boom", result.exceptionOrNull()?.message)
    }

    private class FakeJamendoService : JamendoService {
        var response: JamendoTracksResponse = JamendoTracksResponse(
            results = listOf(
                JamendoTrack(id = "1", name = "Song", artistName = "Artist", audio = "", image = "")
            )
        )
        var error: Throwable? = null
        var lastGenre: String? = null

        override suspend fun getTracks(
            clientId: String,
            format: String,
            limit: Int,
            genre: String,
            order: String
        ): JamendoTracksResponse {
            lastGenre = genre
            error?.let { throw it }
            return response
        }
    }
}
