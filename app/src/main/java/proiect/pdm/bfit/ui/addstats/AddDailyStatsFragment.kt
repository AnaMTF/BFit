package proiect.pdm.bfit.ui.addstats

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import proiect.pdm.bfit.R
import proiect.pdm.bfit.data.BFitDatabase
import proiect.pdm.bfit.data.DailyStatsEntity
import java.text.SimpleDateFormat
import java.util.*

class AddDailyStatsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_daily_stats, container, false)

        val stepsEdit = view.findViewById<EditText>(R.id.editSteps)
        val intakeEdit = view.findViewById<EditText>(R.id.editCaloriesIn)
        val burnedEdit = view.findViewById<EditText>(R.id.editCaloriesOut)
        val saveButton = view.findViewById<Button>(R.id.btnSave)

        saveButton.setOnClickListener {
            val steps = stepsEdit.text.toString().toIntOrNull() ?: 0
            val intake = intakeEdit.text.toString().toIntOrNull() ?: 0
            val burned = burnedEdit.text.toString().toIntOrNull() ?: 0
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val dao = BFitDatabase.getDatabase(requireContext()).statsDao()
            lifecycleScope.launch {
                dao.insert(
                    DailyStatsEntity(
                        date = today,
                        steps = steps,
                        caloriesIntake = intake,
                        caloriesBurned = burned
                    )
                )
                Toast.makeText(context, "Date salvate!", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
