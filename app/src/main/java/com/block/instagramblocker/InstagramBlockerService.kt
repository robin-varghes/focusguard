package com.block.instagramblocker

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.ContextCompat

/**
 * Bounces you to the home screen when Instagram opens, uninstalls Instagram
 * whenever it is installed or enabled, and blocks FocusGuard's own Settings and
 * uninstall screens so Force stop / Uninstall can't be tapped on the phone.
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
    private val confirmLabels = setOf("ok", "uninstall")

    private val handler = Handler(Looper.getMainLooper())
    private var lastKick = 0L
    private val lastUninstallRequest = mutableMapOf<String, Long>()
    private var confirmUntil = 0L
    private var receiverRegistered = false

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val pkg = intent.data?.schemeSpecificPart ?: return
            if (pkg in blockedPackages && isInstalled(pkg)) requestUninstall(pkg)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        A11yGuard.startWatchdog(this)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(this, packageReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        receiverRegistered = true

        blockedPackages.filter(::isInstalled).forEach(::requestUninstall)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return

        if (pkg in blockedPackages) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                kickHome()
                // Let the Home press settle before opening the uninstall dialog.
                handler.postDelayed({ requestUninstall(pkg) }, 800)
            }
            return
        }

        if (pkg !in settingsPackages) return
        val root = rootInActiveWindow ?: return

        if (isFocusGuardControlScreen(pkg, root)) {
            kickHome()
            return
        }

        if (pkg.endsWith("packageinstaller") && SystemClock.elapsedRealtime() < confirmUntil) {
            confirmInstagramUninstall(root)
        }
    }

    private fun isFocusGuardControlScreen(pkg: String, root: AccessibilityNodeInfo): Boolean {
        if (root.findAccessibilityNodeInfosByText(getString(R.string.app_name)).isEmpty()) return false
        if (pkg.endsWith("packageinstaller")) return true
        return dangerLabels.any { root.findAccessibilityNodeInfosByText(it).isNotEmpty() }
    }

    private fun isInstalled(pkg: String): Boolean = try {
        packageManager.getApplicationInfo(pkg, PackageManager.MATCH_DISABLED_COMPONENTS)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    private fun requestUninstall(pkg: String) {
        if (!isInstalled(pkg)) return
        val now = SystemClock.elapsedRealtime()
        if (now - (lastUninstallRequest[pkg] ?: -60_000L) < 30_000) return
        lastUninstallRequest[pkg] = now
        confirmUntil = now + 20_000
        try {
            startActivity(
                Intent(Intent.ACTION_DELETE, Uri.parse("package:$pkg"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            confirmUntil = 0
        }
    }

    private fun confirmInstagramUninstall(root: AccessibilityNodeInfo) {
        if (root.findAccessibilityNodeInfosByText("Instagram").isEmpty()) return
        val button = confirmLabels
            .flatMap { root.findAccessibilityNodeInfosByText(it) }
            .firstOrNull { it.text?.toString()?.trim()?.lowercase() in confirmLabels }
            ?: return
        var target: AccessibilityNodeInfo? = button
        while (target != null && !target.isClickable) target = target.parent
        if (target?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) confirmUntil = 0
    }

    private fun kickHome() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastKick < 400) return
        lastKick = now
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    override fun onDestroy() {
        if (receiverRegistered) unregisterReceiver(packageReceiver)
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onInterrupt() {
        // No-op.
    }
}
