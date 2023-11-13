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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

        fun addAudioData(filePath: String, text: String) {
            val newAudioFileInfo = AudioData.instance(filePath, text)
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
                            Text("${info.duration} ms")
                            Spacer(Modifier.width(8.dp))
                            Text("${info.size} bytes")
                        }
                        Text("Text: ${info.text}")
                    }

                    Button(
                        onClick = {
                            if (buttonStates[index] == true) {
                                dictaphoneActivity.stopPlaying()
                            } else {
                                dictaphoneActivity.startPlaying(info.filePath)
                            }
                            buttonStates[index] = buttonStates[index]?.not() ?: true
                        },
                        modifier = Modifier.size(75.dp, 75.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(23, 29, 91)
                        )
                    ) {
                        Icon(
                            imageVector = if (buttonStates[index] == true) Icons.Default.Done else Icons.Default.PlayArrow,
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

        val ioScope = CoroutineScope(Dispatchers.IO)

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

                        ioScope.launch {
                            dictaphoneActivity.outputFilePath?.let {
                                talk = speechKit.recognize(it)
                                viewModel.addAudioData(
                                    dictaphoneActivity.outputFilePath.toString(),
                                    talk
                                )
                            }
                        }
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
