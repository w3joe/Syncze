package com.t4g.location_tracking_application;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.t4g.location_tracking_application.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private static final String TAG = "Firebase";
    private DatabaseReference mDatabase;
    private TextView test;
    double lat, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//        binding = ActivityMapsBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mMap = googleMap;
        //Refreshes Map Automatically
        addPostEventListener(mDatabase);


    }

    //Retrives current coordinates and fall detection status from Firebase
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
                mMap.addMarker(new MarkerOptions().position(currentLoc).title("Current Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 13));

                boolean fallStatus;
                fallStatus = currentData.isFallStatus();
                if(fallStatus)
                {
                    //notification
                }
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
}