package com.example.android_dictaphone_decoder

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.example.android_dictaphone_decoder.ui.theme.SpeechToTextTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
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
    private var talk by mutableStateOf("Говори, а я все запишу")
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

        fun addAudioData(filePath: String) {
            val newAudioFileInfo = AudioData.instance(filePath)
            val newList = _audioDataList.value.toMutableList()
            newList.add(newAudioFileInfo)
            _audioDataList.value = newList
        }
    }

    @Composable
    fun DisplayAudioData() {
        val audioDataList by viewModel.audioDataList.collectAsState()

        val buttonStates = remember { mutableStateMapOf<Int, Boolean>() }

        LazyColumn {
            itemsIndexed(audioDataList) { index, info ->
                Text("Duration: ${info.duration} ms")
                Text("Format: ${info.format}")
                Text("Size: ${info.size} bytes")

                Button(
                    onClick = {
                        if (buttonStates[index] == true) {
                            dictaphoneActivity.stopPlaying()
                        } else {
                            dictaphoneActivity.startPlaying(info.filePath)
                        }
                        buttonStates[index] = buttonStates[index]?.not() ?: true
                    },
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = if (buttonStates[index] == true) "Stop Playing" else "Start Playing",
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }

    @SuppressLint("NewApi")
    @Composable
    fun SpeechToText() {
        val context = LocalContext.current
        var isRecording by remember { mutableStateOf(false) }

        val ioScope = CoroutineScope(Dispatchers.IO)

        DisplayAudioData()

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = talk,
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (!isRecording) {
                        dictaphoneActivity.startRecording(context)
                    } else {
                        dictaphoneActivity.stopRecording()

                        ioScope.launch {
                            dictaphoneActivity.outputFilePath?.let {
                                talk = "Вы сказали: " + speechKit.recognize(it)
                            }
                        }

                        viewModel.addAudioData(dictaphoneActivity.outputFilePath.toString())
                    }
                    isRecording = !isRecording
                },
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Text(
                    text = if (isRecording) "Stop Recording" else "Start Recording",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp)
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
