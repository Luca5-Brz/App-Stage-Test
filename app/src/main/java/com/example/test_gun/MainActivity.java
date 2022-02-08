package com.example.test_gun;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.*;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    int mModeLuminosite; //Si le telephone est en luminosite auto ou pas
    private final List mBlockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP)); // liste qui contient les boutons à bloquer

    boolean currentFocus;
    boolean isPaused;
    Handler collapseNotificationHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setSeekbar();


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

            hideStatusBar();
    }


    //Cache la barre de Status
    private void hideStatusBar(){
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);


    }

    // Paramètrage de la barre de luminosité
    private void setSeekbar() {

        try{
            mModeLuminosite = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        }
        catch (Exception e) {
            Log.d("tag", e.toString());
        }

        SeekBar seekBar = (SeekBar) findViewById(R.id.main_seekbar_luminosity);
        TextView textViewLuminosity = (TextView) findViewById(R.id.main_textview_luminosity);
        seekBar.setVisibility(View.VISIBLE);
        textViewLuminosity.setVisibility(View.VISIBLE);
        if (mModeLuminosite == 0) {
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

        if (mModeLuminosite == 1) {
            //luminosité auto on
            seekBar.setVisibility(View.GONE);
            textViewLuminosity.setVisibility(View.GONE);
        }
    }

    //Blocage des boutons de volume
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mBlockedKeys.contains(event.getKeyCode())){
            return  true;
        }else{
            return super.dispatchKeyEvent(event);
        }

    }
}