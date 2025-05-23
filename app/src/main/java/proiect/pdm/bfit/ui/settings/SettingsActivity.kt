package proiect.pdm.bfit.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import proiect.pdm.bfit.R
import proiect.pdm.bfit.data.BFitDatabase
import proiect.pdm.bfit.data.UserDao

@AndroidEntryPoint
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
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    private lateinit var imageViewProfilePic: ImageView
    private lateinit var buttonChoosePic: Button
    private lateinit var activityLevelSpinner: Spinner
    private lateinit var goalSpinner: Spinner
    private val activityLevels = listOf("Sedentary", "Lightly active", "Moderately active", "Very active", "Super active")
    private val goals = listOf("Lose weight", "Maintain weight", "Gain muscle")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_settings)

        // Initialize views and DAO
        usernameEditText = findViewById(R.id.editUsername)
        passwordEditText = findViewById(R.id.editPassword)
        heightEditText = findViewById(R.id.editHeight)
        weightEditText = findViewById(R.id.editWeight)
        ageEditText = findViewById(R.id.editAge)
        saveButton = findViewById(R.id.buttonSaveSettings)
        imageViewProfilePic = findViewById(R.id.imageViewProfilePic)
        buttonChoosePic = findViewById(R.id.buttonChoosePic)
        activityLevelSpinner = findViewById(R.id.spinnerActivityLevel)
        goalSpinner = findViewById(R.id.spinnerGoal)

        val db = BFitDatabase.getDatabase(applicationContext)
        userDao = db.userDao()

        // SharedPreferences
        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // Set up spinners
        activityLevelSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityLevels).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        goalSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, goals).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Fetch userId
        userId = intent.getLongExtra("USER_ID", -1)
        if (userId == -1L) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize image picker
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data?.data != null) {
                val imageUri = result.data!!.data!!
                contentResolver.takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                prefs.edit().putString("profile_image_uri", imageUri.toString()).apply()
                Glide.with(this).load(imageUri).into(imageViewProfilePic)
            }
        }
        buttonChoosePic.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            pickImageLauncher.launch(intent)
        }

        // Load preferences and user data
        setupUnitRadio()
        loadProfileImage()
        loadUserData()

        // Save button action
        saveButton.setOnClickListener {
            saveSettings()
        }
    }

    private fun setupUnitRadio() {
        radioGroup = findViewById(R.id.radioGroupUnits)
        val unit = prefs.getString("unit_preference", "grams")
        when (unit) {
            "grams" -> radioGroup.check(R.id.radioGram)
            "oz" -> radioGroup.check(R.id.radioOunce)
        }
    }

    private fun loadProfileImage() {
        val uriString = prefs.getString("profile_image_uri", null)
        uriString?.let {
            val uri = Uri.parse(it)
            Glide.with(this).load(uri).into(imageViewProfilePic)
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val user = userDao.getUserById(userId)
            user?.let {
                usernameEditText.setText(it.username)
                passwordEditText.setText(it.password)
                heightEditText.setText(it.heightCm.toString())
                weightEditText.setText(it.weightKg.toString())
                ageEditText.setText(it.age.toString())

                // Set spinner selections
                val actIndex = activityLevels.indexOf(it.activityLevel)
                if (actIndex >= 0) activityLevelSpinner.setSelection(actIndex)
                val goalIndex = goals.indexOf(it.goal)
                if (goalIndex >= 0) goalSpinner.setSelection(goalIndex)
            }
        }
    }

    private fun saveSettings() {
        val selectedUnit = when (radioGroup.checkedRadioButtonId) {
            R.id.radioGram -> "grams"
            R.id.radioOunce -> "oz"
            else -> "grams"
        }
        prefs.edit().putString("unit_preference", selectedUnit).apply()

        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val height = heightEditText.text.toString().toIntOrNull() ?: 0
        val weight = weightEditText.text.toString().toIntOrNull() ?: 0
        val age = ageEditText.text.toString().toIntOrNull() ?: 0
        val selectedGoal = goalSpinner.selectedItem.toString()
        val selectedActivityLevel = activityLevelSpinner.selectedItem.toString()

        lifecycleScope.launch {
            val existingUser = userDao.getUserById(userId)
            existingUser?.let {
                val updatedUser = it.copy(
                    username = username,
                    password = password,
                    heightCm = height,
                    weightKg = weight,
                    age = age,
                    goal = selectedGoal,
                    activityLevel = selectedActivityLevel
                )
                userDao.update(updatedUser)
                Log.d("SETTINGS", "User ${updatedUser.id} updated: ${updatedUser.username}")
                prefs.edit().putString("logged_in_username", updatedUser.username).apply()
                prefs.edit().putString("logged_in_password", updatedUser.password).apply()
                setResult(RESULT_OK)
                finish()
            }
        }
    }

}
