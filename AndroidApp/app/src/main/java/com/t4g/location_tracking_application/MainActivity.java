package com.t4g.location_tracking_application;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Firebase";
    private DatabaseReference mDatabase;
    private TextView test;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        test = findViewById(R.id.test);
        new Runnable() {
            @Override
            public void run() {
                addPostEventListener(mDatabase);
            }
        };


    }

    //Retrives current coordinates and fall detection status from Firebaes
    private void addPostEventListener(DatabaseReference mPostReference) {
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                CurrentData currentData = dataSnapshot.getValue(CurrentData.class);
                Double lat = currentData.getCurrentLat();
                Double lon = currentData.getCurrentLon();

                test.setText(lat.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                Toast.makeText(MainActivity.this, "Error retrieving location. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        };
        mPostReference.addValueEventListener(postListener);
    }
}