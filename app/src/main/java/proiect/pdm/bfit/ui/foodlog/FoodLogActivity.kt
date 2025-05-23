package proiect.pdm.bfit.ui.foodlog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedReader
import proiect.pdm.bfit.R
import proiect.pdm.bfit.data.BFitDatabase
import proiect.pdm.bfit.data.MealEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FoodLogActivity : AppCompatActivity() {

    private lateinit var mealTypeSpinner: Spinner
    private lateinit var foodNameSpinner: Spinner
    private lateinit var quantityEditText: EditText
    private lateinit var addButton: Button
    private lateinit var tvCalories: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvCarbs: TextView
    private lateinit var tvFat: TextView
    private lateinit var tvUnitMeasure:TextView
    data class FoodItem(
        val name: String,
        val calories: Float,
        val protein: Float,
        val carbs: Float,
        val fat: Float
    )

    private lateinit var foodList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_food_log)

        // Inițializări
        mealTypeSpinner = findViewById(R.id.spinnerMealType)
        foodNameSpinner = findViewById(R.id.spinnerFoodName)
        quantityEditText = findViewById(R.id.editTextQuantity)
        addButton = findViewById(R.id.buttonAddFood)
        tvCalories = findViewById(R.id.tvCalories)
        tvProtein = findViewById(R.id.tvProtein)
        tvCarbs = findViewById(R.id.tvCarbs)
        tvFat = findViewById(R.id.tvFat)
        tvUnitMeasure=findViewById(R.id.tvUnitMeasure)




        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val unitPref = prefs.getString("unit_preference", "grams") ?: "grams"

// Presupunem că ai un TextView cu id-ul tvUnitMeasure
        val tvUnitMeasure = findViewById<TextView>(R.id.tvUnitMeasure)
        tvUnitMeasure.text = unitPref
        val quantityText = quantityEditText.text.toString()
        val quantityInput = quantityText.toFloatOrNull() ?: 0f

        val quantityInGrams = if (unitPref == "oz") {
            quantityInput * 28.3495f
        } else {
            quantityInput
        }



// Salvare unitate de măsură (exemplu pentru RadioGroup)
//        radioGroupUnits.setOnCheckedChangeListener { _, checkedId ->
//            val selectedUnit = when (checkedId) {
//                R.id.radioGram -> "g"
//                R.id.radioOunce -> "oz"
//                else -> "g"
//            }
//            prefs.edit().putString("unit_of_measure", selectedUnit).apply()
//        }

        setupMealTypeSpinner()
        loadFoodDataFromJson()

        addButton.setOnClickListener {
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

                // Transformăm unitatea pentru stocare: "grams" -> "g", "oz" -> "oz"
                val unitForDb = if (unit == "grams") "g" else "oz"

                val currentDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                    Date()
                )// format yyyy-MM-dd

                // Presupunem userId-ul este 1 (schimbă după cum ai nevoie)
                val userId = 1L

                val meal = MealEntity(
                    dayDate = currentDate,
                    userId = userId,
                    mealType = mealType,
                    name = foodName,
                    quantity = quantity,
                    unit = unitForDb
                )

                // Salvează în DB folosind corutină
                lifecycleScope.launch(Dispatchers.IO) {
                    val db = BFitDatabase.getDatabase(applicationContext)
                    db.mealDao().insert(meal)

                    // Resetare câmpuri pe UI Thread după salvare
                    launch(Dispatchers.Main) {
                        quantityEditText.text.clear()
                        mealTypeSpinner.setSelection(0)
                        foodNameSpinner.setSelection(0)

                        Toast.makeText(this@FoodLogActivity, "Meal saved!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            Toast.makeText(this, "Meal saved!", Toast.LENGTH_SHORT).show()
            quantityEditText.text.clear()

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

    private lateinit var foodItems: List<FoodItem>

    private fun loadFoodDataFromJson() {
        val inputStream = assets.open("foods.json")
        val jsonString = inputStream.bufferedReader().use(BufferedReader::readText)
        val jsonArray = JSONArray(jsonString)

        foodItems = List(jsonArray.length()) { i ->
            val obj = jsonArray.getJSONObject(i)
            FoodItem(
                name = obj.getString("name"),
                calories = obj.getDouble("calories").toFloat(),
                protein = obj.getDouble("protein").toFloat(),
                carbs = obj.getDouble("carbs").toFloat(),
                fat = obj.getDouble("fat").toFloat()
            )
        }

        val foodNames = foodItems.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, foodNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        foodNameSpinner.adapter = adapter
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

        // Calculăm macronutrienții în grame (bazat pe 100g)
        val calories = foodItem.calories * factor
        val proteinInGrams = foodItem.protein * factor
        val carbsInGrams = foodItem.carbs * factor
        val fatInGrams = foodItem.fat * factor

        if (unitPref == "oz") {
            // Convertim macronutrienții din grame în oz
            val proteinInOz = proteinInGrams / 28.3495f
            val carbsInOz = carbsInGrams / 28.3495f
            val fatInOz = fatInGrams / 28.3495f

            tvCalories.text = "Calories: %.1f".format(calories) // calorii rămân calorii
            tvProtein.text = "Protein: %.2f oz".format(proteinInOz)
            tvCarbs.text = "Carbs: %.2f oz".format(carbsInOz)
            tvFat.text = "Fat: %.2f oz".format(fatInOz)
        } else {
            // Afișăm în grame
            tvCalories.text = "Calories: %.1f".format(calories)
            tvProtein.text = "Protein: %.1f g".format(proteinInGrams)
            tvCarbs.text = "Carbs: %.1f g".format(carbsInGrams)
            tvFat.text = "Fat: %.1f g".format(fatInGrams)
        }
    }



}
