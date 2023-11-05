package com.example.android_dictaphone_decoder

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp


/*
*
*   Простой диктофон использующий MediaRecorder и имеющий 2 кнопки
*   начать запись/закончить запись и прослушать запись/закончить прослушивание
*
* */
class MainActivity : ComponentActivity() {
    private val dictaphoneActivity = DictaphoneActivity(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DictaphoneApp()
        }

    }

    @Composable
    fun DictaphoneApp() {
        var isRecording by remember { mutableStateOf(false) }
        var isPlaying by remember { mutableStateOf(false) }

        val context = LocalContext.current

        // Проверка разрешений и запрос их у пользователя
        DisposableEffect(Unit) {
            dictaphoneActivity.checkPermission(context)
            onDispose { }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (!isRecording) {
                        dictaphoneActivity.startRecording(context)
                    } else {
                        dictaphoneActivity.stopRecording()
                    }
                    isRecording = !isRecording
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = if (isRecording) "Stop Recording" else "Start Recording")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isPlaying) {
                        dictaphoneActivity.startPlaying(context)
                    } else {
                        dictaphoneActivity.stopPlaying()
                    }
                    isPlaying = !isPlaying
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = if (isPlaying) "Stop Playing" else "Start Playing")
            }
        }
    }

}






