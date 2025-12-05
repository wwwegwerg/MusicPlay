package com.example.musicplay.jamendo

import com.google.gson.annotations.SerializedName

/** Обёртка, которую Jamendo возвращает при запросе треков. */
data class JamendoTracksResponse(
    @SerializedName("results") val results: List<JamendoTrack> = emptyList()
)

/** Подмножество полей трека Jamendo, необходимых текущему UI. */
data class JamendoTrack(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("artist_name") val artistName: String? = null,
    @SerializedName("audio") val audio: String? = null,
    @SerializedName("image") val image: String? = null
)
