package com.tal.alpha3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationSource.OnLocationChangedListener, CompoundButton.OnCheckedChangeListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private static final int requestCode = 101;
    LocationManager lm;
    Location myLocation;
    LatLng newLocation;
    ArrayList<LatLng> locationList = new ArrayList<>();
    ArrayList<String> locationTimeList = new ArrayList<>();
    int listID;
    double lat, lng;

    Switch switch1;
    Button btn_get;

    AlertDialog.Builder adb;
    String markerTag;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Location");
    ValueEventListener locListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        switch1 = (Switch) findViewById(R.id.switch1);
        btn_get = (Button) findViewById(R.id.btnGet);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (switch1.isChecked())
                    centerToCurrentLocation();
            }
        });

        locListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                locationList.clear();
                locationTimeList.clear();
                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    lat = (double) locationSnapshot.child("latitude").getValue();
                    lng = (double) locationSnapshot.child("longitude").getValue();
                    newLocation = new LatLng(lat, lng);
                    locationList.add(newLocation);
                    locationTimeList.add(locationSnapshot.getKey());
                    updateMap();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        myRef.addValueEventListener(locListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        centerToCurrentLocation();
    }


    @Override
    public void onLocationChanged(Location location) {
        if (switch1.isChecked()) {
            centerToCurrentLocation();
        }
    }

    public void centerToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                requestLocationPermission();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        requestCode);
            }
        } else {
            mMap.setMyLocationEnabled(true);
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (myLocation == null) {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                String provider = lm.getBestProvider(criteria, true);
                myLocation = lm.getLastKnownLocation(provider);
            }

            if (myLocation != null) {
                LatLng userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14), 1500, null);
            }
        }
    }

    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                requestCode);
    }

    @SuppressLint("MissingPermission")
    public void uploadLocationToFirebase(View view) {
        switch1.setChecked(true);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        LatLng myLatLng = null;
        String myLocationTime = null;

        if (myLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = lm.getBestProvider(criteria, true);
            myLocation = lm.getLastKnownLocation(provider);
        } else {
            myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            myLocationTime = "" + myLocation.getTime();
        }

        try {
            myRef = database.getReference("Location").child(myLocationTime);
            myRef.setValue(myLatLng);
            Toast.makeText(this, "Upload succeeded.", Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            Toast.makeText(this, "Upload failed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void getLocationFromFirebase(View view) {
        updateMap();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        markerTag = marker.getTag().toString();

        adb = new AlertDialog.Builder(this);
        adb.setTitle("Confirm deleting location from Firebase");
        adb.setMessage("Please confirm deleting location #" + (listID - 1) + " from the database:");
        adb.setCancelable(false);
        adb.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    myRef = database.getReference("Location").child(markerTag);
                    myRef.removeValue();

                    Toast.makeText(MapsActivity.this, "Deleting succeeded.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MapsActivity.this, "Deleting failed.", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        adb.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog ad = adb.create();
        ad.show();
        return false;
    }

    public void updateMap() {
        mMap.clear();
        for (listID = 0; listID < locationList.size(); listID++) {
            LatLng latLng = locationList.get(listID);
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("" + listID))
                    .setTag(locationTimeList.get(listID));
        }
        Toast.makeText(this, "Map updated.", Toast.LENGTH_SHORT).show();
    }

    public void clearMap(View view) {
        mMap.clear();
        Toast.makeText(this, "Map cleared.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent t;
        if (id == R.id.menuUpdate) {
            t = new Intent(this, UpdateActivity.class);
            startActivity(t);
        }
        if (id == R.id.menuGallery) {
            t = new Intent(this, GalleryActivity.class);
            startActivity(t);
        }
        if (id == R.id.menuRegister) {
            t = new Intent(this, MainActivity.class);
            startActivity(t);
        }
        return super.onOptionsItemSelected(item);
    }

    public void switchToggle(View view) {
        switch1.toggle();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
    }
}
