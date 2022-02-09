package com.example.test_gun;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

    private Button mButtonOptions;
    private Button mButtonStopCDH;
    private Button mButtonParams;
    private Button mButtonMAJ;
    public SeekBar mSeekbarLumin;
    public TextView mTextViewLumin;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonOptions = findViewById(R.id.main_button_options);
        mButtonStopCDH = findViewById(R.id.main_button_StopCDH);
        mButtonParams = findViewById(R.id.main_button_parametres);
        mButtonMAJ = findViewById(R.id.main_button_MiseAJour);
        mSeekbarLumin = findViewById(R.id.main_seekbar_luminosity);
        mTextViewLumin = findViewById(R.id.main_textview_luminosity);


        setSeekbarLumin();


        setOnClick();

    }

    @Override
    protected void onResume() {
        super.onResume();

        setSeekbarLumin();

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
    private void setSeekbarLumin() {

        //On va chercher le mode de luminosité système actuel
        try{
            mModeLuminosite = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        }
        catch (Exception e) {
            Log.d("tag", e.toString());
        }

        if (mModeLuminosite==0) {
            // luminosité auto off

            mSeekbarLumin.setVisibility(View.VISIBLE);
            mTextViewLumin.setVisibility(View.VISIBLE);

            //essaye d'accèder à la luminosité actuelle et adapte la barre en fonction
            try
            {
               mSeekbarLumin.setProgress(Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS));
            }
            catch (Settings.SettingNotFoundException e)
            {
                e.printStackTrace();
                Toast.makeText(this, "Impossible d'accéder au param lum", Toast.LENGTH_SHORT).show();
            }

            //On suis les changements de la barre et on adapte le niveau de luminosité en fonction
            mSeekbarLumin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progress = 0;

                @Override
                public void onProgressChanged(SeekBar seekBar, int progressValue,boolean fromUser)
                {
                    progress = progressValue;
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, progressValue);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {
                    //modif de la lumin. système en fonction du niveau mis sur la barre par le User
                    //Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, progress);
                }
            });

        }
        if (mModeLuminosite==1){
            //luminosité auto on
            mSeekbarLumin.setVisibility(View.GONE);
            mTextViewLumin.setVisibility(View.GONE);
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

    //Paramètre l'action à effectuer lors d'un appui sur un bouton.
    public void setOnClick() {
        mButtonOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent optionActivity = new Intent(MainActivity.this, OptionActivity.class);
                startActivity(optionActivity);
            }
        });
        
        mButtonStopCDH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Application Stoppée", Toast.LENGTH_SHORT).show();
            }
        });
        
        mButtonParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View passwdView = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_param_passwd,null);

                AlertDialog.Builder passwdDialog = new AlertDialog.Builder(MainActivity.this);
                passwdDialog.setView(passwdView);

                passwdDialog.setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                String testPasswd ="Test";

                                EditText mEditTextPasswd = passwdView.findViewById(R.id.password_edittext_mdp);

                                if (mEditTextPasswd.getText().toString().equals(testPasswd)){
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                                        }
                                    },500);
                                }else{
                                    Toast.makeText(MainActivity.this, "Mauvais mot de passe", Toast.LENGTH_SHORT).show();
                                }

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.cancel();
                            }
                        })
                        .create()
                        .show();


            }
        });
        
        mButtonMAJ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Mise à jour de l'ATH", Toast.LENGTH_SHORT).show();
            }
        });
        
        
    }




}