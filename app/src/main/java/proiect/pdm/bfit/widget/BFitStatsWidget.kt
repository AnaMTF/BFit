package proiect.pdm.bfit.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import proiect.pdm.bfit.R
import proiect.pdm.bfit.data.BFitDatabase
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class BFitStatsWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_bfit_stats)
        
        CoroutineScope(Dispatchers.IO).launch {
            val statsDao = BFitDatabase.getDatabase(context).statsDao()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val stats = statsDao.getStatsByDate(today)

            views.setTextViewText(R.id.widget_steps, "Steps: ${stats?.steps ?: 0}")
            views.setTextViewText(R.id.widget_water, "Water: ${stats?.waterIntake ?: 0}/2000 ml")
            views.setTextViewText(R.id.widget_calories, "Calories: ${stats?.caloriesBurned ?: 0}")
            views.setTextViewText(R.id.widget_workouts, "Workouts: ${stats?.workouts ?: 0}")

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
} 