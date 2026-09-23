package com.block.instagramblocker

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Bounces you to the home screen when Instagram opens, and when a Settings or
 * uninstall screen for FocusGuard itself opens (so Force stop / Uninstall can't
 * be tapped on the phone; removal is only possible via ADB).
 */
class InstagramBlockerService : AccessibilityService() {

    private val blockedPackages = setOf(
        "com.instagram.android",
        "com.instagram.lite"
    )

    private val settingsPackages = setOf(
        "com.android.settings",
        "com.samsung.android.settings",
        "com.samsung.android.lool",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.samsung.android.packageinstaller"
    )

    private val dangerLabels = listOf("Force stop", "Uninstall", "Disable", "Clear data", "Clear storage")

    private var lastKick = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return

        if (pkg in blockedPackages && event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            kickHome()
            return
        }

        if (pkg in settingsPackages && isFocusGuardControlScreen(pkg)) {
            kickHome()
        }
    }

    private fun isFocusGuardControlScreen(pkg: String): Boolean {
        val root: AccessibilityNodeInfo = rootInActiveWindow ?: return false
        if (root.findAccessibilityNodeInfosByText(getString(R.string.app_name)).isEmpty()) return false
        if (pkg.endsWith("packageinstaller")) return true
        return dangerLabels.any { root.findAccessibilityNodeInfosByText(it).isNotEmpty() }
    }

    private fun kickHome() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastKick < 400) return
        lastKick = now
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        A11yGuard.startWatchdog(this)
    }

    override fun onInterrupt() {
        // No-op.
    }
}
