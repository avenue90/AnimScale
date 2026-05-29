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
