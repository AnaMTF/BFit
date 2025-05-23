package proiect.pdm.bfit.ui.about_app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import proiect.pdm.bfit.R

@AndroidEntryPoint
class AboutAppFragment : Fragment(), SensorEventListener {

    private lateinit var logoImage: ImageView
    private var tapCount = 0
    private var isEasterEggActive = false

    private lateinit var sensorManager: SensorManager
    private var accel = 0f
    private var accelCurrent = 0f
    private var accelLast = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_about_app, container, false)
        logoImage = view.findViewById(R.id.imageView2)

        // BOUNCE
        val bounceAnim = AnimationUtils.loadAnimation(context, R.anim.bounce)

        logoImage.setOnClickListener {
            it.startAnimation(bounceAnim)
            tapCount++
            if (!isEasterEggActive && tapCount >= 5) {
                logoImage.setImageResource(R.drawable.easteregg)
                Toast.makeText(context, "BFit mode: Couch potato!", Toast.LENGTH_SHORT).show()
                isEasterEggActive = true
                tapCount = 0
            } else if (isEasterEggActive && tapCount >= 1) {
                logoImage.setImageResource(R.drawable.img_logo2)
                Toast.makeText(context, "Back to fitness mode!", Toast.LENGTH_SHORT).show()
                isEasterEggActive = false
                tapCount = 0
            }
        }

        // Shake detection
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )

        accel = 10f
        accelCurrent = SensorManager.GRAVITY_EARTH
        accelLast = SensorManager.GRAVITY_EARTH

        return view
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            accelLast = accelCurrent
            accelCurrent = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta = accelCurrent - accelLast
            accel = accel * 0.9f + delta

            if (accel > 12) {
                Toast.makeText(context, "Shake detected! Stay hydrated 🥤", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }
}
