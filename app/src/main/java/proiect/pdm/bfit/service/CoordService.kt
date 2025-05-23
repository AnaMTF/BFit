package proiect.pdm.bfit.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class CoordService : Service() {
    lateinit var client: FusedLocationProviderClient
    lateinit var request: LocationRequest
    var callback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            //result.location e ce are profu
            super.onLocationResult(result)
            val location = result.lastLocation
            // Handle the location update
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        client = LocationServices.getFusedLocationProviderClient(this)
        request = LocationRequest.Builder(LocationRequest.PRIORITY_HIGH_ACCURACY, 5000).build()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        client.removeLocationUpdates(callback)
        super.onDestroy()
    }
}