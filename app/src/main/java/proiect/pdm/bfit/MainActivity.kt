package proiect.pdm.bfit

import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var fab: FloatingActionButton

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

                tvUsername.text = user?.username ?: username
                tvWeight.text   = (user?.weightKg ?: 0).toString() + " kg"
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
}
