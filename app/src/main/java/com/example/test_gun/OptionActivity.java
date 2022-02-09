package com.example.test_gun;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.*;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.*;

public class OptionActivity extends AppCompatActivity {


    private SwitchCompat mSwitchLuminosite;
    private SwitchCompat mSwitchRotation;
    private SeekBar mSeekbarVolume;
    private Button mButton4g;
    private Button mButtonWifi;
    private Button mButtonClavier;

    private int mLuminAuto; //Indique le mode automatique de la luminosité
    private int mRotationAuto; //Indique le mode automatique de la rotation

    AudioManager audioManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        mSwitchLuminosite = findViewById(R.id.option_switch_lumAuto);
        mSwitchRotation = findViewById(R.id.option_switch_rotAuto);
        mSeekbarVolume = findViewById(R.id.option_seekbar_volume);
        mButton4g = findViewById(R.id.option_button_4g);
        mButtonWifi = findViewById(R.id.option_button_wifi);
        mButtonClavier = findViewById(R.id.option_button_clavier);

    //Récuperation de la luminosité et de la rotation actives
        mLuminAuto = getLuminAuto();
        mRotationAuto = getRotationAuto();

        if(mLuminAuto==1){
            mSwitchLuminosite.setChecked(true);
        }else{
            mSwitchLuminosite.setChecked(false);
        }

        if(mRotationAuto==1){
            mSwitchRotation.setChecked(true);
        }else{
            mSwitchRotation.setChecked(false);
        }



        setOnClick();
        setSeekBarVolume();

    }

    public void setOnClick(){
        
        mSwitchLuminosite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                // Si le bouton est coché, on active la luminosité auto
                //setLuminAuto();
                mLuminAuto = getLuminAuto();
                if(mLuminAuto==0){

                    android.provider.Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                    Toast.makeText(OptionActivity.this, "Luminosité Automatique", Toast.LENGTH_SHORT).show();

                }//Si pas, on la désactive
                else {
                    android.provider.Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Toast.makeText(OptionActivity.this, "Luminosité Manuelle", Toast.LENGTH_SHORT).show();

                }

            }
        });

        mSwitchRotation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                //Si le bouton est coché, la rotation automatique est activée.
                mRotationAuto = getRotationAuto();

                if(mRotationAuto==0){
                    Toast.makeText(OptionActivity.this, "Rotation Automatique", Toast.LENGTH_SHORT).show();

                    Settings.System.putInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,1);
                }else {
                    Toast.makeText(OptionActivity.this, "Rotation Bloquée", Toast.LENGTH_SHORT).show();
                    Settings.System.putInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,0);
                }

            }
        });

        mButton4g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                check4GNetwork();
            }
        });

        mButtonWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkWifiNetwork();
            }
        });

        mButtonClavier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(OptionActivity.this, "Choix Clavier", Toast.LENGTH_SHORT).show();
            }
        });




        
    }

    public void setSeekBarVolume(){

        mSeekbarVolume.setMax(7);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        try {
            mSeekbarVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
        } catch (Exception e) {
            e.printStackTrace();
        }


        mSeekbarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i*2, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, i, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, i*2, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_RING,i*2,0);
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,i*2,0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void check4GNetwork(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.Q) {
            wifiManager.setWifiEnabled(false);
            Toast.makeText(this, "Wifi Coupé", Toast.LENGTH_SHORT).show();
        }else
        {
            Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
            startActivity(panelIntent);
        }


    }

    public void checkWifiNetwork(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.Q){
            wifiManager.setWifiEnabled(true);
            Toast.makeText(this, "Wifi Activé", Toast.LENGTH_SHORT).show();
        }else
        {
            Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
            startActivity(panelIntent);
        }

    }

    //************************* Getter and setters

    // On recupére le mode de luminosité du système et le stocke dans une variable
    public int getLuminAuto() {

        try {
            mLuminAuto = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        }
        catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Impossible de récupérer le mode de luminosité", Toast.LENGTH_SHORT).show();
        }

        return mLuminAuto;
    }

    //On récupère le mode de rotation du système et le stocke dans une variable.
    public int getRotationAuto() {

        try {
            mRotationAuto = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
        } 
        catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Impossible de récupérer le mode de rotation", Toast.LENGTH_SHORT).show();
        }

        return mRotationAuto;
    }
}