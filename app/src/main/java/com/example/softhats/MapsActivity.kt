package com.example.softhats

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.softhats.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnSoftHats.setOnClickListener {
            val softHats = LatLng(19.2632604, -98.9035804) // Coordenadas de SoftHats
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(softHats, 17f))
            Toast.makeText(this, "Mostrando ubicación de SoftHats", Toast.LENGTH_SHORT).show()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnUbicacionActual.setOnClickListener {
            mostrarUbicacionActual()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val chalco = LatLng(19.2632604, -98.9035804)
        val marker = MarkerOptions()
            .position(chalco)
            .title("Sucursal Principal HatsGo")
            .snippet("Chalco de Díaz Covarrubias, Méx.")

        mMap.addMarker(marker)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chalco, 17f))
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

    private fun mostrarUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val miUbicacion = LatLng(location.latitude, location.longitude)
                mMap.addMarker(
                    MarkerOptions()
                        .position(miUbicacion)
                        .title("Tu ubicación actual")
                )
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 17f))
                Toast.makeText(this, "Ubicación actual encontrada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
