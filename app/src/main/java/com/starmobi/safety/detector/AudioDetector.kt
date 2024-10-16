package com.starmobi.safety.detector

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher
import be.hogent.tarsos.dsp.onsets.PercussionOnsetDetector
import be.hogent.tarsos.dsp.onsets.PrintOnsetHandler
import com.blankj.utilcode.util.LogUtils
import com.starmobi.safety.R
import com.starmobi.safety.application.getAppContext


/**
 * 音频检测
 */
class AudioDetector {

    companion object {
        // 单例模式
        val INSTANCE: AudioDetector by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AudioDetector()
        }
    }


    fun detectAudio() {
        val mediaPlayer = MediaPlayer.create(getAppContext(), R.raw.claps44774)

        val sampleRate = getSampleRate()
        val bufferSize = 1024
        val threshold = 10.0
        val sensitivity = 50.0
        LogUtils.d("采样率2====$sampleRate")
        val dispatcher = MicrophoneAudioDispatcher(sampleRate, bufferSize, 0)
        PrintOnsetHandler()
//        val dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, 0)
        val mPercussionDetector = PercussionOnsetDetector(
            sampleRate.toFloat(), bufferSize, { time, salience ->
                LogUtils.d("检测到拍手的声音 time===$time , salience===$salience")
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                }
            }, sensitivity, threshold
        )
        dispatcher.addAudioProcessor(mPercussionDetector)
        // 开启线程执行检测声音程序
        Thread(dispatcher, "Audio Dispatcher").start()

//        val am = getAppContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        LogUtils.d("麦克风是否静音====${am.isMicrophoneMute}")
    }


    /**
     * 获取麦克风的采样率
     *
     */
    private fun getSampleRate(): Int {
        var sampleRate = 44100 // Default sample rate

        if (ActivityCompat.checkSelfPermission(
                getAppContext(), Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return sampleRate
        }

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.DEFAULT,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            AudioRecord.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
        )

        if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
            audioRecord.startRecording()
            sampleRate = audioRecord.sampleRate
            audioRecord.stop()
            audioRecord.release()
        }
        LogUtils.d("采样率1====$sampleRate")
        return sampleRate
    }

}