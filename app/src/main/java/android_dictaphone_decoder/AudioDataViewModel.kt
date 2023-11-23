package android_dictaphone_decoder

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.File

/*
*
* Тут лежат методы для сериализации и для десериализации
*
* */

class AudioDataViewModel(private val context: Context) : ViewModel() {
    private val _audioDataList = mutableListOf<AudioData>()
    val audioDataList: List<AudioData> = _audioDataList

    private val gson = Gson()

    fun loadAudioData(selectedDate: String?) {
        try {
            val file = File(context.getExternalFilesDir(null), "$selectedDate/audio_data.json")

            if (file.exists()) {
                val jsonString = file.readText()
                val type: java.lang.reflect.Type? = object : TypeToken<List<AudioData>>() {}.type
                _audioDataList.addAll(gson.fromJson(jsonString, type))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveAudioData(selectedDate: String?) {
        try {
            val file = File(context.getExternalFilesDir(null), "${selectedDate}/audio_data.json")
            val jsonString = gson.toJson(_audioDataList)
            file.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addAudioData(filePath: String, date: String?, selectedDate: String?) {
        val newAudioFileInfo = AudioData.instance(filePath, date, selectedDate)
        if (!_audioDataList.contains(newAudioFileInfo)) {
            _audioDataList.add(newAudioFileInfo)
            saveAudioData(selectedDate)
        }
    }
}
