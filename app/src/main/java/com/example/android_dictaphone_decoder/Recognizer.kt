package com.example.android_dictaphone_decoder

import android.annotation.SuppressLint
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

class Recognizer {
    private val apiKey = "AQVNz2OJ19b7zlbkLyH1dF661jvpJ4X_YXaUkEzh"
    private val folderId = "b1gj2agjgfbobbspr6uu"
    private val apiUrl =
        "https://stt.api.cloud.yandex.net/speech/v1/stt:recognize?folderId=${folderId}&lang=ru-RU"

    fun recognize(filePath: String,): String {
        val newFilePath = filePath


        //Отсюда запрос к Yandex SpeechKit, этот код отдельно работает с .ogg файлом
        val client = OkHttpClient()

        val mediaType = "application/octet-stream".toMediaType()

        val file = File(newFilePath)
        val bytes = file.readBytes()
        val body = bytes.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .addHeader("Authorization", "Api-Key $apiKey")
            .addHeader("Content-Type", "audio/x-pcm;bit=16;rate=44100")
            .build()

        client.newCall(request).execute().use { response ->
            return if (!response.isSuccessful) "Unexpected code $response" else response.body.toString()
        }
    }

    @SuppressLint("WrongConstant")
    private fun convertMp3ToOgg(inputPath: String, outputPath: String) {
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null

        try {
            extractor.setDataSource(inputPath)
            var audioTrackIndex = -1
            var format: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime!!.startsWith("audio/")) {
                    audioTrackIndex = i
                    break
                }
            }
            if (audioTrackIndex == -1) {
                Log.e("AudioConverter", "No audio track found in $inputPath")
                return
            }
            extractor.selectTrack(audioTrackIndex)
            muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val trackIndex = muxer.addTrack(format!!)
            muxer.start()
            val buffer = ByteBuffer.allocate(1024 * 1024)
            val info = MediaCodec.BufferInfo()
            extractor.readSampleData(buffer, 0)
            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) {
                    break
                }
                info.offset = 0
                info.size = sampleSize
                info.presentationTimeUs = extractor.sampleTime
                info.flags = extractor.sampleFlags
                muxer.writeSampleData(trackIndex, buffer, info)
                extractor.advance()
            }
        } catch (e: IOException) {
            Log.e("IOException", "IOException audio track found in $inputPath")
            e.printStackTrace()
        } finally {
            if (muxer != null) {
                muxer.stop()
                muxer.release()
            }
            extractor.release()
        }
    }
}
