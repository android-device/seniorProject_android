package com.mooo.samcat.temperaturemonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class addSensor extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sensor);
        setTitle(getString(R.string.add_sensor));
    }
}
