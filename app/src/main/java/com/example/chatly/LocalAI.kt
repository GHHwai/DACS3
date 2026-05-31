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

    val api: OllamaApi by lazy {

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:11434/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaApi::class.java)
    }
}