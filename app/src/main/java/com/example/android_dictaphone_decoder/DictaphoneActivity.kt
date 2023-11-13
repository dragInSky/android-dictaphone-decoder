package com.example.android_dictaphone_decoder

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
import java.io.IOException

class DictaphoneActivity(private val activity: ComponentActivity) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null

    var outputFilePath: String? = null

    private var counter = 1

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
    fun startRecording(context: Context) {
        try {
            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioEncodingBitRate(16000)
            mediaRecorder?.setAudioSamplingRate(44100)
            outputFilePath = "${activity.getExternalFilesDir(null)}/audio_record${counter++}.ogg"

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

    // данные методы в новой версии пока, что не используются
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
