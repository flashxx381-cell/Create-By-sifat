package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

object GeminiClient {
    private const val TAG = "GeminiClient"

    // Custom system instruction for the AI Islamic Assistant as requested
    private val systemInstructionContent = Content(
        parts = listOf(
            Part(
                text = "আপনি একজন অত্যন্ত বিনয়ী, বিজ্ঞ এবং নির্ভরযোগ্য ইসলামিক এআই সহকারী যার নাম 'নূর শিল্ড' (Noor Shield)। " +
                        "আপনি ব্যবহারকারীকে আল-কুরআন, হাদিস, দুয়া, নামাজের নিয়ম এবং সাধারণ ইসলামিক তথ্য সুন্দর ও সহজ বাংলা ভাষায় বুঝিয়ে বলবেন। " +
                        "আপনার সকল উত্তরের সুর হবে অত্যন্ত সম্মানজনক, ইতিবাচক ও শান্ত। কোনো কঠিন বা জটিল ফতোয়ার বিষয়ে উত্তর দেওয়ার সময় " +
                        "সবসময় ব্যবহারকারীকে একজন যোগ্য ও মুত্তাকী আলেমের পরামর্শ নিতে উৎসাহিত করবেন। আপনি সর্বদা কুরআন এবং সহীহ হাদিসের রেফারেন্স দেওয়ার চেষ্টা করবেন।"
            )
        )
    )

    suspend fun askIslamicQuestion(question: String, history: List<Content> = emptyList()): String {
        return try {
            val apiKey = try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Throwable) {
                ""
            }

            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return "প্রিয় ব্যবহারকারী, আপনার এআই সহকারী সচল করার জন্য দয়া করে সেটিংস বা এআই স্টুডিও সিক্রেট প্যানেলে আপনার আসল GEMINI_API_KEY যুক্ত করুন। " +
                        "আপাতত এই ডেমো উত্তরটি দেখানো হচ্ছে:\n\n" +
                        "ইসলাম শান্তি ও কল্যাণের ধর্ম। কুরআনুল কারীমে আল্লাহ তাআলা বলেন, 'নিশ্চয়ই কষ্টের সাথেই স্বস্তি রয়েছে।' (সুরা আল-ইনশিরাহ: ৬)"
            }

            // Combine history and current question
            val currentTurn = Content(parts = listOf(Part(text = question)))
            val contents = history + currentTurn

            val request = GenerateContentRequest(
                contents = contents,
                systemInstruction = systemInstructionContent,
                generationConfig = GenerationConfig(temperature = 0.7f)
            )

            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "দুঃখিত, কোনো উত্তর খুঁজে পাওয়া যায়নি।"
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini", e)
            "যোগাযোগে সমস্যা হয়েছে। দয়া করে আপনার ইন্টারনেট সংযোগ পরীক্ষা করুন এবং আবার চেষ্টা করুন। ত্রুটি: ${e.localizedMessage}"
        }
    }
}
