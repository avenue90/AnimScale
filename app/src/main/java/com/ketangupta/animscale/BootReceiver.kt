package com.ketangupta.animscale

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // After a reboot or an app update (which stops the service), resume the saved lock.
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val scale = ScaleKeeperService.savedScale(context) ?: return
        ScaleKeeperService.start(context, scale)
    }
}
