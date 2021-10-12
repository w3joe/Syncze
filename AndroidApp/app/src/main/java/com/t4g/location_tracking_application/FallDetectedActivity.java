package com.t4g.location_tracking_application;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FallDetectedActivity extends AppCompatActivity {
    private Button ViewLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_detected);
        ViewLocation = findViewById(R.id.viewlocbtn);
        ViewLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bg = new Intent(FallDetectedActivity.this, MainActivity.class);
                Intent i = new Intent(FallDetectedActivity.this, MapsActivity.class);
                startActivity(bg);
                startActivity(i);
                finish();
            }
        });
    }
}