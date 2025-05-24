package proiect.pdm.bfit.ui.foodlog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import proiect.pdm.bfit.R
import proiect.pdm.bfit.data.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class FoodLogActivity : AppCompatActivity() {

    @Inject
    lateinit var foodRepository: FoodRepository

    private lateinit var mealTypeSpinner: Spinner
    private lateinit var foodNameSpinner: Spinner
    private lateinit var quantityEditText: EditText
    private lateinit var addButton: Button
    private lateinit var tvCalories: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvCarbs: TextView
    private lateinit var tvFat: TextView
    private lateinit var tvUnitMeasure: TextView
    private lateinit var statsRepository: StatsRepository
    private lateinit var foodItems: List<FoodEntity>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_food_log)

        statsRepository = StatsRepository(BFitDatabase.getDatabase(this).statsDao())

        // Initialize views
        mealTypeSpinner = findViewById(R.id.spinnerMealType)
        foodNameSpinner = findViewById(R.id.spinnerFoodName)
        quantityEditText = findViewById(R.id.editTextQuantity)
        addButton = findViewById(R.id.buttonAddFood)
        tvCalories = findViewById(R.id.tvCalories)
        tvProtein = findViewById(R.id.tvProtein)
        tvCarbs = findViewById(R.id.tvCarbs)
        tvFat = findViewById(R.id.tvFat)
        tvUnitMeasure = findViewById(R.id.tvUnitMeasure)

        setupMealTypeSpinner()
        loadFoodData()

        addButton.setOnClickListener {
            val mealType = mealTypeSpinner.selectedItem as String
            val foodName = foodNameSpinner.selectedItem as String
            val quantityStr = quantityEditText.text.toString()

            if (quantityStr.isBlank()) {
                Toast.makeText(this, "Please enter a quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val quantity = quantityStr.toFloatOrNull()
            if (quantity == null || quantity <= 0) {
                Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val unit = prefs.getString("unit_preference", "grams") ?: "grams"
            val unitForDb = if (unit == "grams") "g" else "oz"

            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val userId = intent.getLongExtra("USER_ID", -1)
            if (userId == -1L) {
                Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Calculate calories for the meal
            val foodItem = foodItems.find { it.name == foodName }
            if (foodItem == null) {
                Toast.makeText(this, "Invalid food item", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val quantityInGrams = if (unit == "oz") {
                quantity * 28.3495f
            } else {
                quantity
            }
            val factor = quantityInGrams / 100f
            val calories = (foodItem.calories * factor).toInt()

            val meal = MealEntity(
                dayDate = currentDate,
                userId = userId,
                mealType = mealType,
                name = foodName,
                quantity = quantity,
                unit = unitForDb
            )

            lifecycleScope.launch {
                val db = BFitDatabase.getDatabase(applicationContext)
                db.mealDao().insert(meal)

                // Update daily stats
                val currentStats = statsRepository.getDailyStatsByDate(currentDate) ?: DailyStatsEntity(
                    date = currentDate,
                    steps = 0,
                    caloriesIntake = 0,
                    caloriesBurned = 0,
                    waterIntake = 0,
                    workouts = 0
                )

                val updatedStats = currentStats.copy(
                    caloriesIntake = currentStats.caloriesIntake + calories
                )
                statsRepository.addDailyStats(updatedStats)

                runOnUiThread {
                    quantityEditText.text.clear()
                    mealTypeSpinner.setSelection(0)
                    foodNameSpinner.setSelection(0)
                    Toast.makeText(this@FoodLogActivity, "Meal saved!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        foodNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateNutritionDisplay()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        quantityEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateNutritionDisplay()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupMealTypeSpinner() {
        val mealTypes = listOf("Breakfast", "Lunch", "Dinner")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mealTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mealTypeSpinner.adapter = adapter
    }

    private fun loadFoodData() {
        lifecycleScope.launch {
            foodItems = foodRepository.getAllFoods()
            val foodNames = foodItems.map { it.name }
            val adapter = ArrayAdapter(this@FoodLogActivity, android.R.layout.simple_spinner_item, foodNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            foodNameSpinner.adapter = adapter
        }
    }

    private fun updateNutritionDisplay() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val unitPref = prefs.getString("unit_preference", "grams") ?: "grams"
        tvUnitMeasure.text = unitPref

        val selectedFoodName = foodNameSpinner.selectedItem as? String ?: return
        val foodItem = foodItems.find { it.name == selectedFoodName } ?: return

        val quantityText = quantityEditText.text.toString()
        val quantityInput = quantityText.toFloatOrNull() ?: 0f

        val quantityInGrams = if (unitPref == "oz") {
            quantityInput * 28.3495f
        } else {
            quantityInput
        }

        val factor = quantityInGrams / 100f

        val calories = foodItem.calories * factor
        val proteinInGrams = foodItem.protein * factor
        val carbsInGrams = foodItem.carbs * factor
        val fatInGrams = foodItem.fat * factor

        if (unitPref == "oz") {
            val proteinInOz = proteinInGrams / 28.3495f
            val carbsInOz = carbsInGrams / 28.3495f
            val fatInOz = fatInGrams / 28.3495f

            tvCalories.text = "Calories: %.1f".format(calories)
            tvProtein.text = "Protein: %.2f oz".format(proteinInOz)
            tvCarbs.text = "Carbs: %.2f oz".format(carbsInOz)
            tvFat.text = "Fat: %.2f oz".format(fatInOz)
        } else {
            tvCalories.text = "Calories: %.1f".format(calories)
            tvProtein.text = "Protein: %.1f g".format(proteinInGrams)
            tvCarbs.text = "Carbs: %.1f g".format(carbsInGrams)
            tvFat.text = "Fat: %.1f g".format(fatInGrams)
        }
    }
}
