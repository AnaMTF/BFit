package proiect.pdm.bfit.ui.register

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import proiect.pdm.bfit.R
import proiect.pdm.bfit.data.BFitDatabase
import proiect.pdm.bfit.data.UserEntity

class RegisterFragment : Fragment() {

    private val viewModel: RegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val usernameEditText = view.findViewById<EditText>(R.id.etUsername)
        val passwordEditText = view.findViewById<EditText>(R.id.etPassword)
        val registerButton = view.findViewById<Button>(R.id.btnRegister)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Please complete all fields", Toast.LENGTH_SHORT).show()
            } else {
                val context = requireContext().applicationContext
                val db = BFitDatabase.getDatabase(context)
                val userDao = db.userDao()

                viewLifecycleOwner.lifecycleScope.launch {
                    val existingUser = userDao.getUserByUsername(username)
                    if (existingUser != null) {
                        Toast.makeText(context, "Username already exists", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.username = username
                        viewModel.password = password

                        val user = UserEntity(
                            username = username,
                            password = password,
                            heightCm = 0,
                            weightKg = 0,
                            age = 0,
                            activityLevel = "",
                            goal = "",
                            pfpLocation = "default"
                        )

                        userDao.insert(user)
                        findNavController().navigate(R.id.action_nav_register_to_nav_initialdetails)
                    }
                }
            }
        }

        return view
    }
}
