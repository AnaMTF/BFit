package proiect.pdm.bfit.ui.about_app

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import proiect.pdm.bfit.R

class AboutAppFragment : Fragment() {

    private var tapCount = 0
    private var isEasterEggActive = false
    private lateinit var logoImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_about_app, container, false)
        logoImage = view.findViewById(R.id.imageView2)

        logoImage.setOnClickListener {
            tapCount++

            if (!isEasterEggActive && tapCount >= 5) {
                logoImage.setImageResource(R.drawable.easteregg)
                Toast.makeText(context, "BFit mode: Couch potato!", Toast.LENGTH_SHORT).show()
                isEasterEggActive = true
                tapCount = 0
            } else if (isEasterEggActive && tapCount >= 1) {
                logoImage.setImageResource(R.drawable.img_logo2)
                isEasterEggActive = false
                tapCount = 0
            }
        }

        return view
    }
}
