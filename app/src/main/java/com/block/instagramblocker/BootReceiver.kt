package com.block.instagramblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        A11yGuard.ensureEnabled(context)
        A11yGuard.startWatchdog(context)
    }
}
