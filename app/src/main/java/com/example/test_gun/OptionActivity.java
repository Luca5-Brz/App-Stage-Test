package com.example.test_gun;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Switch;

public class OptionActivity extends AppCompatActivity {

    int modeLuminosite; //Indique si on est en luminosite auto ou pas
    int RotationAuto; //Indique si on est rotation automatique.

    Switch switch_luminosite;
    Switch switch_rotation;
    SeekBar seekbar_volume;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);


    }
}