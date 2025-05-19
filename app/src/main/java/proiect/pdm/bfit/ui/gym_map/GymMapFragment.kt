package proiect.pdm.bfit.ui.gym_map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import proiect.pdm.bfit.R


class GymMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var gMap: GoogleMap
    private lateinit var progress: View
    private val locationPermReq = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_gym_map, container, false)

        mapView  = v.findViewById(R.id.mapView)
        progress = v.findViewById(R.id.progress)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return v
    }

    /* ---------- ON MAP READY ---------- */
    override fun onMapReady(map: GoogleMap) {
        gMap = map
        gMap.uiSettings.isZoomControlsEnabled = true
        enableLocationAndLoadGyms()
    }

    /* ---------- LOCATION PERMISSION ---------- */
    private fun enableLocationAndLoadGyms() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            gMap.isMyLocationEnabled = true
            fetchCurrentLocation()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermReq
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermReq &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) enableLocationAndLoadGyms()
        else Toast.makeText(context,"Permisiunea locaţiei e necesară",Toast.LENGTH_LONG).show()
    }

    /* ---------- CURRENT LOCATION & GYMS ---------- */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun fetchCurrentLocation() {
        val fused = LocationServices.getFusedLocationProviderClient(requireActivity())
        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                val here = LatLng(loc.latitude, loc.longitude)
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(here, 12f))

                loadNearbyGyms(here)      // în practică → Places API
            } else {
                progress.visibility = View.GONE
            }
        }
    }

    private fun loadNearbyGyms(center: LatLng) {
        // TODO: apelează Nearby Search sau backend-ul tău
        dummyMarkersAround(center)
        progress.visibility = View.GONE      // ascundem spinner-ul
    }

    private fun dummyMarkersAround(c: LatLng) {
        listOf(
            LatLng(c.latitude + .05, c.longitude + .03),
            LatLng(c.latitude - .04, c.longitude - .02),
            LatLng(c.latitude + .01, c.longitude - .05)
        ).forEachIndexed { i, p ->
            gMap.addMarker(MarkerOptions().position(p).title("Gym ${i + 1}"))
        }
    }

    /* ---------- MapView LIFECYCLE ---------- */
    override fun onResume()          { super.onResume();  mapView.onResume()  }
    override fun onPause()           { super.onPause();   mapView.onPause()   }
    override fun onDestroy()         { super.onDestroy(); mapView.onDestroy() }
    override fun onLowMemory()       { super.onLowMemory(); mapView.onLowMemory() }
    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out); mapView.onSaveInstanceState(out)
    }
}
