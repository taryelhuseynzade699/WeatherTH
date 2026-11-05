package com.example.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.example.R
import com.example.data.model.WeatherCategory

class WeatherSoundManager(private val context: Context) {
    private var ambientPlayer: MediaPlayer? = null
    private var currentResId: Int? = null
    private var isMuted: Boolean = false
    private var volume: Float = 0.7f

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .build()
        )
        .build()

    private var clickSoundId: Int = 0
    private var thunderSoundId: Int = 0

    init {
        try {
            clickSoundId = soundPool.load(context, R.raw.snd_click, 1)
            thunderSoundId = soundPool.load(context, R.raw.snd_thunder, 1)
        } catch (e: Exception) {
            Log.e("WeatherSoundManager", "Error loading sound effects", e)
        }
    }

    fun playAmbient(category: WeatherCategory, isDay: Boolean) {
        val targetRes = when {
            category == WeatherCategory.RAIN || category == WeatherCategory.DRIZZLE || category == WeatherCategory.THUNDERSTORM -> R.raw.snd_rain
            !isDay -> R.raw.snd_night
            category == WeatherCategory.CLEAR -> R.raw.snd_sun
            else -> R.raw.snd_wind
        }

        if (currentResId == targetRes && ambientPlayer?.isPlaying == true) {
            return
        }

        stopAmbient()
        currentResId = targetRes

        if (isMuted) return

        try {
            ambientPlayer = MediaPlayer.create(context, targetRes)?.apply {
                isLooping = true
                val effectiveVol = if (isMuted) 0f else volume
                setVolume(effectiveVol, effectiveVol)
                start()
            }
        } catch (e: Exception) {
            Log.e("WeatherSoundManager", "Error playing ambient sound", e)
        }
    }

    fun playClick() {
        if (isMuted || clickSoundId == 0) return
        try {
            soundPool.play(clickSoundId, volume * 0.7f, volume * 0.7f, 1, 0, 1.0f)
        } catch (e: Exception) {
            Log.e("WeatherSoundManager", "Error playing click sound", e)
        }
    }

    fun playThunder() {
        if (isMuted || thunderSoundId == 0) return
        try {
            soundPool.play(thunderSoundId, volume, volume, 2, 0, 1.0f)
        } catch (e: Exception) {
            Log.e("WeatherSoundManager", "Error playing thunder sound", e)
        }
    }

    fun setSoundEnabled(enabled: Boolean, category: WeatherCategory? = null, isDay: Boolean = true) {
        isMuted = !enabled
        if (isMuted) {
            ambientPlayer?.pause()
        } else {
            if (ambientPlayer != null) {
                val effectiveVol = volume
                ambientPlayer?.setVolume(effectiveVol, effectiveVol)
                ambientPlayer?.start()
            } else if (category != null) {
                playAmbient(category, isDay)
            }
        }
    }

    fun setVolume(newVolume: Float) {
        volume = newVolume.coerceIn(0f, 1f)
        if (!isMuted) {
            ambientPlayer?.setVolume(volume, volume)
        }
    }

    fun stopAmbient() {
        try {
            ambientPlayer?.stop()
            ambientPlayer?.release()
        } catch (e: Exception) {
            Log.e("WeatherSoundManager", "Error stopping ambient player", e)
        } finally {
            ambientPlayer = null
        }
    }

    fun pause() {
        try {
            if (ambientPlayer?.isPlaying == true) {
                ambientPlayer?.pause()
            }
        } catch (e: Exception) {
            Log.e("WeatherSoundManager", "Error pausing ambient sound", e)
        }
    }

    fun resume() {
        try {
            if (!isMuted && ambientPlayer != null && ambientPlayer?.isPlaying == false) {
                ambientPlayer?.start()
            }
        } catch (e: Exception) {
            Log.e("WeatherSoundManager", "Error resuming ambient sound", e)
        }
    }

    fun release() {
        stopAmbient()
        try {
            soundPool.release()
        } catch (e: Exception) {
            Log.e("WeatherSoundManager", "Error releasing sound pool", e)
        }
    }
}
