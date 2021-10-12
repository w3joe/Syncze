package com.t4g.location_tracking_application;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.t4g.location_tracking_application.databinding.ActivityMapsBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "Firebase";
    private DatabaseReference mDatabase;
    private GeofencingClient geofencingClient;
    double lat, lon;
    private Button StartGeoBtn, EndGeoBtn;
    private TextInputEditText SearchField, RadiusField;
    private String searchVal, radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//        binding = ActivityMapsBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
        StartGeoBtn = findViewById(R.id.startgeobtn);
        SearchField = findViewById(R.id.searchinput);
        RadiusField = findViewById(R.id.radiusinput);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geofencingClient = LocationServices.getGeofencingClient(this);
        StartGeoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchVal = SearchField.getText().toString();
                radius = RadiusField.getText().toString();
                if (validateInputs(searchVal, radius))
                    oneMapSearch();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mMap = googleMap;
        //Refreshes Map Automatically
        addPostEventListener(mDatabase);

    }

    //Retrives current coordinates from Firebase
    private void addPostEventListener(DatabaseReference mPostReference) {
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                CurrentData currentData = dataSnapshot.getValue(CurrentData.class);
                mMap.clear();
                lat = currentData.getCurrentLat();
                lon = currentData.getCurrentLon();
                LatLng currentLoc = new LatLng(lat, lon);
                mMap.addMarker(new MarkerOptions().position(currentLoc).title("Current Location")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.person_marker))
                        .snippet("Updated: Every 5 minutes")
                );
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 13));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                Toast.makeText(MapsActivity.this, "Error retrieving location. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        };
        mPostReference.addValueEventListener(postListener);
    }

    private void oneMapSearch() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://developers.onemap.sg/commonapi/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        OneMapAPICall apiCall = retrofit.create(OneMapAPICall.class);
        Call<JSONResponse> call = apiCall.getData(searchVal, "Y", "N");
        call.enqueue(new Callback<JSONResponse>() {

            private ArrayList<Results> ResultList;

            @Override
            public void onResponse(Call<JSONResponse> call, Response<JSONResponse> response) {
                if (response.code() != 200) {
                    Log.d("", "Check Connection");
                    return;
                } else {
                    JSONResponse jsonResponse = response.body();
                    if (jsonResponse.getFound() == 0) {
                        Toast.makeText(MapsActivity.this, "No search results found.", Toast.LENGTH_SHORT).show();
                    } else {
                        ResultList = new ArrayList<>(Arrays.asList(jsonResponse.getResults()));
                        Double sLat = ResultList.get(0).getLATITUDE();
                        Double sLon = ResultList.get(0).getLONGITUDE();
                        LatLng newGeoPt = new LatLng(sLat, sLon);
                        drawCircle(newGeoPt);
                        CompareCurrentoGeofence(mDatabase, sLat, sLon);
                    }
                }
            }

            @Override
            public void onFailure(Call<JSONResponse> call, Throwable t) {
                Toast.makeText(MapsActivity.this, "Service unavailable. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs(String searchVal, String radius) {
        if (TextUtils.isEmpty(searchVal)) {
            SearchField.setError("Field cannot be empty!");
            return false;
        } else if (TextUtils.isEmpty(radius)) {
            RadiusField.setError("Field cannot be empty");
            return false;
        } else if (Integer.valueOf(radius) < 20) {
            RadiusField.setError("Minimum radius is 20m");
            return false;
        } else {
            return true;
        }
    }

    private void drawCircle(LatLng point) {
        Circle drawnCircle = null;

        Integer rad = Integer.valueOf(radius);
        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions()
                .center(point)
                .radius(rad)
                .strokeColor(R.color.synczepri)
                .fillColor(R.color.synczesec)
                .strokeWidth(5);

        drawnCircle = mMap.addCircle(circleOptions);

    }

    //Retrives current coordinates from Firebase
    private void CompareCurrentoGeofence(DatabaseReference mPostReference, Double sLat, Double sLon) {
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            boolean inside = true;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                CurrentData currentData = dataSnapshot.getValue(CurrentData.class);
                Double pLat = currentData.getCurrentLat();
                Double pLon = currentData.getCurrentLon();

                    if (compareDist(sLat, sLon, pLat, pLon, Integer.valueOf(radius)) && inside == false) {
                        Toast.makeText(MapsActivity.this, "In circle", Toast.LENGTH_SHORT).show();
                        inside = true;
                    } else if(!compareDist(sLat, sLon, pLat, pLon, Integer.valueOf(radius)) && inside == true) {
                        Toast.makeText(MapsActivity.this, "Outside circle", Toast.LENGTH_SHORT).show();
                        inside = false;
                    } else {}

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                Toast.makeText(MapsActivity.this, "Error retrieving location. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        };
        mPostReference.addValueEventListener(postListener);
    }

    private boolean compareDist(Double sLat, Double sLon, Double pLat, Double pLon, Integer radius)
    {
        Double distance;
        if ((sLat == pLat) && (sLon == pLon)) {
            distance = 0.0;
        }
        else {
            double theta = sLon - pLon;
            double dist = Math.sin(Math.toRadians(sLat)) * Math.sin(Math.toRadians(pLat)) + Math.cos(Math.toRadians(sLat)) * Math.cos(Math.toRadians(pLat)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            distance = dist * 60 * 1.1515;
        }
        Double radiuskm = radius / 1000.0;
        if(distance <= radiuskm){
            return true;
        } else {
            return false;
        }
    }



}