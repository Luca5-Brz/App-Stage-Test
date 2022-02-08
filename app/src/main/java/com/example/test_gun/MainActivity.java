package com.example.test_gun;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    int mGestionLuminosite;
    private void setSeekbar() {

        try{
            mGestionLuminosite = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        }
        catch (Exception e) {
            Log.d("tag", e.toString());
        }

        SeekBar seekBar = (SeekBar) findViewById(R.id.main_seekbar_luminosity);
        TextView textViewLuminosity = (TextView) findViewById(R.id.main_textview_luminosity);
        seekBar.setVisibility(View.VISIBLE);
        textViewLuminosity.setVisibility(View.VISIBLE);
        if (mGestionLuminosite == 0) {
            // luminosité auto off

            seekBar.setMax(255);
            float curBrightnessValue = 0;

            //essaye d'accèder à la luminosité actuelle
            try
            {
                curBrightnessValue = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
            }
            catch (Settings.SettingNotFoundException e)
            {
                e.printStackTrace();
            }

            int screen_brightness = (int) curBrightnessValue;
            seekBar.setProgress(screen_brightness);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progress = 0;

                @Override
                public void onProgressChanged(SeekBar seekBar, int progresValue,boolean fromUser)
                {
                    progress = progresValue;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {
                    android.provider.Settings.System.putInt(getContentResolver(),android.provider.Settings.System.SCREEN_BRIGHTNESS,progress);
                }
            });

        }

        if (mGestionLuminosite == 1) {
            //luminosité auto on
            seekBar.setVisibility(View.GONE);
            textViewLuminosity.setVisibility(View.GONE);
        }
    }


}