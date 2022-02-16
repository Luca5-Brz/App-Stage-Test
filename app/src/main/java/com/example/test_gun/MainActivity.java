package com.example.test_gun;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.text.format.Formatter;
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

import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.example.test_gun.ConnectionToServer;

public class MainActivity extends AppCompatActivity{

    int mModeLuminosite; //Si le telephone est en luminosite auto ou pas
    private final List<Integer> mBlockedKeys = new ArrayList<>(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP)); // liste qui contient les boutons à bloquer

    //Déclaration des composants Visuels de "activity_main.xml"
    private Button mButtonOptions;
    private Button mButtonStopCDH;
    private Button mButtonParams;
    private Button mButtonMAJ;
    public SeekBar mSeekbarLumin;
    public TextView mTextViewLumin;
    public Button mButton1;
    public Button mButton2;
    public Button mButton3;
    public Button mButton4;
    public Button mButton5;
    public Button mButton6;
    public Button mButton7;
    public Button mButton8;
    public Button mButton9;
    public Button mButton10;
    public Button mButton11;
    public Button mButton12;

    public String deviceId; // Id de la Device pour les logs
    public String deviceTitle; // Variable pour afficher un Titre sur le Gun
    public String ipAddr;//Adress IP compléte du device

    public String urlSrv = "http://212.166.21.236:8080/StoreRequest.php";
    public String[] packagesNames = {"com.computerland.cdh.mobile"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectionToServer conn = new ConnectionToServer();
        conn.execute(urlSrv);


        //Récupération adresse MAC
        if (Build.VERSION.SDK_INT >= 18/*Build.VERSION_CODES.M*/) {
            //Demande des permissions
            askPermissions();

            //On va stocker la première addresse ip du device dans les SharedPrefereces afin d'avoir toujours le même Title
            ipAddr = getSharedPreferences("Adresse IP", MODE_PRIVATE).getString("Addresse Ip",null);

            if(ipAddr == null) //Si c'est la premiére fois qu'on allume le device
            {
                //On récupere l'adresse Ip du device
                WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                ipAddr = Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());


                String[] ipAddrSplit = ipAddr.split("\\."); //On Sépare l'adresse ip pour récupérer le dernier Octet
                deviceTitle = ipAddrSplit[3]; // On ne récupére que le dernier octet afin de l'afficher

                getSharedPreferences("Adresse IP", MODE_PRIVATE)
                        .edit()
                        .putString("Addresse Ip",ipAddr)
                        .apply();

            }else{ //S'il existe déjà une addresse ip pour ce device, on la reprend et l'affiche
                String[] ipAddrSplit = ipAddr.split("\\.");
                deviceTitle=ipAddrSplit[3];
            }
            setTitle(deviceTitle);

            //On récupére l'addresse MAC pour en faire un ID
            deviceId = getMacAddr();

        }

        //Instanciation de différents composants visuels
        mButtonOptions = findViewById(R.id.main_button_options);
        mButtonStopCDH = findViewById(R.id.main_button_StopCDH);
        mButtonParams = findViewById(R.id.main_button_parametres);
        mButtonMAJ = findViewById(R.id.main_button_MiseAJour);
        mSeekbarLumin = findViewById(R.id.main_seekbar_luminosity);
        mTextViewLumin = findViewById(R.id.main_textview_luminosity);
        mButton1 = findViewById(R.id.main_button1);
        mButton2 = findViewById(R.id.main_button2);
        mButton3 = findViewById(R.id.main_button3);
        mButton4 = findViewById(R.id.main_button4);
        mButton5 = findViewById(R.id.main_button5);
        mButton6 = findViewById(R.id.main_button6);
        mButton7 = findViewById(R.id.main_button7);
        mButton8 = findViewById(R.id.main_button8);
        mButton9 = findViewById(R.id.main_button9);
        mButton10 = findViewById(R.id.main_button10);
        mButton11 = findViewById(R.id.main_button11);
        mButton12 = findViewById(R.id.main_button12);

        //Parametre la barre de luminosité
        setSeekbarLumin();

        //Paramètre l'action des différents boutons présent sur l'ATH
        setOnClick();
        initializeButtons();

        //Verrouille l'orientation du Launcher seulement
        setOrientation();

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Re-Affiche la barre de luminosité en fonciton de l'activation ou non du mode auto
        setSeekbarLumin();

    }

    //Méthode pour avoir la mac Addr
    public static String getMacAddr() {
        StringBuilder res1 = new StringBuilder();
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("p2p0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    continue;
                }

                res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
            }
        } catch (Exception ignored) {
        }
        return res1.toString();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        //Cache la barre de status
            hideStatusBar();
    }

    //Verrouille l'orientaiton du launcher seulement
    @SuppressLint("SourceLockedOrientationActivity")
    public void setOrientation(){

        if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4) // Check si Tablette
        { //Si large écran
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

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

    //Initialisation des boutons
    public void initializeButtons(){

        mButton1.getBackground().setAlpha(255);
        String packageName = packagesNames[0];
        try {
            //on affiche l'icone de l'application sur le bouton
            Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
            PackageManager packageManager = getApplicationContext().getPackageManager();
            String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
            //on affiche le nom de l'application
            mButton1.setText(appName);
            mButton1.setBackground(appIcon);
            mButton1.setVisibility(View.VISIBLE);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            mButton1.setVisibility(View.VISIBLE);
        }


    }

    //Paramètre l'action à effectuer lors d'un appui sur un bouton.
    public void setOnClick() {

        mButton1.setOnClickListener(view -> {
            Toast.makeText(this, "Lancement App", Toast.LENGTH_SHORT).show();
        });


        /* Lors ce qu'on appuie sur le bouton "Options"
            on ouvre la page avec les options utilisateurs*/
        mButtonOptions.setOnClickListener(view -> {
            Intent optionActivity = new Intent(MainActivity.this, OptionActivity.class);
            startActivity(optionActivity);
        });

        //Kill l'applicatin "CDH" quand on appui sur le bouton "Stop Application CDH"
        mButtonStopCDH.setOnClickListener(view -> Toast.makeText(MainActivity.this, "Application Stoppée", Toast.LENGTH_SHORT).show());

        //Accés aux paramétres d'Android. Action bloquée par un mot de passe
        mButtonParams.setOnClickListener(view -> {

            //Création de l'EditText et attribution de certains attributs
            EditText mPasswdEditText = new EditText(MainActivity.this);
            mPasswdEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPasswdEditText.setTextColor(0xFFFFFF);

            //Définition du Mot de passe
            final String mPassword;

            Date date = Calendar.getInstance().getTime();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");
            mPassword = dateFormat.format(date);


            //Affichage de l'AlertBox demandant le mot de passe
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Mot de Passe :")
                    .setView(mPasswdEditText)
                    .setPositiveButton("OK", (dialog1, which) -> {

                        if (mPasswdEditText.getText().toString().equals(mPassword)) {

                            new Handler().postDelayed(() -> startActivity(new Intent(Settings.ACTION_SETTINGS)),0);

                        }else{
                            Toast.makeText(MainActivity.this, "Mauvais Mot De Passe", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            dialog.show();

        });

        //Actualise l'affichage du Launcher
        mButtonMAJ.setOnClickListener(view -> Toast.makeText(MainActivity.this, "Mise à jour de l'ATH", Toast.LENGTH_SHORT).show());

    }

    //Demande au User l'accés aux permissions voulues
    public void askPermissions(){
        String[] mPermissionsTab = {Manifest.permission.ACCESS_FINE_LOCATION, //liste des permissions à demander
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.INTERNET};

        String mPermissionsCheck = Manifest.permission.ACCESS_FINE_LOCATION + //Liste des permissions à vérifier si on a
                Manifest.permission.READ_EXTERNAL_STORAGE + Manifest.permission.READ_PHONE_STATE
                + Manifest.permission.INTERNET;

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

    //Permission d'accés aux paramétres système
    public void askPermissionsWriteSettings(){

        if (Build.VERSION.SDK_INT >= 23){

            if(!Settings.System.canWrite(getApplicationContext())){
                Intent intentWriteSettings = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:"+getPackageName()));
                startActivity(intentWriteSettings);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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