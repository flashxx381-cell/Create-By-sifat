package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(val text: String)

@JsonClass(generateAdapter = true)
data class Content(val parts: List<Part>, val role: String = "user")

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(val content: Content)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(val candidates: List<Candidate>?)

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val api: GeminiApi = retrofit.create(GeminiApi::class.java)
}

class GeminiRepository {
    private val modelName = "gemini-3.5-flash"
    private val apiKey = BuildConfig.GEMINI_API_KEY

    suspend fun askAssistant(prompt: String, chatHistory: List<Content> = emptyList()): String {
        return try {
            val systemInstruction = Content(
                parts = listOf(
                    Part(
                        "You are Noor Shield AI Islamic Assistant (নূর শিল্ড এআই ইসলামিক অ্যাসিস্ট্যান্ট). " +
                        "You must answer questions exclusively in Bengali language. " +
                        "Answer general Islamic questions, help users find relevant Quran verses and daily duas. " +
                        "Always keep your tone deeply respectful, premium, elegant, and warm. " +
                        "Always add a gentle, humble disclaimer at the end of answers encouraging users to consult qualified, recognized Islamic scholars for official/formal religious rulings (ফতোয়া)."
                    )
                ),
                role = "system"
            )

            val fullContents = chatHistory + Content(parts = listOf(Part(prompt)), role = "user")

            val request = GenerateContentRequest(
                contents = fullContents,
                systemInstruction = systemInstruction
            )

            val response = GeminiClient.api.generateContent(
                model = modelName,
                apiKey = apiKey,
                request = request
            )

            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "দুঃখিত, কোনো উত্তর পাওয়া যায়নি। অনুগ্রহ করে আবার চেষ্টা করুন।"
        } catch (e: Exception) {
            e.printStackTrace()
            "একটি ত্রুটি ঘটেছে: ${e.localizedMessage ?: "নেটওয়ার্ক কানেকশন পরীক্ষা করুন।"}"
        }
    }
}
