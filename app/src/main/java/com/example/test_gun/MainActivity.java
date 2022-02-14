package com.example.test_gun;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    int mModeLuminosite; //Si le telephone est en luminosite auto ou pas
    private final List mBlockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP)); // liste qui contient les boutons à bloquer

    //Initialisiatoin des composants Visuels de "activity_main.xml"
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

        //Test récupération adresse MAC
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String macAddress = manager.getConnectionInfo().getMacAddress();
        setTitle(macAddress);

        //Demande des permissions
        askPermissions();

        //Instanciation de différents composants visuels
        mButtonOptions = findViewById(R.id.main_button_options);
        mButtonStopCDH = findViewById(R.id.main_button_StopCDH);
        mButtonParams = findViewById(R.id.main_button_parametres);
        mButtonMAJ = findViewById(R.id.main_button_MiseAJour);
        mSeekbarLumin = findViewById(R.id.main_seekbar_luminosity);
        mTextViewLumin = findViewById(R.id.main_textview_luminosity);

        //Parametre la barre de luminosité
        setSeekbarLumin();

        //Paramètre l'action des différents boutons présent sur l'ATH
        setOnClick();

        //Verrouille l'orientation du Launcher seulement
        setOrientation();

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Re-Affiche la barre de luminosité en fonciton de l'activation ou non du mode auto
        setSeekbarLumin();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        //Cache la barre de status
            hideStatusBar();
    }

    //Verrouille l'orientaiton du launcher seulement
    public void setOrientation(){

        if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4) // Check si Tablette
        { //Si large écran
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);

        }else{ // Si petits écrans

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        }

    }

    //Cache la barre de Status
    private void hideStatusBar(){
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
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

                //La luminosité de l'écran change en même temps qu'on slide sur la barre
                @Override
                public void onProgressChanged(SeekBar seekBar, int progressValue,boolean fromUser)
                {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, progressValue);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {
                }
            });

        }
        // Si la luminosité auto est active, on cache la barre et le texte pour un effet visuel cool
        if (mModeLuminosite==1){
            //luminosité auto on
            mSeekbarLumin.setVisibility(View.GONE);
            mTextViewLumin.setVisibility(View.GONE);
        }
    }

    //Blocage des boutons de volume
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        /*Dés qu'on appui sur l'un des boutons de la liste "mBlockedKeys"
         on bloque l'action initiale de ce bouton*/
        if (mBlockedKeys.contains(event.getKeyCode())){
            return  true;
        }else{
            return super.dispatchKeyEvent(event);
        }

    }

    //Paramètre l'action à effectuer lors d'un appui sur un bouton.
    public void setOnClick() {
        /* Lors ce qu'on appuie sur le bouton "Options"
            on ouvre la page avec les options utilisateurs*/
        mButtonOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent optionActivity = new Intent(MainActivity.this, OptionActivity.class);
                startActivity(optionActivity);
            }
        });

        //Kill l'applicatin "CDH" quand on appui sur le bouton "Stop Application CDH"
        mButtonStopCDH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(MainActivity.this, "Application Stoppée", Toast.LENGTH_SHORT).show();

            }
        });

        //Accés aux paramétres d'Android. Action bloquée par un mot de passe
        mButtonParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Création de l'EditText et attribution de certains attributs
                EditText mPasswdEditText = new EditText(MainActivity.this);
                mPasswdEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                mPasswdEditText.setTextColor(0xFFFFFF);

                //Définition du Mot de passe
                final String mPassword;

                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");
                mPassword = dateFormat.format(date);


                //Affichage de l'AlertBox demandant le mot de passe
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Mot de Passe :")
                        .setView(mPasswdEditText)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (mPasswdEditText.getText().toString().equals(mPassword)) {

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                                        }
                                    },0);

                                }else{
                                    Toast.makeText(MainActivity.this, "Mauvais Mot De Passe", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();

            }
        });

        //Actualise l'affichage du Launcher
        mButtonMAJ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Mise à jour de l'ATH", Toast.LENGTH_SHORT).show();
            }
        });

    }


    //Demande au User l'accés aux permissions voulues
    public void askPermissions(){
        String[] mPermissionsTab = {Manifest.permission.ACCESS_FINE_LOCATION, //liste des permissions à demander
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_PHONE_STATE};

        String mPermissionsCheck = Manifest.permission.ACCESS_FINE_LOCATION + //Liste des permissions à vérifier si on a
                Manifest.permission.READ_EXTERNAL_STORAGE + Manifest.permission.READ_PHONE_STATE;

        if(Build.VERSION.SDK_INT >= 23) //On check si Android est supérieur à Android 6
                                        // car avant on ne dois pas demander les permissions
        {
            //On check si on à déjà les permissions
            if (ContextCompat.checkSelfPermission(this, mPermissionsCheck) != 0 ) // 0 = permission accordée
            {
                //On demande les permissions d'accés
                ActivityCompat.requestPermissions(this, mPermissionsTab, 0);
            }
            else
            {
                //On a déjà les permissions alors on passe à le demande suivante
                askPermissionsWriteSettings();
            }
        }
    }

    /*public void askPermissionsStorage(){
        if (Build.VERSION.SDK_INT >= 23){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                //Toast.makeText(this,"ask storage set",Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

            } else {
                // Permission has already been granted
                // Toast.makeText(this,"already storage set",Toast.LENGTH_LONG).show();
            }
        }
    }*/

    public void askPermissionsWriteSettings(){

        if (Build.VERSION.SDK_INT >= 23){

            if(!Settings.System.canWrite(getApplicationContext())){
                Intent intentWriteSettings = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:"+getPackageName()));
                startActivity(intentWriteSettings);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==0)//Permissions Location
        {
            if (grantResults[0]==0)// Permission accordée
            {
                askPermissionsWriteSettings();
            }
        }

    }
}