package com.example.data.network

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GroundingPlaceInfo(
    val title: String,
    val uri: String? = null
)

data class GeminiGroundedResponse(
    val answer: String,
    val groundedPlaces: List<GroundingPlaceInfo> = emptyList(),
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

object GeminiMapsService {
    private const val TAG = "GeminiMapsService"
    private const val MODEL_NAME = "gemini-3.8-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun queryMapsGrounding(prompt: String): GeminiGroundedResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GeminiGroundedResponse(
                answer = "กรุณากำหนดค่า GEMINI_API_KEY ใน Secrets panel ของ AI Studio เพื่อใช้งาน Google Maps Grounding กับข้อมูลสดรอบเกาะสมุย\n\nข้อมูลทั่วไป: เกาะสมุย จ.สุราษฎร์ธานี มีหาดที่มีชื่อเสียง เช่น หาดละไม หาดเฉวง หาดบ่อผุด พร้อมร้านอาหารและจุดชมวิวธรรมชาติระดับโลก",
                isSuccess = false,
                errorMessage = "ยังไม่ได้ระบุ GEMINI_API_KEY"
            )
        }

        try {
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                // Add Google Maps Grounding Tool
                val toolsArray = JSONArray().apply {
                    val googleMapsTool = JSONObject().put("googleMaps", JSONObject())
                    put(googleMapsTool)
                }
                put("tools", toolsArray)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                Log.e(TAG, "Gemini API error code: ${response.code}, body: $responseBody")
                val parsedErrorMsg = if (!responseBody.isNullOrBlank()) {
                    try {
                        val errObj = JSONObject(responseBody).optJSONObject("error")
                        errObj?.optString("message") ?: "HTTP ${response.code}"
                    } catch (e: Exception) {
                        "HTTP ${response.code}"
                    }
                } else {
                    "HTTP ${response.code}"
                }

                return@withContext GeminiGroundedResponse(
                    answer = "ไม่สามารถเชื่อมต่อ Google Maps AI ได้ในขณะนี้ ($parsedErrorMsg)\n\nข้อมูลแนะนำรอบเกาะสมุย: พิกัดกล้องเชื่อมต่อกับชุมชนหาดละไมและหาดบ่อผุด ซึ่งมีร้านอาหารและจุดท่องเที่ยวตลอดแนวชายหาด",
                    isSuccess = false,
                    errorMessage = "HTTP ${response.code}: $parsedErrorMsg"
                )
            }

            val json = JSONObject(responseBody)
            
            // Check for API-level error inside JSON
            if (json.has("error")) {
                val errObj = json.optJSONObject("error")
                val msg = errObj?.optString("message") ?: "API Error"
                return@withContext GeminiGroundedResponse(
                    answer = "ไม่สามารถดึงข้อมูลได้: $msg",
                    isSuccess = false,
                    errorMessage = msg
                )
            }

            val candidates = json.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext GeminiGroundedResponse(
                    answer = "ไม่พบข้อมูลจากระบบในขณะนี้",
                    isSuccess = false
                )
            }

            val firstCandidate = candidates.optJSONObject(0)
            if (firstCandidate == null) {
                return@withContext GeminiGroundedResponse(
                    answer = "ไม่พบข้อมูลเนื้อหาจากคำตอบ",
                    isSuccess = false
                )
            }

            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textBuilder = StringBuilder()

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i) ?: continue
                    if (part.has("text") && !part.isNull("text")) {
                        textBuilder.append(part.optString("text"))
                    }
                }
            } else if (firstCandidate.has("text") && !firstCandidate.isNull("text")) {
                textBuilder.append(firstCandidate.optString("text"))
            }

            // Extract grounding chunks with robust nested/un-nested support
            val places = mutableListOf<GroundingPlaceInfo>()
            val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
                ?: json.optJSONObject("groundingMetadata")

            if (groundingMetadata != null) {
                val groundingChunks = groundingMetadata.optJSONArray("groundingChunks")
                if (groundingChunks != null) {
                    for (i in 0 until groundingChunks.length()) {
                        val chunk = groundingChunks.optJSONObject(i) ?: continue

                        // 1. Check nested "maps" object
                        val maps = chunk.optJSONObject("maps")
                        if (maps != null) {
                            val title = maps.optString("title").ifBlank { "Google Maps Location" }
                            val uri = if (maps.has("uri") && !maps.isNull("uri")) maps.optString("uri") else null
                            places.add(GroundingPlaceInfo(title = title, uri = uri))
                            continue
                        }

                        // 2. Check nested "web" object
                        val web = chunk.optJSONObject("web")
                        if (web != null) {
                            val title = web.optString("title").ifBlank { "Google Maps Source" }
                            val uri = if (web.has("uri") && !web.isNull("uri")) web.optString("uri") else null
                            places.add(GroundingPlaceInfo(title = title, uri = uri))
                            continue
                        }

                        // 3. Check direct keys if flat
                        if (chunk.has("title") || chunk.has("uri")) {
                            val title = chunk.optString("title").ifBlank { "Google Maps Point" }
                            val uri = if (chunk.has("uri") && !chunk.isNull("uri")) chunk.optString("uri") else null
                            places.add(GroundingPlaceInfo(title = title, uri = uri))
                        }
                    }
                }
            }

            GeminiGroundedResponse(
                answer = if (textBuilder.isNotEmpty()) textBuilder.toString() else "ได้รับข้อมูลเรียบร้อยแล้ว",
                groundedPlaces = places,
                isSuccess = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed calling Gemini with Maps Grounding", e)
            GeminiGroundedResponse(
                answer = "เกิดข้อผิดพลาดในการดึงข้อมูลจาก Google Maps AI: ${e.localizedMessage ?: "Unknown Error"}",
                isSuccess = false,
                errorMessage = e.message
            )
        }
    }
}
