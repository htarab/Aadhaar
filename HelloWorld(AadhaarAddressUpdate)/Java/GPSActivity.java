package com.example.aadhaar.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aadhaar.Address;
import com.example.aadhaar.R;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.jetbrains.annotations.NotNull;

public class GPSActivity extends AppCompatActivity {
    Button btn_current_location;
    ProgressBar progress_bar;
    TextView txt_long, txt_lat;

    public static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpsactivity);

        btn_current_location = findViewById(R.id.btn_current_location);
        progress_bar = findViewById(R.id.progress_bar);
        txt_long = findViewById(R.id.txt_long);
        txt_lat = findViewById(R.id.txt_lat);

        btn_current_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(GPSActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                } else {
                    getCurrentLocation();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Give Permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() 
    {
        progress_bar.setVisibility(View.VISIBLE);

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(GPSActivity.this).requestLocationUpdates(locationRequest, new LocationCallback()
        {
            @Override
            public void onLocationResult(@NonNull @NotNull LocationResult locationResult)
            {
                super.onLocationResult(locationResult);

                LocationServices.getFusedLocationProviderClient(GPSActivity.this).removeLocationUpdates(this);

                if(locationResult != null && locationResult.getLocations().size() > 0)
                {
                    int latestLocationIndex = locationResult.getLocations().size() - 1;
                    double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                    double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                    txt_lat.setText(String.valueOf(latitude));
                    txt_long.setText(String.valueOf(longitude));
                    progress_bar.setVisibility(View.GONE);
                    StringBuffer result = Address.getAddress(latitude, longitude);
                    Log.d("TAG", "Address " + result);
                }
            }
        }, Looper.getMainLooper());
    }
}