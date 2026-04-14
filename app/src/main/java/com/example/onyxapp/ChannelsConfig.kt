package com.example.onyxapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val name: String,
    val url: String,
    val logo: String? = null,
    val group: String? = null,
    val isActive: Boolean = true,
    val id: String? = null,
    val order: Int = 0
)

@Serializable
data class Movie(
    val id: String? = null,
    val title: String,
    val description: String? = null,
    val poster_url: String? = null,
    val backdrop_url: String? = null,
    val video_url: String,
    val category: String? = null,
    val is_active: Boolean = true
)

@Serializable
data class UserProfile(
    @SerialName("id") val id: String,
    @SerialName("role") val role: String = "USER",
    @SerialName("isActive") val isActive: Boolean = false,
    @SerialName("expiryDate") val expiryDate: String? = null,
    @SerialName("deviceId") val deviceId: String? = null,
    @SerialName("deviceId2") val deviceId2: String? = null,
    @SerialName("subscriptionPlan") val subscriptionPlan: String? = "1 MES",
    @SerialName("email") val email: String? = null
)

object ChannelsConfig {
    const val PC_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}
