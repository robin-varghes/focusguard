package com.block.instagramblocker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Watches which app comes to the foreground. If it is Instagram, it immediately
 * sends the phone to the home screen, so Instagram never stays open.
 *
 * This is the fallback block: even if OS-level hiding is unavailable on a given
 * ROM, this bounces you out of the app.
 */
class InstagramBlockerService : AccessibilityService() {

    private val blockedPackages = setOf(
        "com.instagram.android",
        "com.instagram.lite"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg in blockedPackages) {
            // Kick back to the home screen.
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        A11yGuard.startWatchdog(this)
    }

    override fun onInterrupt() {
        // No-op.
    }
}
