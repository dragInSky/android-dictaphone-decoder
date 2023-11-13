package com.example.android_dictaphone_decoder

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class SpeechKit {
    private val apiKey = "AQVNz2OJ19b7zlbkLyH1dF661jvpJ4X_YXaUkEzh"
    private val folderId = "b1gj2agjgfbobbspr6uu"
    private val apiUrl =
        "https://stt.api.cloud.yandex.net/speech/v1/stt:recognize?folderId=${folderId}&lang=ru-RU"

    fun recognize(filePath: String): String {
        val client = OkHttpClient()

        val mediaType = "application/octet-stream".toMediaType()

        val file = File(filePath)
        val bytes = file.readBytes()
        val body = bytes.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .addHeader("Authorization", "Api-Key $apiKey")
            .addHeader("Content-Type", "audio/x-pcm;bit=16;rate=44100")
            .build()

        client.newCall(request).execute().use { response ->
            return if (!response.isSuccessful) "Unexpected code $response"
            else response.body?.string()?.drop(11)?.dropLast(2).toString()
        }
    }
}
