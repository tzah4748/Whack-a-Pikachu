package com.example.Whack_a_Pikachu;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

class LocationHelper {

    private Activity callingActivity;
    private int locationRequestCode;
    private LatLng latLng;
    private FusedLocationProviderClient mFusedLocationClient;

    LocationHelper(Activity callingActivity, int locationRequestCode) {
        this.locationRequestCode = locationRequestCode;
        this.callingActivity = callingActivity;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(callingActivity);
    }

    void fetchLocation() {
        if (isPermissionGranted()) {
            asyncGetCurrentLocation();
        } else {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(callingActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(callingActivity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(callingActivity, "Location permission is needed to show the location of top 10 high scores.", Toast.LENGTH_SHORT).show();
            }
            requestPermissions();
        }
    }

    LatLng getCurrentLocation() {
        return latLng;
    }

    private void asyncGetCurrentLocation() {
        mFusedLocationClient.getLastLocation().addOnSuccessListener(callingActivity, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    latLng = new LatLng(location.getLatitude(),location.getLongitude());
                }
            }
        });
    }

    private boolean isPermissionGranted() {
        return ActivityCompat.checkSelfPermission(callingActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(callingActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(callingActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                locationRequestCode);
    }
}