package com.example.android_dictaphone_decoder
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class DictaphoneActivity(private val activity: ComponentActivity) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var outputFilePath: String? = null
    private var counterRecord: Int? = 0


    // проверка на разрешения доступа к микрофону и к записи в хранилище
    fun checkPermission(context: Context) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(activity, permissions,0)
        }
    }

    fun startRecording(context: Context) {
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
        outputFilePath = createOutputFile().absolutePath

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mediaRecorder = MediaRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(getOutputFilePath(context))
                try{
                    prepare()
                    start()
                } catch (e: IOException)
                {
                    // перехват
                }

            } }
    }

    private fun createOutputFile(): File {
        val directory = Environment.getExternalStorageDirectory().absolutePath
        val folder = File(directory, "MyRecorder")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        counterRecord = counterRecord!! + 1
        return File(folder, "recording_${counterRecord}.mp3")
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }
    fun startPlaying(context: Context) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(getOutputFilePath(context))
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
    private fun getOutputFilePath(context: Context): String {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val fileName = "recording_${counterRecord}.3gp"
        val file = File(storageDir, fileName)
        return file.absolutePath
    }
}
