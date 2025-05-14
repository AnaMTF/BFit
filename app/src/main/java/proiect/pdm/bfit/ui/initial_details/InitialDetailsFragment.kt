package proiect.pdm.bfit.ui.initial_details

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import proiect.pdm.bfit.R
import proiect.pdm.bfit.data.BFitDatabase
import proiect.pdm.bfit.ui.register.RegisterViewModel

class InitialDetailsFragment : Fragment() {

    private val sharedViewModel: RegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_initial_details, container, false)

        val etHeight = view.findViewById<EditText>(R.id.etHeight)
        val etWeight = view.findViewById<EditText>(R.id.etWeight)
        val etAge = view.findViewById<EditText>(R.id.etAge)
        val spinnerActivity = view.findViewById<Spinner>(R.id.spinnerActivity)
        val spinnerGoal = view.findViewById<Spinner>(R.id.spinnerGoal)
        val btnSave = view.findViewById<Button>(R.id.btnSaveUser)

        // Setări spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.activity_levels,
            android.R.layout.simple_spinner_dropdown_item
        ).also { spinnerActivity.adapter = it }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.goals,
            android.R.layout.simple_spinner_dropdown_item
        ).also { spinnerGoal.adapter = it }

        btnSave.setOnClickListener {
            val height = etHeight.text.toString().toIntOrNull()
            val weight = etWeight.text.toString().toIntOrNull()
            val age = etAge.text.toString().toIntOrNull()
            val activity = spinnerActivity.selectedItem.toString()
            val goal = spinnerGoal.selectedItem.toString()

            if (height == null || weight == null || age == null) {
                Toast.makeText(context, "Please complete all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                Log.d("InitialDetails", "Username from ViewModel: '${sharedViewModel.username}'")
                val userDao = BFitDatabase.getDatabase(requireContext()).userDao()
                val existingUser = userDao.getUserByUsername(sharedViewModel.username)

                val user = userDao.getUserByUsername(sharedViewModel.username)
                Log.d("InitialDetails", "User from DB: $user")

                if (existingUser != null) {
                    val updatedUser = existingUser.copy(
                        heightCm = height,
                        weightKg = weight,
                        age = age,
                        activityLevel = activity,
                        goal = goal
                    )
                    userDao.update(updatedUser)
                    Toast.makeText(context, "Account updated!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.nav_home)
                } else {
                    Toast.makeText(context, "Error: user not found", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }
}
