package com.example.data.model

data class CctvCamera(
    val id: String,
    val name: String,
    val thaiName: String,
    val location: String,
    val area: String,
    val province: String = "สุราษฎร์ธานี",
    val island: String = "เกาะสมุย",
    val youtubeVideoId: String,
    val originalUrl: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val category: CameraCategory,
    val streamResolution: String = "1080p FHD",
    val streamFps: Int = 30,
    val isOnline: Boolean = true
)

enum class CameraCategory(val label: String) {
    BEACH("ชายหาดทะเล"),
    STREET("ถนนคนเดิน/ย่านชุมชน"),
    RESTAURANT("ร้านอาหารริมทะเล")
}
