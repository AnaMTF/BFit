package proiect.pdm.bfit.ui.dashboard

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import proiect.pdm.bfit.R
import proiect.pdm.bfit.data.BFitDatabase
import proiect.pdm.bfit.data.DailyStatsEntity
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Initialize database access
        val context = requireContext().applicationContext
        val statsDao = BFitDatabase.getDatabase(context).statsDao()

        // Insert temporary data if no entry exists for today
        lifecycleScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val existing = statsDao.getStatsByDate(today)
            if (existing == null) {
                statsDao.insert(
                    DailyStatsEntity(
                        date = today,
                        steps = 7500,
                        caloriesIntake = 1200,
                        caloriesBurned = 400,
                        waterIntake = 1500,
                        workouts = 2
                    )
                )
            }
        }

        // Setup pie chart
        setupPieChart(view)

        // Setup button click listeners
        setupButtonListeners(view)

        // Observe LiveData for updates
        dashboardViewModel.dailyStats.observe(viewLifecycleOwner) { stats ->
            if (stats != null) {
                view.findViewById<TextView>(R.id.textSteps).text = "${stats.steps} steps today"
                view.findViewById<TextView>(R.id.textWater).text = "${stats.waterIntake} / 2000 ml"
                view.findViewById<TextView>(R.id.textWorkouts).text = "${stats.workouts} workouts today"
                updatePieChart(view, stats.caloriesIntake, stats.caloriesBurned)
            }
        }

        // Observe selected date
        dashboardViewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            view.findViewById<MaterialButton>(R.id.dateSelectorButton).text = 
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(dateFormat.parse(date)!!)
        }

        return view
    }

    private fun setupButtonListeners(view: View) {
        // Date selector button
        view.findViewById<MaterialButton>(R.id.dateSelectorButton).setOnClickListener {
            showDatePicker()
        }

        // Refresh button
        view.findViewById<MaterialButton>(R.id.refreshButton).setOnClickListener {
            dashboardViewModel.refreshStats()
        }

        // Add water button
        view.findViewById<MaterialButton>(R.id.addWaterButton).setOnClickListener {
            dashboardViewModel.addWaterIntake(250)
        }

        // Add steps button
        view.findViewById<MaterialButton>(R.id.addStepsButton).setOnClickListener {
            dashboardViewModel.addSteps(1000)
        }

        // Add workout button
        view.findViewById<MaterialButton>(R.id.addWorkoutButton).setOnClickListener {
            dashboardViewModel.addWorkout()
        }
    }

    private fun showDatePicker() {
        val currentDate = dateFormat.parse(dashboardViewModel.selectedDate.value!!)!!
        val calendar = Calendar.getInstance().apply { time = currentDate }
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                dashboardViewModel.selectDate(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupPieChart(view: View) {
        val pieChart = view.findViewById<com.github.mikephil.charting.charts.PieChart>(R.id.caloriesPieChart)
        pieChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
        }
    }

    private fun updatePieChart(view: View, caloriesConsumed: Int, caloriesBurned: Int) {
        val pieChart = view.findViewById<com.github.mikephil.charting.charts.PieChart>(R.id.caloriesPieChart)
        
        val entries = listOf(
            PieEntry(caloriesConsumed.toFloat(), "Consumed"),
            PieEntry(caloriesBurned.toFloat(), "Burned")
        )

        val dataSet = PieDataSet(entries, "Calories")
        dataSet.colors = listOf(
            Color.rgb(255, 99, 71), // Tomato color for consumed
            Color.rgb(144, 238, 144) // Light green for burned
        )

        val data = PieData(dataSet)
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.WHITE)

        pieChart.data = data
        pieChart.animateY(1000)
        pieChart.invalidate()
    }
}
