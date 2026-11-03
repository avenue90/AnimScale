package com.ketangupta.animscale

import android.content.Context
import android.provider.Settings

object AnimationScaleManager {

    val SCALE_KEYS = listOf(
        Settings.Global.WINDOW_ANIMATION_SCALE,
        Settings.Global.TRANSITION_ANIMATION_SCALE,
        Settings.Global.ANIMATOR_DURATION_SCALE,
    )

    fun setScale(context: Context, scale: Float) {
        val resolver = context.contentResolver
        SCALE_KEYS.forEach { key ->
            Settings.Global.putFloat(resolver, key, scale)
        }
    }

    /** True only when all three stored scales equal [scale]. */
    fun allAt(context: Context, scale: Float): Boolean = SCALE_KEYS.all {
        Settings.Global.getFloat(context.contentResolver, it, 1f) == scale
    }

    /**
     * Makes the window manager re-read [scale] even when the stored value is unchanged.
     * Toggling Developer Options (One UI) resets the window manager's applied scales to 1x
     * without writing these settings, and writing an identical value sends no change
     * notification. Alternating between two spellings of the same number ("0.5"/"0.50")
     * always counts as a change, with no visible intermediate value.
     */
    fun reapply(context: Context, scale: Float) {
        val resolver = context.contentResolver
        val plain = scale.toString()
        SCALE_KEYS.forEach { key ->
            val current = Settings.Global.getString(resolver, key)
            Settings.Global.putString(resolver, key, if (current == plain) plain + "0" else plain)
        }
    }

    fun currentScale(context: Context): Float {
        return Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.WINDOW_ANIMATION_SCALE,
            1f
        )
    }

    fun hasPermission(context: Context): Boolean = try {
        val resolver = context.contentResolver
        val current = Settings.Global.getFloat(resolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
        Settings.Global.putFloat(resolver, Settings.Global.WINDOW_ANIMATION_SCALE, current)
        true
    } catch (e: SecurityException) {
        false
    }
}
