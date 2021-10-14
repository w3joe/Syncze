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
    private Integer firstTime = 0;

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
                //add all points
                for(i = 0; i < Coordinates.size(); i++) {

                    Double lat = Coordinates.get(i).get(0);
                    Double lon = Coordinates.get(i).get(1);
                    Double date = Coordinates.get(i).get(2);
                    Double time = Coordinates.get(i).get(3);

                    //set camera based on first coordinate and do it only once
                    if(firstTime == 0){
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
                            .snippet(formatTime(date, time))
                            .icon(smallMarkerIcon)
                                    );
                }
                mMap.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .color(R.color.synczepri)
                        .addAll(latlngs));
                firstTime++;
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

    private String formatTime(Double date, Double time)
    {
        String sdate = date.toString();
        char[] cdate = new char[sdate.length()];
        for (int i = 0; i < sdate.length(); i++) {
            cdate[i] = sdate.charAt(i);
        }
        String fdate = String.valueOf(cdate[7]) + String.valueOf(cdate[8]) + "/" + String.valueOf(cdate[5]) + String.valueOf(cdate[6]) + "/"
                + String.valueOf(cdate[0]) + String.valueOf(cdate[2]) + String.valueOf(cdate[3]) + String.valueOf(cdate[4]);

        String stime = time.toString();
        char[] ctime = new char[stime.length()];
        for (int i = 0; i < stime.length(); i++) {
            ctime[i] = stime.charAt(i);
        }
        String ftime = String.valueOf(ctime[0]) + String.valueOf(ctime[1]) + ":" + String.valueOf(ctime[2]) + String.valueOf(ctime[3])
                + ":" + String.valueOf(ctime[4]) + String.valueOf(ctime[5]);

        String formatted = "Timestamp: " + fdate + " at " + ftime;
        return formatted;
    }
}