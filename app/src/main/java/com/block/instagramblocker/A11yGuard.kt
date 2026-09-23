package com.block.instagramblocker

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat

object A11yGuard {

    // Requires: adb shell pm grant com.block.instagramblocker android.permission.WRITE_SECURE_SETTINGS
    fun ensureEnabled(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val cr = context.contentResolver
        val me = ComponentName(context, InstagramBlockerService::class.java)
        val enabled = Settings.Secure.getString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            .orEmpty().split(':').filter { it.isNotBlank() }

        if (enabled.none { ComponentName.unflattenFromString(it) == me }) {
            Settings.Secure.putString(
                cr,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                (enabled + me.flattenToString()).joinToString(":")
            )
        }
        if (Settings.Secure.getInt(cr, Settings.Secure.ACCESSIBILITY_ENABLED, 0) != 1) {
            Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_ENABLED, 1)
        }
    }

    fun startWatchdog(context: Context) {
        try {
            ContextCompat.startForegroundService(context, Intent(context, WatchdogService::class.java))
        } catch (e: Exception) {
            // Background start not allowed right now; boot receiver or next trigger will retry.
        }
    }
}
