package proiect.pdm.bfit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import proiect.pdm.bfit.data.BFitDatabase
import proiect.pdm.bfit.databinding.ActivityMainBinding
import proiect.pdm.bfit.ui.foodlog.FoodLogActivity
import proiect.pdm.bfit.ui.settings.SettingsActivity
import proiect.pdm.bfit.ui.workoutlog.WorkoutLogActivity

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var fab: FloatingActionButton
    var userId: Long = 0L
    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            updateDrawerHeader() // reîncarcă userul din DB și actualizează UI-ul
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* --------------------- Toolbar & Drawer --------------------- */
        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView   = binding.navView
        navController                 = findNavController(R.id.nav_host_fragment_content_main)

        // top-level
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_about_app,
                R.id.nav_gym_map,
            ),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)


        /* FAB vizibil doar pe Dashboard*/
        fab = binding.appBarMain.fab
        fab.setOnClickListener { v ->
            val options = arrayOf("Log Workout", "Log Meal", "Log Water")
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("What would you like to log?")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            val intent = Intent(this, WorkoutLogActivity::class.java).apply {
                                putExtra("USER_ID", userId)
                            }
                            startActivity(intent)
                        }
                        1 -> {
                            val intent = Intent(this, FoodLogActivity::class.java).apply {
                                putExtra("USER_ID", userId)
                            }
                            startActivity(intent)
                        }
                        2 -> {
                            // Show water logging dialog
                            val dialogView = layoutInflater.inflate(R.layout.dialog_water_input, null)
                            val waterInput = dialogView.findViewById<android.widget.EditText>(R.id.waterInput)

                            androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Add Water Intake")
                                .setView(dialogView)
                                .setPositiveButton("Add") { _, _ ->
                                    val amount = waterInput.text.toString().toIntOrNull() ?: 250
                                    lifecycleScope.launch {
                                        val statsDao = BFitDatabase.getDatabase(this@MainActivity).statsDao()
                                        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                                        val currentStats = statsDao.getStatsByDate(today) ?: proiect.pdm.bfit.data.DailyStatsEntity(
                                            date = today,
                                            steps = 0,
                                            caloriesIntake = 0,
                                            caloriesBurned = 0,
                                            waterIntake = 0,
                                            workouts = 0
                                        )
                                        val updatedStats = currentStats.copy(
                                            waterIntake = currentStats.waterIntake + amount
                                        )
                                        statsDao.insert(updatedStats)
                                    }
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        }
                    }
                }
                .show()
        }

        // ascundem / afişăm în funcţie de destinaţia curentă
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.nav_home) fab.show() else fab.hide()
        }

        val headerView        = navView.getHeaderView(0)
        val tvUsername        = headerView.findViewById<TextView>(R.id.tvHeaderUsername)
        val tvWeight          = headerView.findViewById<TextView>(R.id.tvHeaderWeight)

        val prefs    = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val username = prefs.getString("logged_in_username", null)
        navView.setupWithNavController(navController)
        if (username != null) {
            lifecycleScope.launch {
                val userDao = BFitDatabase.getDatabase(this@MainActivity).userDao()
                val user    = userDao.getUserByUsername(username)
                lifecycleScope.launch {
                    userId = user?.id ?: 0L
                    runOnUiThread {

                        navView.setNavigationItemSelectedListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.nav_workout_log -> {
                                    val intent = Intent(this@MainActivity, WorkoutLogActivity::class.java).apply {
                                        putExtra("USER_ID", userId)
                                    }
                                    startActivity(intent)
                                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                                    true
                                }

                                R.id.nav_food_log -> {
                                    val intent = Intent(this@MainActivity, FoodLogActivity::class.java).apply {
                                        putExtra("USER_ID", userId)
                                    }
                                    startActivity(intent)
                                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                                    true
                                }

                                else -> {
                                    // Navigare normală cu NavController pentru fragmente
                                    val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                                    if (handled) binding.drawerLayout.closeDrawer(GravityCompat.START)
                                    handled
                                }
                            }
                        }
                    }
                }
                tvUsername.text = user?.username ?: username
                tvWeight.text = (user?.weightKg ?: 0).toString() + " kg"
            }
//
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java).apply {
                    putExtra("USER_ID", userId)
                }
                startActivity(intent)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()


    private fun updateDrawerHeader() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val username = prefs.getString("logged_in_username", null)
        val profileImageUriString = prefs.getString("profile_image_uri", null)

        if (username != null) {
            lifecycleScope.launch {
                val userDao = BFitDatabase.getDatabase(this@MainActivity).userDao()
                val user = userDao.getUserByUsername(username)
                runOnUiThread {
                    val headerView = binding.navView.getHeaderView(0)
                    val tvUsername = headerView.findViewById<TextView>(R.id.tvHeaderUsername)
                    val tvWeight = headerView.findViewById<TextView>(R.id.tvHeaderWeight)
                    val ivProfile = headerView.findViewById<ImageView>(R.id.imageViewProfilePicHeader) // asigură-te că ai ImageView în header

                    tvUsername.text = user?.username ?: username
                    tvWeight.text = (user?.weightKg ?: 0).toString() + " kg"

                    if (profileImageUriString != null) {
                        Glide.with(this@MainActivity)
                            .load(Uri.parse(profileImageUriString))
                            .into(ivProfile)

                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateDrawerHeader()
    }

}
