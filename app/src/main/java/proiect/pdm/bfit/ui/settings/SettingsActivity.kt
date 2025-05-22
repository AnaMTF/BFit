package proiect.pdm.bfit.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import proiect.pdm.bfit.R
import proiect.pdm.bfit.data.BFitDatabase
import proiect.pdm.bfit.data.UserDao

class SettingsActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao
    private lateinit var prefs: SharedPreferences
    private lateinit var radioGroup: RadioGroup
    private lateinit var saveButton: Button
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var heightEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var ageEditText: EditText
    private var userId: Long = 0

    private lateinit var activityLevelSpinner: Spinner
    private lateinit var goalSpinner: Spinner
    val activityLevels = listOf("Sedentary", "Lightly active", "Moderately active", "Very active", "Super active")
    val goals = listOf("Lose weight", "Maintain weight", "Gain muscle")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        usernameEditText = findViewById(R.id.editUsername)
        passwordEditText = findViewById(R.id.editPassword)
        heightEditText = findViewById(R.id.editHeight)
        weightEditText = findViewById(R.id.editWeight)
        ageEditText = findViewById(R.id.editAge)
        saveButton = findViewById(R.id.buttonSaveSettings)
        val db = BFitDatabase.getDatabase(applicationContext)
        userDao = db.userDao()
        userId = intent.getLongExtra("USER_ID", -1)

        if (userId == -1L) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show()
            finish()
        }


        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        activityLevelSpinner = findViewById(R.id.spinnerActivityLevel)
        goalSpinner = findViewById(R.id.spinnerGoal)

        activityLevelSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityLevels).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        goalSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, goals).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        radioGroup = findViewById(R.id.radioGroupUnits)
        saveButton = findViewById(R.id.buttonSaveSettings)

        // Încarcă preferința salvată și setează radio button-ul corect
        val unit = prefs.getString("unit_preference", "grams")
        when (unit) {
            "grams" -> radioGroup.check(R.id.radioGram)
            "oz" -> radioGroup.check(R.id.radioOunce)
        }

        saveButton.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            val selectedUnit = when (selectedId) {
                R.id.radioGram -> "grams"
                R.id.radioOunce -> "oz"
                else -> "grams"
            }

            // Salvează preferința de unitate
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            prefs.edit().putString("unit_preference", selectedUnit).apply()

            // Preia datele introduse de utilizator
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val height = heightEditText.text.toString().toIntOrNull() ?: 0
            val weight = weightEditText.text.toString().toIntOrNull() ?: 0
            val age = ageEditText.text.toString().toIntOrNull() ?: 0
            val selectedGoal = goalSpinner.selectedItem.toString()
            val selectedActivityLevel = activityLevelSpinner.selectedItem.toString()

            // Actualizează în BD
            lifecycleScope.launch {
                val existingUser = userDao.getUserById(userId)
                val updatedUser = existingUser?.copy(
                    username = username,
                    password = password,
                    heightCm = height,
                    weightKg = weight,
                    age = age,
                    goal = selectedGoal,
                    activityLevel = selectedActivityLevel
                )
                if (updatedUser != null) {
                    userDao.update(updatedUser)
                    Log.d("SETTINGS", "Updating user with ID = ${updatedUser.id}, new username = ${updatedUser.username}")
                    prefs.edit().putString("logged_in_username", updatedUser.username).apply()
                    prefs.edit().putString("logged_in_password", updatedUser.password).apply()

                }

//                Toast.makeText(this@SettingsActivity, "Date salvate", Toast.LENGTH_SHORT).show()
                val resultIntent = Intent()
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

    }
}

