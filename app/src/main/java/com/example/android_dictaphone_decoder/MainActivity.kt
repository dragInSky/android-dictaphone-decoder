package com.example.android_dictaphone_decoder

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import com.example.android_dictaphone_decoder.ui.theme.AppColors
import com.example.android_dictaphone_decoder.ui.theme.SpeechToTextTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/*
*   Простой диктофон и перевод голоса в текст.
*   Если возникла проблема с тем, что приложение вылетает
*   проверь разрешение для микрофона у приложения
* */
class MainActivity : ComponentActivity() {
    private val dictaphone = Dictaphone(this)
    private val speechKit = SpeechKit()
    private var viewModel = AudioDataViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpeechToTextTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.VeryLight)
                ) {
                    RecordButton()
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("NewApi")
    @Composable
    fun DisplayAudioData(context: Context) {
        val audioDataList by viewModel.audioDataList.collectAsState()

        val textStates = remember { mutableStateMapOf<Int, String>() }
        val playButtonStates = remember { mutableStateMapOf<Int, Boolean>() }
        val textButtonStates = remember { mutableStateMapOf<Int, Boolean>() }

        val ioScope = CoroutineScope(Dispatchers.IO)

        LazyColumn {
            itemsIndexed(audioDataList) { index, info ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.border(2.dp, AppColors.Black)
                        .background(AppColors.Light)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row {
                            Text("${String.format("%.2f", info.duration / 1000f)}s")

                            Spacer(Modifier.width(8.dp))

                            Text("${String.format("%.2f", info.size / 1024f)}kb")

                            Spacer(Modifier.width(8.dp))

                            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                            val formattedDateTime = info.date.format(formatter)
                            Text(formattedDateTime)
                        }
                        Row {
                            Button(
                                onClick = {
                                    if (textStates[index].isNullOrEmpty()) {
                                        Toast.makeText(
                                            context,
                                            "Wait speech-to-text",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        ioScope.launch {
                                            textStates[index] = speechKit.recognize(info.filePath)
                                            textButtonStates[index] =
                                                textButtonStates[index]?.not() ?: true
                                        }
                                    } else {
                                        textButtonStates[index] =
                                            textButtonStates[index]?.not() ?: true
                                    }
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.VeryDark,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(if (textButtonStates[index] == true) "Скрыть" else "Распознать")
                            }

                            Spacer(Modifier.width(8.dp))

                            Text(
                                if (textButtonStates[index] == true) "${textStates[index]}" else "",
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (playButtonStates[index] == true) {
                                dictaphone.stopPlaying()
                            } else {
                                dictaphone.startPlaying(info.filePath)

//                                GlobalScope.launch {
//                                    delay(info.duration)
//
//                                    dictaphone.stopPlaying()
//                                    playButtonStates[index] = false
//                                }
                            }
                            playButtonStates[index] = playButtonStates[index]?.not() ?: true
                        },
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(75.dp, 75.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.VeryDark
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
    fun RecordButton() {
        val context = LocalContext.current
        var isRecording by remember { mutableStateOf(false) }

        DisplayAudioData(context)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Button(
                onClick = {
                    if (!isRecording) {
                        dictaphone.startRecording(context)
                    } else {
                        dictaphone.stopRecording()

                        viewModel.addAudioData(
                            dictaphone.outputFilePath,
                            LocalDateTime.now()
                        )
                    }
                    isRecording = !isRecording
                },
                modifier = Modifier.size(75.dp, 75.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.VeryDark
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
