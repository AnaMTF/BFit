package proiect.pdm.bfit.ui.dashboard

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import proiect.pdm.bfit.MainActivity
import proiect.pdm.bfit.R
import proiect.pdm.bfit.data.BFitDatabase
import proiect.pdm.bfit.data.DailyStatsEntity
import proiect.pdm.bfit.data.MealEntity
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val TAG = "DashboardFragment"
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private lateinit var breakfastAdapter: MealAdapter
    private lateinit var lunchAdapter: MealAdapter
    private lateinit var dinnerAdapter: MealAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Get user ID from MainActivity
        val mainActivity = requireActivity() as MainActivity
        val userId = mainActivity.userId
        dashboardViewModel.setUserId(userId)

        // Initialize database access
        val context = requireContext().applicationContext
        val statsDao = BFitDatabase.getDatabase(context).statsDao()
        val mealDao = BFitDatabase.getDatabase(context).mealDao()

        // Setup RecyclerViews
        setupRecyclerViews(view)

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
            
            // Update meals for the selected date
            lifecycleScope.launch {
                Log.d(TAG, "Fetching meals for date: $date")
                val userId = (requireActivity() as MainActivity).userId
                val meals = BFitDatabase.getDatabase(requireContext()).mealDao().getMealsByDate(date, userId)
                Log.d(TAG, "Retrieved meals for date $date: $meals")
                
                val breakfastMeals = meals.filter { it.mealType == "Breakfast" }
                val lunchMeals = meals.filter { it.mealType == "Lunch" }
                val dinnerMeals = meals.filter { it.mealType == "Dinner" }
                
                Log.d(TAG, "Breakfast meals: $breakfastMeals")
                Log.d(TAG, "Lunch meals: $lunchMeals")
                Log.d(TAG, "Dinner meals: $dinnerMeals")
                
                breakfastAdapter.updateMeals(breakfastMeals)
                lunchAdapter.updateMeals(lunchMeals)
                dinnerAdapter.updateMeals(dinnerMeals)
                
                dashboardViewModel.refreshStats()
            }
        }

        return view
    }

    private fun setupRecyclerViews(view: View) {
        // Breakfast RecyclerView
        val breakfastRecyclerView = view.findViewById<RecyclerView>(R.id.breakfastRecyclerView)
        breakfastRecyclerView.layoutManager = LinearLayoutManager(context)
        breakfastAdapter = MealAdapter(emptyList()) { meal ->
            lifecycleScope.launch {
                BFitDatabase.getDatabase(requireContext()).mealDao().deleteMeal(meal)
                dashboardViewModel.refreshStats()
            }
        }
        breakfastRecyclerView.adapter = breakfastAdapter

        // Lunch RecyclerView
        val lunchRecyclerView = view.findViewById<RecyclerView>(R.id.lunchRecyclerView)
        lunchRecyclerView.layoutManager = LinearLayoutManager(context)
        lunchAdapter = MealAdapter(emptyList()) { meal ->
            lifecycleScope.launch {
                BFitDatabase.getDatabase(requireContext()).mealDao().deleteMeal(meal)
                dashboardViewModel.refreshStats()
            }
        }
        lunchRecyclerView.adapter = lunchAdapter

        // Dinner RecyclerView
        val dinnerRecyclerView = view.findViewById<RecyclerView>(R.id.dinnerRecyclerView)
        dinnerRecyclerView.layoutManager = LinearLayoutManager(context)
        dinnerAdapter = MealAdapter(emptyList()) { meal ->
            lifecycleScope.launch {
                BFitDatabase.getDatabase(requireContext()).mealDao().deleteMeal(meal)
                dashboardViewModel.refreshStats()
            }
        }
        dinnerRecyclerView.adapter = dinnerAdapter
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
            showWaterDialog()
        }

        // Add steps button
        view.findViewById<MaterialButton>(R.id.addStepsButton).setOnClickListener {
            showStepsDialog()
        }

        // Add workout button
        view.findViewById<MaterialButton>(R.id.addWorkoutButton).setOnClickListener {
            showWorkoutDialog()
        }

        // Add meal buttons
        view.findViewById<MaterialButton>(R.id.addBreakfastButton).setOnClickListener {
            showMealDialog("Breakfast")
        }

        view.findViewById<MaterialButton>(R.id.addLunchButton).setOnClickListener {
            showMealDialog("Lunch")
        }

        view.findViewById<MaterialButton>(R.id.addDinnerButton).setOnClickListener {
            showMealDialog("Dinner")
        }
    }

    private fun showMealDialog(mealType: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_meal_input, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.mealNameInput)
        val quantityInput = dialogView.findViewById<EditText>(R.id.mealQuantityInput)
        val unitInput = dialogView.findViewById<EditText>(R.id.mealUnitInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Add $mealType")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString()
                val quantity = quantityInput.text.toString().toFloatOrNull() ?: 0f
                val unit = unitInput.text.toString()
                
                lifecycleScope.launch {
                    val userId = (requireActivity() as MainActivity).userId
                    val meal = MealEntity(
                        dayDate = dashboardViewModel.selectedDate.value!!,
                        userId = userId,
                        mealType = mealType,
                        name = name,
                        quantity = quantity,
                        unit = unit
                    )
                    Log.d(TAG, "Adding meal: $meal")
                    BFitDatabase.getDatabase(requireContext()).mealDao().insert(meal)
                    
                    // Immediately refresh the meals for the current date
                    val meals = BFitDatabase.getDatabase(requireContext()).mealDao().getMealsByDate(dashboardViewModel.selectedDate.value!!, userId)
                    Log.d(TAG, "Retrieved meals: $meals")
                    
                    val breakfastMeals = meals.filter { it.mealType == "Breakfast" }
                    val lunchMeals = meals.filter { it.mealType == "Lunch" }
                    val dinnerMeals = meals.filter { it.mealType == "Dinner" }
                    
                    breakfastAdapter.updateMeals(breakfastMeals)
                    lunchAdapter.updateMeals(lunchMeals)
                    dinnerAdapter.updateMeals(dinnerMeals)
                    
                    dashboardViewModel.refreshStats()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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

    private fun showWaterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_water_input, null)
        val waterInput = dialogView.findViewById<EditText>(R.id.waterInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Water Intake")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val amount = waterInput.text.toString().toIntOrNull() ?: 250
                dashboardViewModel.addWaterIntake(amount)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showStepsDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_steps_input, null)
        val stepsInput = dialogView.findViewById<EditText>(R.id.stepsInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Steps")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val steps = stepsInput.text.toString().toIntOrNull() ?: 1000
                dashboardViewModel.addSteps(steps)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showWorkoutDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_workout_input, null)
        val workoutTypeInput = dialogView.findViewById<EditText>(R.id.workoutTypeInput)
        val durationInput = dialogView.findViewById<EditText>(R.id.durationInput)
        val caloriesInput = dialogView.findViewById<EditText>(R.id.caloriesInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Workout")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val workoutType = workoutTypeInput.text.toString()
                val duration = durationInput.text.toString().toIntOrNull() ?: 30
                val calories = caloriesInput.text.toString().toIntOrNull() ?: 200
                dashboardViewModel.addWorkout(workoutType, duration, calories)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupPieChart(view: View) {
        val pieChart = view.findViewById<com.github.mikephil.charting.charts.PieChart>(R.id.caloriesPieChart)
        pieChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(16f)
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
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 18f
        dataSet.valueTypeface = android.graphics.Typeface.DEFAULT_BOLD

        val data = PieData(dataSet)
        data.setValueTextSize(18f)
        data.setValueTextColor(Color.BLACK)
        data.setValueTypeface(android.graphics.Typeface.DEFAULT_BOLD)

        pieChart.data = data
        pieChart.animateY(1000)
        pieChart.invalidate()
    }
}
