package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class RateAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val usd = prefs.getFloat(KEY_USD, 876.80f).toDouble()
        val eur = prefs.getFloat(KEY_EUR, 982.00f).toDouble()
        val usdt = prefs.getFloat(KEY_USDT, 1008.35f).toDouble()

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, usd, eur, usdt)
        }
    }

    companion object {
        const val PREFS_NAME = "tasacambio_widget_prefs"
        const val KEY_USD = "key_usd"
        const val KEY_EUR = "key_eur"
        const val KEY_USDT = "key_usdt"

        private val df: DecimalFormat by lazy {
            val symbols = DecimalFormatSymbols(Locale("es", "VE")).apply {
                decimalSeparator = ','
                groupingSeparator = '.'
            }
            DecimalFormat("#,##0.00", symbols)
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            usdRate: Double,
            eurRate: Double,
            usdtRate: Double
        ) {
            val views = RemoteViews(context.packageName, R.layout.rate_appwidget_layout)

            views.setTextViewText(R.id.widget_usd_rate, df.format(usdRate))
            views.setTextViewText(R.id.widget_eur_rate, df.format(eurRate))
            views.setTextViewText(R.id.widget_usdt_rate, df.format(usdtRate))

            // Click to launch MainActivity
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(
            context: Context,
            usdRate: Double,
            eurRate: Double,
            usdtRate: Double
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putFloat(KEY_USD, usdRate.toFloat())
                .putFloat(KEY_EUR, eurRate.toFloat())
                .putFloat(KEY_USDT, usdtRate.toFloat())
                .apply()

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, RateAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, usdRate, eurRate, usdtRate)
            }
        }
    }
}
