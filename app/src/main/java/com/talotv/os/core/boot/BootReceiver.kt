package com.talotv.os.core.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.talotv.os.MainActivity
import com.talotv.os.core.security.RemoteLockOverlayService
import com.talotv.os.core.security.TaloSecurityVault
import com.talotv.os.core.system.CloudSyncManager

/**
 * Boot Receiver
 * Runs immediately at TV power-on or restart.
 * Enforces unbreachable Lost Mode if the device was locked before reboot.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("TaloBootReceiver", "Boot action received: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            // Initialize security vault state
            CloudSyncManager.initSecurityState(context)

            val isLocked = TaloSecurityVault.isLocked(context)
            val lockMsg = TaloSecurityVault.getLockMessage(context)

            if (isLocked) {
                Log.w("TaloBootReceiver", "EMERGENCY: Device booted in LOST MODE. Enforcing hardware lock.")
                val lockIntent = Intent(context, RemoteLockOverlayService::class.java).apply {
                    putExtra("IS_LOCKED", true)
                    putExtra("LOCK_MESSAGE", lockMsg)
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(lockIntent)
                    } else {
                        context.startService(lockIntent)
                    }
                } catch (e: Exception) {
                    Log.e("TaloBootReceiver", "Cannot start lock overlay: ${e.message}")
                }
            }

            // Launch Talo OS Main Activity
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(launchIntent)

            // Trigger background cloud sync & telemetry
            CloudSyncManager.startBackgroundSync(context)
        }
    }
}
