package com.ketangupta.animscale

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ketangupta.animscale.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermission()
        updateStatus()

        binding.btnGrantPermission.setOnClickListener { tryAutoGrant() }
        binding.btnOff.setOnClickListener    { applyScale(0f,   "Off (0x)") }
        binding.btnPoint2.setOnClickListener { applyScale(0.2f, "0.2x") }
        binding.btnHalf.setOnClickListener   { applyScale(0.5f, "0.5x") }
        binding.btnPoint8.setOnClickListener { applyScale(0.8f, "0.8x") }
        binding.btnOne.setOnClickListener    { applyScale(1.0f, "1x") }
        binding.btnTwo.setOnClickListener    { applyScale(2.0f, "2x") }
    }

    override fun onResume() {
        super.onResume()
        // Opening the app resumes a saved lock if the service was stopped.
        ScaleKeeperService.savedScale(this)?.let { if (AnimationScaleManager.hasPermission(this)) ScaleKeeperService.start(this, it) }
        updateStatus()
    }

    private fun applyScale(scale: Float, label: String) {
        if (!AnimationScaleManager.hasPermission(this)) {
            tryAutoGrant()
            return
        }
        try {
            AnimationScaleManager.setScale(this, scale)
            ScaleKeeperService.start(this, scale)
            updateStatus()
            Toast.makeText(this, "Animations locked to $label", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            showPermissionHelp()
        }
    }

    private fun tryAutoGrant() {
        binding.btnGrantPermission.isEnabled = false
        binding.tvPermissionStatus.text = "Granting permission…"
        Thread {
            // Try root, then bare pm (works on some AOSP images)
            for (cmd in listOf(
                arrayOf("su", "0", "pm", "grant", packageName, "android.permission.SET_ANIMATION_SCALE"),
                arrayOf("pm", "grant", packageName, "android.permission.SET_ANIMATION_SCALE"),
            )) {
                runCatching { Runtime.getRuntime().exec(cmd).waitFor() }
                if (AnimationScaleManager.hasPermission(this)) break
            }
            runOnUiThread {
                binding.btnGrantPermission.isEnabled = true
                if (AnimationScaleManager.hasPermission(this)) {
                    updateStatus()
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show()
                } else {
                    updateStatus()
                    showPermissionHelp()
                }
            }
        }.start()
    }

    private fun updateStatus() {
        val current = AnimationScaleManager.currentScale(this)
        val label = when (current) {
            0f   -> "Off (0x)"
            0.2f -> "0.2x (very fast)"
            0.5f -> "0.5x (fast)"
            0.8f -> "0.8x (slightly fast)"
            1f   -> "1x (default)"
            2f   -> "2x (slow)"
            else -> "${current}x"
        }
        binding.tvCurrentScale.text = "Current: $label"

        val hasPermission = AnimationScaleManager.hasPermission(this)
        binding.tvPermissionStatus.text = if (hasPermission) "Permission: granted" else "Permission: not granted"
        binding.btnGrantPermission.visibility = if (hasPermission) View.GONE else View.VISIBLE
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }

    private fun showPermissionHelp() {
        val command = "adb shell pm grant $packageName android.permission.SET_ANIMATION_SCALE"
        android.app.AlertDialog.Builder(this)
            .setTitle("One-time setup required")
            .setMessage(
                "Run setup.sh from the project root — it builds, installs, and grants in one go:\n\n" +
                "    ./setup.sh\n\n" +
                "Or run just the grant step if the app is already installed:\n\n" +
                "    $command\n\n" +
                "Permission persists across reboots and updates — this is a one-time step."
            )
            .setPositiveButton("Copy Grant Command") { _, _ ->
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("adb command", command))
                Toast.makeText(this, "Command copied to clipboard", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Dismiss", null)
            .show()
    }
}
