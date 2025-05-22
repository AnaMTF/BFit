package proiect.pdm.bfit

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import proiect.pdm.bfit.data.BFitDatabase
import proiect.pdm.bfit.databinding.ActivityMainBinding
import proiect.pdm.bfit.ui.foodlog.FoodLogActivity
import proiect.pdm.bfit.ui.settings.SettingsActivity
import proiect.pdm.bfit.ui.workoutlog.WorkoutLogActivity

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var fab: FloatingActionButton
    private var userId: Long = 0L
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
                R.id.nav_gallery,
                R.id.nav_slideshow,
                R.id.nav_about_app,
                R.id.nav_gym_map,
                R.id.nav_add_daily_stats
            ),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        /* FAB vizibil doar pe Dashboard*/
        fab = binding.appBarMain.fab
        fab.setOnClickListener { v ->
            Snackbar
                .make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab)
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

        if (username != null) {
            lifecycleScope.launch {
                val userDao = BFitDatabase.getDatabase(this@MainActivity).userDao()
                val user    = userDao.getUserByUsername(username)
                lifecycleScope.launch {
                    userId = user?.id ?: 0L
                    runOnUiThread {
                        binding.navView.setNavigationItemSelectedListener { menuItem ->
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

                                else -> false
                            }
                        }
                    }
                }
                tvUsername.text = user?.username ?: username
                tvWeight.text   = (user?.weightKg ?: 0).toString() + " kg"
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
        if (username != null) {
            lifecycleScope.launch {
                val userDao = BFitDatabase.getDatabase(this@MainActivity).userDao()
                val user = userDao.getUserByUsername(username)
                runOnUiThread {
                    val headerView = binding.navView.getHeaderView(0)
                    val tvUsername = headerView.findViewById<TextView>(R.id.tvHeaderUsername)
                    val tvWeight = headerView.findViewById<TextView>(R.id.tvHeaderWeight)
                    tvUsername.text = user?.username ?: username
                    tvWeight.text = (user?.weightKg ?: 0).toString() + " kg"
                }
            }
        }
    }
}
