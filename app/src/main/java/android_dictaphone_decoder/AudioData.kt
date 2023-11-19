package android_dictaphone_decoder

import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.time.format.DateTimeFormatter

data class AudioData(
    val filePath: String, val duration: Long, val size: Long,
    val date: String, val selectedDate: String? // поменял форматы, потому что иначе в json не записывалось
) {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun instance(filePath: String, date: String?, selectedDate: String?): AudioData {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)

            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                    ?: 0
            val size = File(filePath).length()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            retriever.release()

            return AudioData(filePath, duration, size, date!!.format(formatter), selectedDate)
        }
    }
}