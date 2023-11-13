package com.example.android_dictaphone_decoder

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.android_dictaphone_decoder.ui.theme.SpeechToTextTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/*
*   Простой диктофон и перевод голоса в текст.
*   2 отдельные кнопки на 2 действия.
*   Если возникла проблема с тем, что приложение вылетает
*   проверь разрешение для микрофона у приложения
* */
class MainActivity : ComponentActivity() {
    private val dictaphoneActivity = DictaphoneActivity(this)
    private val speechKit = SpeechKit()
    private var viewModel = AudioDataViewModel()

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpeechToTextTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = "Речь в текст",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            )
                        }
                    ) {
                        SpeechToText()
                    }
                }
            }
        }
    }

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

    @SuppressLint("NewApi")
    @Composable
    fun DisplayAudioData() {
        val audioDataList by viewModel.audioDataList.collectAsState()

        val ioScope = CoroutineScope(Dispatchers.IO)

        val textStates = remember { mutableStateMapOf<Int, String>() }
        val playButtonStates = remember { mutableStateMapOf<Int, Boolean>() }
        val textButtonStates = remember { mutableStateMapOf<Int, Boolean>() }

        LazyColumn {
            itemsIndexed(audioDataList) { index, info ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.border(2.dp, Color.Black)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row {
                            val currentDateTime = info.date

                            // Форматирование даты и времени
                            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                            val formattedDateTime = currentDateTime.format(formatter)

                            Text("${String.format("%.2f", info.duration / 1000f)}s")
                            Spacer(Modifier.width(8.dp))
                            Text("${String.format("%.2f", info.size / 1024f)}kb")
                            Spacer(Modifier.width(8.dp))
                            Text(formattedDateTime)
                        }
                        Button(
                            onClick = {
                                if (textStates[index].isNullOrEmpty()) {
                                    ioScope.launch {
                                        textStates[index] = speechKit.recognize(info.filePath)
                                        textButtonStates[index] = textButtonStates[index]?.not() ?: true
                                    }
                                }
                                else {
                                    textButtonStates[index] = textButtonStates[index]?.not() ?: true
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(23, 29, 91),
                                contentColor = Color.White
                            )
                        ) {
                            Text(if (textButtonStates[index] == true) "Скрыть" else "Распознать")
                        }
                        Text(if (textButtonStates[index] == true) "${textStates[index]}" else "")
                    }

                    Button(
                        onClick = {
                            if (playButtonStates[index] == true) {
                                dictaphoneActivity.stopPlaying()
                            } else {
                                dictaphoneActivity.startPlaying(info.filePath)
                            }
                            playButtonStates[index] = playButtonStates[index]?.not() ?: true
                        },
                        modifier = Modifier.size(75.dp, 75.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(23, 29, 91)
                        )
                    ) {
                        Icon(
                            imageVector = if (playButtonStates[index] == true) Icons.Default.Done else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    @Composable
    fun SpeechToText() {
        val context = LocalContext.current
        var isRecording by remember { mutableStateOf(false) }

        DisplayAudioData()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Button(
                onClick = {
                    if (!isRecording) {
                        dictaphoneActivity.startRecording(context)
                    } else {
                        dictaphoneActivity.stopRecording()

                        viewModel.addAudioData(
                            dictaphoneActivity.outputFilePath.toString(),
                            LocalDateTime.now()
                        )
                    }
                    isRecording = !isRecording
                },
                modifier = Modifier.size(75.dp, 75.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(23, 29, 91)
                )
            ) {
                Icon(
                    imageVector = if (!isRecording) Icons.Default.Add else Icons.Default.Done,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }

    private fun saveTextToFile(text: String, fileName: String) {
        try {
            val file = File(getExternalFilesDir(null), fileName)
            val writer = FileWriter(file)
            writer.append(text)
            writer.flush()
            writer.close()
            Toast.makeText(this, "Text saved to $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving text", Toast.LENGTH_SHORT).show()
        }
    }
}
