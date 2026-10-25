package com.talotv.os.core.security

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.talotv.os.MainActivity

/**
 * Remote Lock Overlay & Foreground Security Guardian
 * Maintains foreground process priority in Android OS when Lost Mode is active.
 * Ensures the system will never kill the lock process and forces MainActivity to front.
 */
class RemoteLockOverlayService : Service() {

    companion object {
        private const val CHANNEL_ID = "talo_security_shield"
        private const val NOTIFICATION_ID = 9991
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isLocked = intent?.getBooleanExtra("IS_LOCKED", false) ?: false

        if (isLocked) {
            val notification = createSecurityNotification()
            startForeground(NOTIFICATION_ID, notification)

            // Force Talo OS lock screen to the top of the activity stack
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(mainIntent)
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Talo Security Shield",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Bouclier de sécurité actif pour le Mode Perdu"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createSecurityNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Talo OS — Bouclier Actif")
                .setContentText("Appareil verrouillé en Mode Perdu.")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Talo OS — Bouclier Actif")
                .setContentText("Appareil verrouillé en Mode Perdu.")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setOngoing(true)
                .build()
        }
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}
