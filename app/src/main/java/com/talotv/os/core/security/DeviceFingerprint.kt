package com.talotv.os.core.security

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest
import java.util.UUID

object DeviceFingerprint {

    fun getFingerprint(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
        val hardwareInfo = "${Build.MANUFACTURER}_${Build.MODEL}_${Build.SERIAL}_${Build.BOARD}"
        val raw = "$androidId:$hardwareInfo"

        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(raw.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }.take(16).uppercase()
        } catch (e: Exception) {
            UUID.randomUUID().toString().take(16).uppercase()
        }
    }
}
