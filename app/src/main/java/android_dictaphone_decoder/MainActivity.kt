package android_dictaphone_decoder

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import android_dictaphone_decoder.SpeechKit
import android_dictaphone_decoder.theme.AppColors
import android_dictaphone_decoder.theme.SpeechToTextTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
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
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    AppColors.gradientBotton,
                                    AppColors.gradientMid,
                                    AppColors.gradientTop
                                )
                            )
                        )
                ) {
                    RecordButton()
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @SuppressLint("NewApi", "MutableCollectionMutableState")
    @Composable
    fun DisplayAudioData(context: Context) {
        val audioDataList by viewModel.audioDataList.collectAsState()

        val textStates = remember { mutableStateMapOf<Int, String>() }
        val playButtonStates = remember { mutableStateMapOf<Int, Boolean>() }
        val textButtonStates = remember { mutableStateMapOf<Int, Boolean>() }

        val textFieldValues = remember { mutableStateOf(mutableMapOf<Int, TextFieldValue>()) }
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current

        val ioScope = CoroutineScope(Dispatchers.IO)

        LazyColumn {
            itemsIndexed(audioDataList) { index, info ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .border(2.dp, AppColors.Black)
                        .background(AppColors.Ghost)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        var value by remember {
                            mutableStateOf(
                                textFieldValues.value[index]?.text ?: ""
                            )
                        }

                        TextField(
                            value = value,
                            onValueChange = { newValue ->
                                value = newValue
                                textFieldValues.value[index] = TextFieldValue(newValue)
                            },
                            placeholder = {
                                Text(
                                    "Record ${
                                        if (info.filePath.length >= 5) info.filePath[info.filePath.length - 5] else 0
                                    }"
                                )
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            )
                        )
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
                            val checkExpr =
                                info.duration / 1000f < 30 && info.size / 1024f / 1024f < 1

                            Button(
                                onClick = {
                                    if (textStates[index] == null) {
                                        if (checkExpr) {
                                            Toast.makeText(
                                                context,
                                                "Wait speech-to-text",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            ioScope.launch {
                                                textStates[index] =
                                                    speechKit.recognize(info.filePath)
                                                textButtonStates[index] =
                                                    textButtonStates[index]?.not() ?: true
                                            }
                                        } else {
                                            textStates[index] = "Too long to recognize"
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
                                    containerColor = AppColors.Darkest,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(if (textButtonStates[index] == true) "Hide" else "Text")
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
                            containerColor = AppColors.Darkest
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

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("NewApi")
    @Composable
    fun RecordButton() {
        val context = LocalContext.current
        var isRecording by remember { mutableStateOf(false) }

        var elapsedTime by remember { mutableLongStateOf(0L) }

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
                        isRecording = true
                        GlobalScope.launch {
                            while (isRecording) {
                                delay(100)
                                elapsedTime += 1
                            }
                        }
                        dictaphone.startRecording(context)
                    } else {
                        isRecording = false
                        dictaphone.stopRecording()

                        viewModel.addAudioData(
                            dictaphone.outputFilePath,
                            LocalDateTime.now()
                        )

                        elapsedTime = 0
                    }
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .size(75.dp, 75.dp)
                    .border(
                        2.dp, AppColors.Lightest,
                        shape = RoundedCornerShape(20.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Darkest
                )
            ) {
                Icon(
                    imageVector = if (!isRecording) Icons.Default.Add else Icons.Default.Done,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            if (isRecording) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(AppColors.DarkGhost, CircleShape)
                        .padding(16.dp)
                        .align(Alignment.Center)
                ) {
                    Text(
                        text = "${elapsedTime / 10f}",
                        color = AppColors.Lightest,
                        style = MaterialTheme.typography.h2.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
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