package com.example.android_dictaphone_decoder

import android.media.MediaMetadataRetriever
import java.io.File

data class AudioData(val filePath: String, val duration: Long, val format: String, val size: Long) {
    companion object {
        fun instance(filePath: String): AudioData {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)

            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                    ?: 0
            val format = filePath.substring(filePath.lastIndexOf("."))
            val size = File(filePath).length()

            retriever.release()

            return AudioData(filePath, duration, format, size)
        }
    }
}