package proiect.pdm.bfit.ui.login

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import proiect.pdm.bfit.R
import proiect.pdm.bfit.data.BFitDatabase

@AndroidEntryPoint
class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val etUsername = view.findViewById<EditText>(R.id.etLoginUsername)
        val etPassword = view.findViewById<EditText>(R.id.etLoginPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnRegister = view.findViewById<Button>(R.id.btnGoToRegister)

        val shakeAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                etUsername.startAnimation(shakeAnim)
                etPassword.startAnimation(shakeAnim)
                vibrator.vibrate(300)
                Toast.makeText(context, "Complete all fields", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    val userDao = BFitDatabase.getDatabase(requireContext()).userDao()
                    val user = userDao.getUserByUsername(username)

                    if (user != null && user.password == password) {
                        // Salvăm utilizatorul curent
                        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("logged_in_username", username).apply()

                        Toast.makeText(context, "Welcome, $username!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.nav_home)
                    } else {
                        etUsername.startAnimation(shakeAnim)
                        etPassword.startAnimation(shakeAnim)
                        vibrator.vibrate(300)
                        Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_nav_register)
        }

        return view
    }
}
