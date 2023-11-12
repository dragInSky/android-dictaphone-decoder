package com.example.android_dictaphone_decoder

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.android_dictaphone_decoder.ui.theme.SpeechToTextTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private val recognizer = Recognizer()
    private var talk by mutableStateOf("Говори, а я все запишу")

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun SpeechToText() {
        val context = LocalContext.current
        var isRecording by remember { mutableStateOf(false) }
        var isPlaying by remember { mutableStateOf(false) }

        val ioScope = CoroutineScope(Dispatchers.IO)

        Log.ERROR

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
                                talk = recognizer.recognize(it)
                            }
                        }
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

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (!isPlaying) {
                        dictaphoneActivity.startPlaying()
                    } else {
                        dictaphoneActivity.stopPlaying()
                    }
                    isPlaying = !isPlaying
                },
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Text(
                    text = if (isPlaying) "Stop Playing" else "Start Playing",
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
