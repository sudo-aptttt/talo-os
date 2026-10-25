package com.talotv.os.core.audio

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.SoundPool
import android.view.SoundEffectConstants

class SoundEffectManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("talo_audio_prefs", Context.MODE_PRIVATE)
    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = 0

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean("sound_fx_enabled", true)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_fx_enabled", enabled).apply()
    }

    fun playFocusSound() {
        if (!isSoundEnabled()) return
        // Plays subtle sound pool stream or fallbacks to native navigation tick
        if (clickSoundId != 0) {
            soundPool?.play(clickSoundId, 0.35f, 0.35f, 1, 0, 1.0f)
        }
    }

    fun playSelectSound() {
        if (!isSoundEnabled()) return
        if (clickSoundId != 0) {
            soundPool?.play(clickSoundId, 0.6f, 0.6f, 2, 0, 1.1f)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
