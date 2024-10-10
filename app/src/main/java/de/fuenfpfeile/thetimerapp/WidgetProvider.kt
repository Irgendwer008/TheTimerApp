package de.fuenfpfeile.thetimerapp

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

class WidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Start the update service to refresh the widget
        context.startService(Intent(context, UpdateService::class.java))
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        // Optionally stop the service when the widget is removed from the home screen
        context?.stopService(Intent(context, UpdateService::class.java))
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
    }
}
