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
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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

    private static final String CHANNEL_ID = "124";
    private GoogleMap mMap;
    private static final String TAG = "Firebase";
    private DatabaseReference mDatabase;
    private GeofencingClient geofencingClient;
    double lat, lon;
    private Button StartGeoBtn, EndGeoBtn;
    private TextInputEditText SearchField, RadiusField;
    private TextInputLayout SearchFieldL, RadiusFieldL;
    private TextView Info;
    private String searchVal, radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//        binding = ActivityMapsBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
        StartGeoBtn = findViewById(R.id.startgeobtn);
        EndGeoBtn = findViewById(R.id.endgeobtn);
        EndGeoBtn.setVisibility(View.GONE);
        Info = findViewById(R.id.infotext);
        SearchField = findViewById(R.id.searchinput);
        SearchFieldL = findViewById(R.id.searchinputframe);
        RadiusField = findViewById(R.id.radiusinput);
        RadiusFieldL = findViewById(R.id.radiusinputframe);
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
                    if (validateInputs(searchVal, radius)) {
                        oneMapSearch();
                    }
            }
        });

        EndGeoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delGeofromLocal();
                updateUIStopped();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mMap = googleMap;
        //Refreshes Map Automatically
        addPostEventListener(mDatabase);
        SharedPreferences sharedPref = getSharedPreferences("GEOFENCE",MODE_PRIVATE);
        Double exLat = Double.valueOf(sharedPref.getString("sLat", "0.0"));
        Double exLon = Double.valueOf(sharedPref.getString("sLon", "0.0"));
        Integer exRadius = sharedPref.getInt("radius", 0);
        if(exRadius != 0) {
            LatLng exGeoPt = new LatLng(exLat, exLon);
            drawCircle(exGeoPt, exRadius);
            CompareCurrentoGeofence(mDatabase, exLat, exLon, exRadius);
            updateUIStarted();
        }
    }

    Marker marker;
    Circle circle;
    //Retrives current coordinates from Firebase
    private void addPostEventListener(DatabaseReference mPostReference) {
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                CurrentData currentData = dataSnapshot.getValue(CurrentData.class);

                if(null != marker) marker.remove();
                lat = currentData.getCurrentLat();
                lon = currentData.getCurrentLon();
                LatLng currentLoc = new LatLng(lat, lon);
                marker = mMap.addMarker(new MarkerOptions().position(currentLoc).title("Current Location")
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
                        Integer rad = Integer.valueOf(radius);
                        drawCircle(newGeoPt, rad);
                        saveGeotoLocal(sLat,sLon, rad);
                        CompareCurrentoGeofence(mDatabase, sLat, sLon, rad);
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

    private void drawCircle(LatLng point, Integer radius) {
        updateUIStarted();
        if(circle != null) circle.remove();
        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions()
                .center(point)
                .radius(radius)
                .strokeColor(R.color.synczepri)
                .fillColor(R.color.synczesec)
                .strokeWidth(5);

        circle = mMap.addCircle(circleOptions);

    }

    private void saveGeotoLocal(Double sLat, Double sLon, Integer radius) {
        SharedPreferences sharedPref = getSharedPreferences("GEOFENCE",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("sLat", String.valueOf(sLat));
        editor.putString("sLon", String.valueOf(sLon));
        editor.putInt("radius", radius);
        editor.apply();
    }

    private void delGeofromLocal(){
        SharedPreferences sharedPref = getSharedPreferences("GEOFENCE",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("sLat", "0.0");
        editor.putString("sLon", "0.0");
        editor.putInt("radius", 0);
        editor.apply();
    }

    //Retrives current coordinates from Firebase
    private void CompareCurrentoGeofence(DatabaseReference mPostReference, Double sLat, Double sLon, Integer radius) {
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            boolean inside = true;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                CurrentData currentData = dataSnapshot.getValue(CurrentData.class);
                Double pLat = currentData.getCurrentLat();
                Double pLon = currentData.getCurrentLon();

                    if (compareDist(sLat, sLon, pLat, pLon, radius) && inside == false) {
                        Intent insideCircle = new Intent(MapsActivity.this, MapsActivity.class);
                        createNotificationChannel();
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MapsActivity.this);
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(MapsActivity.this);
                        stackBuilder.addNextIntentWithParentStack(insideCircle);
                        // Get the PendingIntent containing the entire back stack
                        PendingIntent resultPendingIntent =
                                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                        Integer notificationId = 123;
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MapsActivity.this, CHANNEL_ID)
                                .setSmallIcon(R.drawable.past_person_marker)
                                .setContentTitle("Geofence Alert")
                                .setContentText("Wearer has entered the geofenced area.")
                                .setContentIntent(resultPendingIntent)
                                .setPriority(NotificationCompat.PRIORITY_MAX);
                        notificationManager.notify(notificationId, builder.build());
                        inside = true;
                    } else if(!compareDist(sLat, sLon, pLat, pLon, radius) && inside == true) {
                        Intent outsideCircle = new Intent(MapsActivity.this, MapsActivity.class);
                        createNotificationChannel();
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MapsActivity.this);
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(MapsActivity.this);
                        stackBuilder.addNextIntentWithParentStack(outsideCircle);
                        // Get the PendingIntent containing the entire back stack
                        PendingIntent resultPendingIntent =
                                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                        Integer notificationId = 123;
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MapsActivity.this, CHANNEL_ID)
                                .setSmallIcon(R.drawable.past_person_marker)
                                .setContentTitle("Geofence Alert")
                                .setContentText("Wearer has exited the geofenced area.")
                                .setContentIntent(resultPendingIntent)
                                .setPriority(NotificationCompat.PRIORITY_MAX);
                        notificationManager.notify(notificationId, builder.build());
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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Fall Detection Channel";
            String description = "Sends a notification if fall is detected";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void updateUIStarted(){
        Info.setText(R.string.infostopping);
        SearchField.setVisibility(View.GONE);
        SearchFieldL.setVisibility(View.GONE);
        RadiusFieldL.setVisibility(View.GONE);
        RadiusField.setVisibility(View.GONE);
        StartGeoBtn.setVisibility(View.GONE);
        EndGeoBtn.setVisibility(View.VISIBLE);
    }

    private void updateUIStopped(){
        Info.setText(R.string.infostarting);
        SearchField.setVisibility(View.VISIBLE);
        RadiusField.setVisibility(View.VISIBLE);
        SearchFieldL.setVisibility(View.VISIBLE);
        RadiusFieldL.setVisibility(View.VISIBLE);
        StartGeoBtn.setVisibility(View.VISIBLE);
        EndGeoBtn.setVisibility(View.GONE);
        circle.remove();
    }

}