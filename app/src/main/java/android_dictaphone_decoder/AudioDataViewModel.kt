package android_dictaphone_decoder

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

class AudioDataViewModel : ViewModel() {
    private val _audioDataList = MutableStateFlow(listOf<AudioData>())
    val audioDataList = _audioDataList.asStateFlow()

    fun addAudioData(filePath: String, date: LocalDateTime) {
        val newAudioFileInfo = AudioData.instance(filePath, date)
        val newList = _audioDataList.value.toMutableList()
        newList.add(newAudioFileInfo)
        _audioDataList.value = newList
    }
}