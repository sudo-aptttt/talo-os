package com.talotv.os.core.boot

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.talotv.os.MainActivity
import com.talotv.os.core.security.TaloSecurityVault

/**
 * Home Key Accessibility Service & Fortress Lock Shield
 * Intercepts navigation keys (Home, Back, Menu, AppSwitch).
 * When Lost Mode is active, consumes ALL navigation keys to prevent escape.
 */
class HomeKeyAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used, we only filter key events
    }

    override fun onInterrupt() {
        Log.w("TaloAccessibility", "Accessibility service interrupted")
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action

        val isLocked = TaloSecurityVault.isLocked(this)

        if (isLocked) {
            // When in Lost Mode, lock out all navigation attempts
            if (keyCode == KeyEvent.KEYCODE_HOME ||
                keyCode == KeyEvent.KEYCODE_BACK ||
                keyCode == KeyEvent.KEYCODE_MENU ||
                keyCode == KeyEvent.KEYCODE_SETTINGS ||
                keyCode == KeyEvent.KEYCODE_APP_SWITCH
            ) {
                if (action == KeyEvent.ACTION_UP) {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    startActivity(intent)
                }
                return true // Consume and block completely
            }
        }

        // Standard Home key interception (Google TV stock override)
        if (keyCode == KeyEvent.KEYCODE_HOME && action == KeyEvent.ACTION_UP) {
            Log.d("TaloAccessibility", "Home Key captured. Launching Talo OS.")
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
            return true
        }

        return super.onKeyEvent(event)
    }
}
