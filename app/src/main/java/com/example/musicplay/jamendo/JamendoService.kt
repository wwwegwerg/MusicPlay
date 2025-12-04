package com.example.musicplay.jamendo

import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoService {
    @GET("tracks/")
    suspend fun getTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("fuzzytags") genre: String,
        @Query("order") order: String = "popularity_total"
    ): JamendoTracksResponse
}
