package com.example.chatly
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST


data class OllamaRequest(
    val model: String = "llama2",
    val messages: List<OllamaMessage>,
    val stream: Boolean = false
)

data class OllamaMessage(
    val role: String,
    val content: String
)

data class OllamaResponse(
    val message: OllamaMessage
)


interface OllamaApi {
    @POST("api/chat")
    suspend fun chat(@Body request: OllamaRequest): OllamaResponse
}


object OllamaClient {

    private const val BASE_URL = "http://10.0.2.2:11434/"

    private val okHttpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .callTimeout(150, java.util.concurrent.TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    val api: OllamaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaApi::class.java)
    }
}