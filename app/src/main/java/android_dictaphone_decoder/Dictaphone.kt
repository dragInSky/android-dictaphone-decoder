package android_dictaphone_decoder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException

class Dictaphone(private val activity: ComponentActivity) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null

    private var counter = 1



    var outputFilePath: String = ""

    // проверка на разрешения доступа к микрофону и к записи в хранилище
    fun checkPermission(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(activity, permissions, 0)
        }
    }

    // старт записи голоса
    @RequiresApi(Build.VERSION_CODES.Q)
    fun startRecording(context: Context, selectedDay: String) {
        try {
            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioEncodingBitRate(16000)
            mediaRecorder?.setAudioSamplingRate(44100)
            val directory = File(activity.getExternalFilesDir(null), selectedDay)
            if (!directory.exists()) {
                directory.mkdirs()
            }

            outputFilePath = "${directory}/audio_record${counter++}.ogg"

            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.OGG)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
            mediaRecorder?.setOutputFile(outputFilePath)

            mediaRecorder?.prepare()
            mediaRecorder?.start()

            Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error starting recording", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopRecording() {
        //задержка чтобы запись резко не обрывалась
        runBlocking {
            delay(200)
        }

        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null

            Toast.makeText(activity, "Recording stopped", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activity, "Error stopping recording", Toast.LENGTH_SHORT).show()
        }
    }

    fun startPlaying(filePath: String) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
            start()
        }
    }

    fun stopPlaying() {
        mediaPlayer?.apply {
            stop()
            release()
        }
    }
}
