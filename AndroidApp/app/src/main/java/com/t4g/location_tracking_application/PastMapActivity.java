package com.t4g.location_tracking_application;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PastMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "Firebase";
    private DatabaseReference mDatabase;
    double lat, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_map);
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

    //Retrieves current coordinates from Firebase
    private void addPostEventListener(DatabaseReference mPostReference) {
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                CurrentData currentData = dataSnapshot.getValue(CurrentData.class);
                mMap.clear();
                ArrayList<ArrayList<Double>> Coordinates = currentData.getPastLocation();
                // Add polylines to the map.
                // Polylines are useful to show a route or some other connection between points.
                List<LatLng> latlngs = new ArrayList<>();
                int i;
                for(i = 0; i < Coordinates.size(); i++) {

                    Double lat = Coordinates.get(i).get(0);
                    Double lon = Coordinates.get(i).get(1);

                    //set camera based on first coordinate
                    if(i == 0){
                        LatLng firstPoint = new LatLng(lat,lon);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPoint, 12));
                    }
                    LatLng point = new LatLng(lat,lon);
                    latlngs.add(point);
                    int height = 60;
                    int width = 40;
                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.past_person_marker);
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                    BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                    mMap.addMarker(new MarkerOptions().position(point).title("Coordinates: " + lat + ", " + lon)
                            .icon(smallMarkerIcon)
                                    );
                }
                mMap.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .color(R.color.synczepri)
                        .addAll(latlngs));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                Toast.makeText(PastMapActivity.this, "Error retrieving location. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        };
        mPostReference.addValueEventListener(postListener);
    }


}