package proiect.pdm.bfit.ui.workoutlog

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import proiect.pdm.bfit.R
import java.io.InputStreamReader
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import proiect.pdm.bfit.data.BFitDatabase
import proiect.pdm.bfit.data.WorkoutEntity
import proiect.pdm.bfit.data.DailyStatsEntity
import proiect.pdm.bfit.data.StatsRepository

data class WorkoutDefinition(
    val name: String,
    val caloriesPerMinute: Int,
    val image: String
)

@AndroidEntryPoint
class WorkoutLogActivity : AppCompatActivity() {

    private lateinit var workouts: List<WorkoutDefinition>
    private lateinit var spinner: Spinner
    private lateinit var imgWorkout: ImageView
    private lateinit var etMinutes: EditText
    private lateinit var btnSave: Button
    private lateinit var statsRepository: StatsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_workout_log)

        statsRepository = StatsRepository(BFitDatabase.getDatabase(this).statsDao())

        spinner = findViewById(R.id.spinnerWorkouts)
        imgWorkout = findViewById(R.id.imgWorkout)
        etMinutes = findViewById(R.id.etMinutes)
        btnSave = findViewById(R.id.btnSaveWorkout)

        loadWorkoutsFromJson()

        val workoutNames = workouts.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, workoutNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        val tvTotalCalories = findViewById<TextView>(R.id.tvTotalCalories)

        fun updateCalories() {
            val minutes = etMinutes.text.toString().toIntOrNull() ?: 0
            val selected = workouts[spinner.selectedItemPosition]
            val totalCalories = selected.caloriesPerMinute * minutes
            tvTotalCalories.text = "Total Calories Burned: $totalCalories"
        }

        etMinutes.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateCalories()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedWorkout = workouts[position]
                val imageResId = resources.getIdentifier(
                    selectedWorkout.image.removeSuffix(".png"),
                    "drawable",
                    packageName
                )
                imgWorkout.setImageResource(imageResId)
                updateCalories()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnSave.setOnClickListener {
            val selected = workouts[spinner.selectedItemPosition]
            val minutes = etMinutes.text.toString().toIntOrNull()

            if (minutes == null || minutes <= 0) {
                Toast.makeText(this, "Please enter valid minutes", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalCalories = selected.caloriesPerMinute * minutes

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = sdf.format(Date())

            val userId = intent.getLongExtra("USER_ID", -1)
            if (userId == -1L) {
                Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val workout = WorkoutEntity(
                dayDate = today,
                name = selected.name,
                durationMinutes = minutes,
                caloriesBurned = totalCalories,
                userId = userId
            )

            lifecycleScope.launch {
                val dao = BFitDatabase.getDatabase(this@WorkoutLogActivity).workoutDao()
                dao.insert(workout)

                // Update daily stats
                val currentStats = statsRepository.getDailyStatsByDate(today) ?: DailyStatsEntity(
                    date = today,
                    steps = 0,
                    caloriesIntake = 0,
                    caloriesBurned = 0,
                    waterIntake = 0,
                    workouts = 0
                )

                val updatedStats = currentStats.copy(
                    caloriesBurned = currentStats.caloriesBurned + totalCalories,
                    workouts = currentStats.workouts + 1
                )
                statsRepository.addDailyStats(updatedStats)

                Toast.makeText(this@WorkoutLogActivity, "Workout saved!", Toast.LENGTH_SHORT).show()
                etMinutes.text.clear()
            }
        }
    }

    private fun loadWorkoutsFromJson() {
        val inputStream = assets.open("workouts.json")
        val reader = InputStreamReader(inputStream)
        val type = object : TypeToken<List<WorkoutDefinition>>() {}.type
        workouts = Gson().fromJson(reader, type)
        reader.close()
    }
}
