package com.ketangupta.animscale

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class AnimScaleWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_SET_HALF = "com.ketangupta.animscale.ACTION_SET_HALF"
        const val ACTION_SET_ONE = "com.ketangupta.animscale.ACTION_SET_ONE"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, AnimScaleWidget::class.java))
            if (ids.isNotEmpty()) {
                val provider = AnimScaleWidget()
                provider.onUpdate(context, manager, ids)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id ->
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_SET_HALF -> {
                AnimationScaleManager.setScale(context, 0.5f)
                updateAllWidgets(context)
            }
            ACTION_SET_ONE -> {
                AnimationScaleManager.setScale(context, 1.0f)
                updateAllWidgets(context)
            }
        }
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        val currentScale = AnimationScaleManager.currentScale(context)
        val scaleLabel = when {
            currentScale == 0f -> "Off"
            currentScale <= 0.5f -> "0.5x"
            else -> "1x"
        }
        views.setTextViewText(R.id.tv_widget_scale, scaleLabel)

        val halfIntent = Intent(context, AnimScaleWidget::class.java).apply {
            action = ACTION_SET_HALF
        }
        val onePendingIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, AnimScaleWidget::class.java).apply { action = ACTION_SET_ONE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val halfPendingIntent = PendingIntent.getBroadcast(
            context, 1, halfIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(R.id.btn_widget_half, halfPendingIntent)
        views.setOnClickPendingIntent(R.id.btn_widget_one, onePendingIntent)

        manager.updateAppWidget(widgetId, views)
    }
}
