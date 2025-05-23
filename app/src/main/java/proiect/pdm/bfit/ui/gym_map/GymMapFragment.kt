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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import dagger.hilt.android.AndroidEntryPoint
import proiect.pdm.bfit.R

@AndroidEntryPoint
class GymMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var gMap: GoogleMap
    private lateinit var progress: View
    private lateinit var placesClient: PlacesClient
    private val locationPermReq = 1001

    /* ---------- View / Map initialisation ---------- */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_gym_map, container, false)

        mapView  = v.findViewById(R.id.mapView)
        progress = v.findViewById(R.id.progress)

        // Make sure the Places SDK is ready exactly once in the whole process
        if (!Places.isInitialized()) {
            Places.initialize(
                requireContext(),
                getString(R.string.google_maps_key),           // same key you use for the map
//                Locale.getDefault()
            )
        }
        placesClient = Places.createClient(requireContext())

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return v
    }

    override fun onMapReady(map: GoogleMap) {
        gMap = map.apply { uiSettings.isZoomControlsEnabled = true }
        enableLocationAndLoadGyms()
    }

    /* ---------- Permission handling ---------- */
    private fun enableLocationAndLoadGyms() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            gMap.isMyLocationEnabled = true
            fetchCurrentLocation()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermReq
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermReq &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            enableLocationAndLoadGyms()
        } else {
            Toast.makeText(
                context,
                "Permisiunea locaţiei e necesară",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /* ---------- Fetch device location ---------- */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION])
    private fun fetchCurrentLocation() {
        val fused = LocationServices.getFusedLocationProviderClient(requireActivity())
        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                val here = LatLng(loc.latitude, loc.longitude)
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(here, 12f))
                loadNearbyGyms(here)
            } else {
                progress.visibility = View.GONE
            }
        }
    }

    /* ---------- Places SDK: “Nearby Search (New)” ---------- */
    private fun loadNearbyGyms(center: LatLng) {
        progress.visibility = View.VISIBLE

        // 1. Restrict the search to a 3 km circle around the user
        val restriction = CircularBounds.newInstance(center, 3_000.0)  // metres :contentReference[oaicite:0]{index=0}

        // 2. Decide which fields you really need
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG
        )

        // 3. Build the request
        val request = SearchNearbyRequest.builder(restriction, fields)
            .setIncludedPrimaryTypes(listOf(PlaceTypes.GYM))             // only gyms
            .setMaxResultCount(20)                                        // up to 20 results
            .build()                                                      // :contentReference[oaicite:1]{index=1}

        // 4. Fire the request
        placesClient.searchNearby(request)
            .addOnSuccessListener { response ->
                response.places.forEach { place ->
                    place.latLng?.let {
                        gMap.addMarker(
                            MarkerOptions()
                                .position(it)
                                .title(place.name)
                        )
                    }
                }
                progress.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Eroare la încărcarea sălilor: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
                progress.visibility = View.GONE
            }
    }

    /* ---------- MapView lifecycle ---------- */
    override fun onResume()          { super.onResume();  mapView.onResume() }
    override fun onPause()           { super.onPause();   mapView.onPause() }
    override fun onDestroy()         { super.onDestroy(); mapView.onDestroy() }
    override fun onLowMemory()       { super.onLowMemory(); mapView.onLowMemory() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
