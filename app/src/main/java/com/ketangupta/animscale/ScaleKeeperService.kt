package com.ketangupta.animscale

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat

class ScaleKeeperService : Service() {

    companion object {
        const val PREFS = "anim_prefs"
        const val KEY_SCALE = "target_scale"
        private const val EXTRA_SCALE = "scale"
        private const val CHANNEL_ID = "scale_keeper"
        private const val NOTIF_ID = 1

        fun start(context: Context, scale: Float) {
            context.getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putFloat(KEY_SCALE, scale).apply()
            val intent = Intent(context, ScaleKeeperService::class.java).putExtra(EXTRA_SCALE, scale)
            ContextCompat.startForegroundService(context, intent)
        }

        fun savedScale(context: Context): Float? {
            val prefs = context.getSharedPreferences(PREFS, MODE_PRIVATE)
            return if (prefs.contains(KEY_SCALE)) prefs.getFloat(KEY_SCALE, 1f) else null
        }
    }

    private var targetScale = 1f
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var observer: ContentObserver

    private val restoreRunnable = Runnable {
        if (!AnimationScaleManager.hasPermission(this)) return@Runnable
        if (AnimationScaleManager.currentScale(this) != targetScale) {
            AnimationScaleManager.setScale(this, targetScale)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        targetScale = intent?.getFloatExtra(EXTRA_SCALE,
            getSharedPreferences(PREFS, MODE_PRIVATE).getFloat(KEY_SCALE, 1f)
        ) ?: getSharedPreferences(PREFS, MODE_PRIVATE).getFloat(KEY_SCALE, 1f)

        createChannel()
        startForeground(NOTIF_ID, buildNotification())

        // Apply immediately
        if (AnimationScaleManager.hasPermission(this)) {
            AnimationScaleManager.setScale(this, targetScale)
        }

        // Watch all scale keys + developer options toggle
        observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                handler.removeCallbacks(restoreRunnable)
                handler.postDelayed(restoreRunnable, 300)
            }
        }
        AnimationScaleManager.SCALE_KEYS.forEach { key ->
            contentResolver.registerContentObserver(Settings.Global.getUriFor(key), false, observer)
        }
        contentResolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED), false, observer
        )

        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(restoreRunnable)
        contentResolver.unregisterContentObserver(observer)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Animation Scale", NotificationManager.IMPORTANCE_MIN)
        channel.setShowBadge(false)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val label = when (targetScale) {
            0f -> "off (0x)"
            1f -> "1x"
            else -> "${targetScale}x"
        }
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Animation scale locked at $label")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setOngoing(true)
            .build()
    }
}
