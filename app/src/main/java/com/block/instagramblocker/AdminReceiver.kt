package com.block.instagramblocker

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Device Admin receiver. When provisioned as Device Owner via ADB, this is the
 * component that holds the admin policies.
 */
class AdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "FocusGuard admin activated", Toast.LENGTH_SHORT).show()
    }
}
