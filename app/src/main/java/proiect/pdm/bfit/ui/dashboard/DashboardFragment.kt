package proiect.pdm.bfit.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val stepsText = view.findViewById<TextView>(R.id.textSteps)
        val intakeText = view.findViewById<TextView>(R.id.textIntake)
        val burnedText = view.findViewById<TextView>(R.id.textBurned)

        // 💾 Inserare temporară în baza de date dacă nu există intrare pentru azi
        val context = requireContext().applicationContext
        val statsDao = BFitDatabase.getDatabase(context).statsDao()

        lifecycleScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val existing = statsDao.getStatsByDate(today)
            if (existing == null) {
                statsDao.insert(
                    DailyStatsEntity(
                        date = today,
                        steps = 7500,
                        caloriesIntake = 2100,
                        caloriesBurned = 400
                    )
                )
            }
        }

        // 👁️ Observator pentru LiveData
        dashboardViewModel.dailyStats.observe(viewLifecycleOwner) { stats ->
            if (stats != null) {
                stepsText.text = "Pași: ${stats.steps}"
                intakeText.text = "Calorii consumate: ${stats.caloriesIntake}"
                burnedText.text = "Calorii arse: ${stats.caloriesBurned}"
            } else {
                stepsText.text = "Fără date pentru azi"
                intakeText.text = ""
                burnedText.text = ""
            }
        }

        return view
    }
}
