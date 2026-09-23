package com.block.instagramblocker

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Visible control screen (Option A: the app stays in your launcher and app list,
 * and stays removable from a computer via ADB).
 *
 * When the app is Device Owner it:
 *   1. Blocks its own uninstall from the phone UI (so a weak-moment you can't
 *      just tap Uninstall) — but it is still removable via ADB from a computer.
 *   2. Hides Instagram at the OS level where the ROM allows it.
 * The accessibility service is the always-on fallback block.
 */
class MainActivity : AppCompatActivity() {

    private val instagramPackages = listOf(
        "com.instagram.android",
        "com.instagram.lite"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(this, AdminReceiver::class.java)
        val isOwner = dpm.isDeviceOwnerApp(packageName)

        if (isOwner) {
            // 1. Block uninstall from the phone UI (still removable via ADB).
            dpm.setUninstallBlocked(admin, packageName, true)

            // 2. Hide Instagram at the OS level where supported.
            for (ig in instagramPackages) {
                try {
                    dpm.setApplicationHidden(admin, ig, true)
                } catch (e: Exception) {
                    // Not installed, or blocked by the ROM — the accessibility
                    // service still bounces you out.
                }
            }
        }

        val status = findViewById<TextView>(R.id.txtStatus)
        status.text = if (isOwner) {
            getString(R.string.status_active)
        } else {
            getString(R.string.status_not_owner)
        }

        findViewById<Button>(R.id.btnAccessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(
                this,
                "Turn FocusGuard ON, then press back",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
