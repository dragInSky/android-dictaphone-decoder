package com.example.android_dictaphone_decoder

import android.media.MediaMetadataRetriever
import java.io.File
import java.time.LocalDateTime

data class AudioData(
    val filePath: String, val duration: Long, val size: Long,
    val date: LocalDateTime
) {
    companion object {
        fun instance(filePath: String, date: LocalDateTime): AudioData {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)

            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                    ?: 0
            val size = File(filePath).length()

            retriever.release()

            return AudioData(filePath, duration, size, date)
        }
    }
}