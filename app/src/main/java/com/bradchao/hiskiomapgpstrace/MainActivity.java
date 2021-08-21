package com.bradchao.hiskiomapgpstrace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private HashMap<String,Double> myLocation = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            init();
        }else{
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                init();
            }else{
                finish();
            }
        }
    }

    private FusedLocationProviderClient fusedLocationClient;

    @SuppressLint("MissingPermission")
    private void init(){
        myLocation.put("lat", 0.0); myLocation.put("lng", 0.0);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null){
                            Log.v("bradlog", location.getLatitude() + ", " + location.getLongitude());
                            myLocation.put("lat", location.getLatitude());
                            myLocation.put("lng", location.getLongitude());
                            updateMap();
                        }
                    }
                });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                LatLng p0 = new LatLng(myLocation.get("lat"), myLocation.get("lng"));
                Location location = locationResult.getLastLocation();
                myLocation.put("lat", location.getLatitude());
                myLocation.put("lng", location.getLongitude());
                LatLng p1 = new LatLng(myLocation.get("lat"), myLocation.get("lng"));
                mMap.addPolyline(new PolylineOptions().add(p0, p1));
                updateMap();

            }
        };


        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.main_map);
        mapFragment.getMapAsync(this);
    }

    public void gotoMap(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private Marker marker;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng latLng = new LatLng(myLocation.get("lat"), myLocation.get("lng"));
        marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Brad"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                Log.v("bradlog", "touch: " + latLng.latitude + ", " + latLng.longitude);

                LatLng p0 = new LatLng(myLocation.get("lat"), myLocation.get("lng"));
                myLocation.put("lat", latLng.latitude);
                myLocation.put("lng", latLng.longitude);
                LatLng p1 = new LatLng(myLocation.get("lat"), myLocation.get("lng"));
                mMap.addPolyline(new PolylineOptions().add(p0, p1));
                updateMap();
            }
        });
    }

    private void updateMap(){
        LatLng latLng = new LatLng(myLocation.get("lat"), myLocation.get("lng"));
        marker.setPosition(latLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @SuppressLint("MissingPermission")
    private void startLocationUpdates(){
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}