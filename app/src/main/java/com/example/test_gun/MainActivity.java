package com.example.test_gun;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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

    //public String BaseUrlSrv = "http://212.166.21.236:8080";
    public String BaseUrlSrv = "https://launcher.carrieresduhainaut.com/launcherdev";
    public String urlSrv;
    public String[] packagesNames = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
    public String apkName = "";


    private long myDownloadReference;
    private boolean downloading = true;


    LocationManager locationManager = null;
    private String fournisseur;
    GpsLocalisation gpsListener;
    Location localisation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askPermissions();

        setIpTitle();

        initialiserLocalisation();

        urlSrv = BaseUrlSrv + "/StoreRequest.php?gun=" + deviceId+"&coordLg="+localisation.getLongitude()+"&coordLt="+localisation.getLatitude();
        Log.e("URL", urlSrv);

        ConnectionToServer conn = new ConnectionToServer(this);
        conn.execute(urlSrv);

        //Instanciation de différents composants visuels
        instanciationXMLComponents();

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
        initializeButtons();

        initialiserLocalisation();

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
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
            }
        } catch (Exception ignored) {
        }
        return res1.toString();
    }

    public void setIpTitle() {

        //Récupération adresse IP
        if (Build.VERSION.SDK_INT >= 18/*Build.VERSION_CODES.M*/) {
            //Demande des permissions
            //askPermissions();

            //On va stocker la première addresse ip du device dans les SharedPrefereces afin d'avoir toujours le même Title
            ipAddr = getSharedPreferences("Adresse IP", MODE_PRIVATE).getString("Addresse Ip", null);

            if (ipAddr == null) //Si c'est la premiére fois qu'on allume le device
            {
                //On récupere l'adresse Ip du device
                WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                ipAddr = Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());
                Log.e("IP", ipAddr);


                String[] ipAddrSplit = ipAddr.split("\\."); //On Sépare l'adresse ip pour récupérer le dernier Octet
                deviceTitle = ipAddrSplit[3]; // On ne récupére que le dernier octet afin de l'afficher

                getSharedPreferences("Adresse IP", MODE_PRIVATE)
                        .edit()
                        .putString("Addresse Ip", ipAddr)
                        .apply();

            } else { //S'il existe déjà une addresse ip pour ce device, on la reprend et l'affiche
                String[] ipAddrSplit = ipAddr.split("\\.");
                deviceTitle = ipAddrSplit[3];
            }
            setTitle(deviceTitle);

            //On récupére l'addresse MAC pour en faire un ID
            deviceId = getMacAddr();
            Log.e("ID", deviceId);

        }


    }

    public void instanciationXMLComponents() {

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


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        //Cache la barre de status
        hideStatusBar();
    }

    //Verrouille l'orientaiton du launcher seulement
    @SuppressLint("SourceLockedOrientationActivity")
    public void setOrientation() {

        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4) // Check si Tablette
        { //Si large écran
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        } else { // Si petits écrans

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        }

    }

    //Cache la barre de Status
    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Paramètrage de la barre de luminosité
    private void setSeekbarLumin() {

        //On va chercher le mode de luminosité système actuel
        try {
            mModeLuminosite = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Exception e) {
            Log.d("tag", e.toString());
        }

        if (mModeLuminosite == 0) {
            // luminosité auto off

            mSeekbarLumin.setVisibility(View.VISIBLE);
            mTextViewLumin.setVisibility(View.VISIBLE);

            //essaye d'accèder à la luminosité actuelle et adapte la barre en fonction
            try {
                mSeekbarLumin.setProgress(Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS));
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Impossible d'accéder au param lum", Toast.LENGTH_SHORT).show();
            }

            //On suis les changements de la barre et on adapte le niveau de luminosité en fonction
            mSeekbarLumin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                //La luminosité de l'écran change en même temps qu'on slide sur la barre
                @Override
                public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, progressValue);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

        }
        // Si la luminosité auto est active, on cache la barre et le texte pour un effet visuel cool
        if (mModeLuminosite == 1) {
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
        if (mBlockedKeys.contains(event.getKeyCode())) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }

    }

    //Initialisation des boutons
    @SuppressLint("NewApi")
    public void initializeButtons() {
        String packageName;
        int numBtn = 0;
        int incrementNumBtn = 1;

        mButton1.getBackground().setAlpha(255);
        mButton1.setBackgroundTintList(null);
        packageName = packagesNames[numBtn];
        numBtn += incrementNumBtn;
        if (!packageName.isEmpty()) {
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

        mButton2.getBackground().setAlpha(255);
        mButton2.setBackgroundTintList(null);
        packageName = packagesNames[numBtn];
        numBtn += incrementNumBtn;
        if (!packageName.isEmpty()) {
            try {
                //on affiche l'icone de l'application sur le bouton
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                //on affiche le nom de l'application
                mButton2.setText(appName);
                mButton2.setBackground(appIcon);
                mButton2.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mButton2.setVisibility(View.VISIBLE);
            }
        }

        mButton3.getBackground().setAlpha(255);
        mButton3.setBackgroundTintList(null);
        packageName = packagesNames[numBtn];
        numBtn += incrementNumBtn;
        if (!packageName.isEmpty()) {
            try {
                //on affiche l'icone de l'application sur le bouton
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                //on affiche le nom de l'application
                mButton3.setText(appName);
                mButton3.setBackground(appIcon);
                mButton3.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mButton3.setVisibility(View.VISIBLE);
            }
        }

        mButton4.getBackground().setAlpha(255);
        mButton4.setBackgroundTintList(null);
        packageName = packagesNames[numBtn];
        numBtn += incrementNumBtn;
        if (!packageName.isEmpty()) {
            try {
                //on affiche l'icone de l'application sur le bouton
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                //on affiche le nom de l'application
                mButton4.setText(appName);
                mButton4.setBackground(appIcon);
                mButton4.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mButton4.setVisibility(View.VISIBLE);
            }
        }

        mButton5.getBackground().setAlpha(255);
        mButton5.setBackgroundTintList(null);
        packageName = packagesNames[numBtn];
        numBtn += incrementNumBtn;
        if (!packageName.isEmpty()) {
            try {
                //on affiche l'icone de l'application sur le bouton
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                //on affiche le nom de l'application
                mButton5.setText(appName);
                mButton5.setBackground(appIcon);
                mButton5.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mButton5.setVisibility(View.VISIBLE);
            }
        }

        mButton6.getBackground().setAlpha(255);
        mButton6.setBackgroundTintList(null);
        packageName = packagesNames[numBtn];
        numBtn += incrementNumBtn;
        if (!packageName.isEmpty()) {
            try {
                //on affiche l'icone de l'application sur le bouton
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                //on affiche le nom de l'application
                mButton6.setText(appName);
                mButton6.setBackground(appIcon);
                mButton6.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mButton6.setVisibility(View.VISIBLE);
            }
        }

        mButton7.getBackground().setAlpha(255);
        mButton7.setBackgroundTintList(null);
        packageName = packagesNames[numBtn];
        numBtn += incrementNumBtn;
        if (!packageName.isEmpty()) {
            try {
                //on affiche l'icone de l'application sur le bouton
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                //on affiche le nom de l'application
                mButton7.setText(appName);
                mButton7.setBackground(appIcon);
                mButton7.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mButton7.setVisibility(View.VISIBLE);
            }
        }

        mButton8.getBackground().setAlpha(255);
        mButton8.setBackgroundTintList(null);
        packageName = packagesNames[numBtn];
        numBtn += incrementNumBtn;
        if (!packageName.isEmpty()) {
            try {
                //on affiche l'icone de l'application sur le bouton
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                //on affiche le nom de l'application
                mButton8.setText(appName);
                mButton8.setBackground(appIcon);
                mButton8.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mButton8.setVisibility(View.VISIBLE);
            }
        }

        mButton9.getBackground().setAlpha(255);
        mButton9.setBackgroundTintList(null);
        packageName = packagesNames[numBtn];
        numBtn += incrementNumBtn;
        if (!packageName.isEmpty()) {
            try {
                //on affiche l'icone de l'application sur le bouton
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                //on affiche le nom de l'application
                mButton9.setText(appName);
                mButton9.setBackground(appIcon);
                mButton9.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mButton9.setVisibility(View.VISIBLE);
            }
        }

        mButton10.getBackground().setAlpha(255);
        mButton10.setBackgroundTintList(null);
        packageName = packagesNames[numBtn];
        numBtn += incrementNumBtn;
        if (!packageName.isEmpty()) {
            try {
                //on affiche l'icone de l'application sur le bouton
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                //on affiche le nom de l'application
                mButton10.setText(appName);
                mButton10.setBackground(appIcon);
                mButton10.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mButton10.setVisibility(View.VISIBLE);
            }
        }

        mButton11.getBackground().setAlpha(255);
        mButton11.setBackgroundTintList(null);
        packageName = packagesNames[numBtn];
        numBtn += incrementNumBtn;
        if (!packageName.isEmpty()) {
            try {
                //on affiche l'icone de l'application sur le bouton
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                //on affiche le nom de l'application
                mButton11.setText(appName);
                mButton11.setBackground(appIcon);
                mButton11.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mButton11.setVisibility(View.VISIBLE);
            }
        }

        mButton12.getBackground().setAlpha(255);
        mButton12.setBackgroundTintList(null);
        packageName = packagesNames[numBtn];
        numBtn += incrementNumBtn;
        if (!packageName.isEmpty()) {
            try {
                //on affiche l'icone de l'application sur le bouton
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                //on affiche le nom de l'application
                mButton12.setText(appName);
                mButton12.setBackground(appIcon);
                mButton12.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mButton12.setVisibility(View.VISIBLE);
            }
        }


    }

    //Paramètre l'action à effectuer lors d'un appui sur un bouton.
    public void setOnClick() {

        mButton1.setOnClickListener(view -> {

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packagesNames[0]);
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }//si cela n'existe pas --> download it
            else {
                GetApkNameFromServer(packagesNames[0]);
            }

        });
        mButton2.setOnClickListener(view -> {

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packagesNames[1]);
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }//si cela n'existe pas --> download it
            else {
                GetApkNameFromServer(packagesNames[1]);
            }

        });
        mButton3.setOnClickListener(view -> {

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packagesNames[2]);
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }//si cela n'existe pas --> download it
            else {
                GetApkNameFromServer(packagesNames[2]);
            }

        });
        mButton4.setOnClickListener(view -> {

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packagesNames[3]);
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }//si cela n'existe pas --> download it
            else {
                GetApkNameFromServer(packagesNames[3]);
            }

        });
        mButton5.setOnClickListener(view -> {

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packagesNames[4]);
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }//si cela n'existe pas --> download it
            else {
                GetApkNameFromServer(packagesNames[4]);
            }

        });
        mButton6.setOnClickListener(view -> {

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packagesNames[5]);
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }//si cela n'existe pas --> download it
            else {
                GetApkNameFromServer(packagesNames[5]);
            }

        });
        mButton7.setOnClickListener(view -> {

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packagesNames[6]);
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }//si cela n'existe pas --> download it
            else {
                GetApkNameFromServer(packagesNames[6]);
            }

        });
        mButton8.setOnClickListener(view -> {

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packagesNames[7]);
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }//si cela n'existe pas --> download it
            else {
                GetApkNameFromServer(packagesNames[7]);
            }

        });
        mButton9.setOnClickListener(view -> {

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packagesNames[8]);
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }//si cela n'existe pas --> download it
            else {
                GetApkNameFromServer(packagesNames[8]);
            }

        });
        mButton10.setOnClickListener(view -> {

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packagesNames[9]);
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }//si cela n'existe pas --> download it
            else {
                GetApkNameFromServer(packagesNames[9]);
            }

        });
        mButton11.setOnClickListener(view -> {

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packagesNames[10]);
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }//si cela n'existe pas --> download it
            else {
                GetApkNameFromServer(packagesNames[10]);
            }

        });
        mButton12.setOnClickListener(view -> {

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packagesNames[11]);
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }//si cela n'existe pas --> download it
            else {
                GetApkNameFromServer(packagesNames[11]);
            }

        });


        /* Lors ce qu'on appuie sur le bouton "Options"
            on ouvre la page avec les options utilisateurs*/
        mButtonOptions.setOnClickListener(view -> {
            Intent optionActivity = new Intent(MainActivity.this, OptionActivity.class);
            startActivity(optionActivity);
        });

        //Kill l'applicatin "CDH" quand on appui sur le bouton "Stop Application CDH"
        mButtonStopCDH.setOnClickListener(view -> {

        });

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

                            new Handler().postDelayed(() -> startActivity(new Intent(Settings.ACTION_SETTINGS)), 0);

                        } else {
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
    public void askPermissions() {
        String[] mPermissionsTab = {Manifest.permission.ACCESS_FINE_LOCATION, //liste des permissions à demander
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.INTERNET};

        String mPermissionsCheck = Manifest.permission.ACCESS_FINE_LOCATION + //Liste des permissions à vérifier si on a
                Manifest.permission.READ_EXTERNAL_STORAGE + Manifest.permission.READ_PHONE_STATE
                + Manifest.permission.INTERNET;

        if (Build.VERSION.SDK_INT >= 23) //On check si Android est supérieur à Android 6
        // car avant on ne dois pas demander les permissions
        {
            //On check si on à déjà les permissions
            if (ContextCompat.checkSelfPermission(this, mPermissionsCheck) != 0) // 0 = permission accordée
            {
                //On demande les permissions d'accés
                ActivityCompat.requestPermissions(this, mPermissionsTab, 0);
            } else {
                //On a déjà les permissions alors on passe à le demande suivante
                askPermissionsWriteSettings();
            }
        }
    }

    //Permission d'accés aux paramétres système
    public void askPermissionsWriteSettings() {

        if (Build.VERSION.SDK_INT >= 23) {

            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intentWriteSettings = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intentWriteSettings);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0)//Permissions Location
        {
            if (grantResults[0] == 0)// Permission accordée
            {
                askPermissionsWriteSettings();
            }
        }

    }

    public void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("buttonConfig.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("buttonConfig.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: ");
            Log.e("login activity", "Can not read file: ");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public void splitString() {

        String reader = readFromFile(getApplicationContext());
        String[] readerSplit = reader.split("[,;]");

        int i = 0;
        /*for (int i=0;i<=(readerSplit.length/3)-1;i++){

            readerSplit[s]=readerSplit[s].replaceAll("[\\s+]","");

            packagesNames[i]=readerSplit[s];
            s += 3;
        }*/
        for (String s : readerSplit) {

            if (i == 0) {

                s = s.replaceAll("^\\s+", "");

            }

            packagesNames[i] = s;

            i++;
        }
        initializeButtons();

    }

    //Demande au serveur l'APK à télécharger en fonction du nom de Package qu'on lui transmets
    public void GetApkNameFromServer(String packageName) {

        String urlForApk;
        urlForApk = BaseUrlSrv + "/APKRequest.php?package=" + packageName;
        Log.e("URL for APK's", urlForApk);

        GestionDownload conn = new GestionDownload(this);
        conn.execute(urlForApk);
    }

    //Télécharge l'APK sur le serveur
    public void download(String apk) {
        String path;//= Environment.getExternalStorageDirectory().getPath();

        //int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        //if (currentapiVersion <= android.os.Build.VERSION_CODES.LOLLIPOP_MR1){
        //    // Do something for JellyBean 4.2.2
        //    path = "/mnt/sdcard/Android/data/com.example.info_jvs.launcher/files/Download";

        //}
        //else{
        //    // do something for phones running an SDK above JellyBean
        //    path = Environment.getExternalStorageDirectory() + "/android/data/com.example.info_jvs.launcher/files/Download/";

        //}
        path = Environment.getExternalStorageDirectory().getPath() + "/android/data/" + getPackageName() + "/files/Download/";

        apkName = apk;
        String storeUrl = BaseUrlSrv + "/Store/";

        // Si le fichiers existe déja, on le supprime
        File f = new File(path + apkName);
        if (f.exists()) {
            f.delete();
        }

        Toast.makeText(this, "Je télécharge l'app " + apkName, Toast.LENGTH_SHORT).show();

        //initialisation du gestionnaire de téléchargement
        Intent intent2 = new Intent(Intent.ACTION_VIEW);
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(storeUrl + apkName);
        final DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

        //Set the local destination for the downloaded file to a path within the application's external files directory
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, apkName);
        myDownloadReference = downloadManager.enqueue(request);

        //////////////////
        ///progressBar///
        ////////////////

        final ProgressDialog progressBarDialog = new ProgressDialog(this);
        progressBarDialog.setTitle("Téléchargement en cours ...");
        progressBarDialog.setMessage("À la fin du téléchargement, appuyez sur installer");

        progressBarDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBarDialog.setCancelable(false);

        progressBarDialog.setProgress(0);
        //création d'un thead différent pour le téléchargemet (obligatoire)
        new Thread(new Runnable() {

            @Override
            public void run() {

                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                //tant que cela télécharge, on boucle
                while (downloading) {

                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(myDownloadReference); //filter by id which you have receieved when reqesting download from download manager
                    Cursor cursor = manager.query(q);
                    cursor.moveToFirst();
                    int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    //permet de sortir de la boucle quand le téléchargement est fini
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                    }

                    final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //affiche où en est le téléchargement
                            progressBarDialog.setProgress((int) dl_progress);

                        }
                    });

                    cursor.close();
                }

            }
        }).start();


        //show the dialog
        progressBarDialog.show();
        //////////////////////
        //Fin progressBar////
        ////////////////////


        /////////////////////////////////////////////////////////////
        // "surveille" la fin du téléchargement avant de l'éxécuter///
        ///////////////////////////////////////////////////////////
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {

                final long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (myDownloadReference == reference) {
                    //attendre 1 seconde avant d'éxécuter
                    android.os.Handler handler = new android.os.Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            //executer le fichier
                            //String dir="";
                            //int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                            //if (currentapiVersion <= android.os.Build.VERSION_CODES.LOLLIPOP_MR1){
                            //    // Do something for JellyBean and above versions
                            //    dir = Environment.getExternalStorageDirectory().getPath()+"/Android/data/"+getPackageName()+"/files/Download";

                            //}
                            //else{
                            //    // do something for phones running an SDK above JellyBean
                            //    dir = Environment.getExternalStorageDirectory() + "/android/data/"+getPackageName()+"/files/Download/";
                            //}
                            String dir = Environment.getExternalStorageDirectory() + "/android/data/" + getPackageName() + "/files/download/";

                            Log.e("dir", "data uri: " + dir + apkName);
                            File file = new File(dir, apkName);

                            Intent promt = new Intent(Intent.ACTION_VIEW);
                            if (Build.VERSION.SDK_INT >= 24) {

                                Uri uriFile = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", file);


                                promt.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                promt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                promt.setDataAndType(uriFile, "application/vnd.android.package-archive");
                                startActivity(promt);

                            } else {
                                promt.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                                promt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                promt.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                            }

                            if (file.exists()) {
                                Log.e("dir", "data trouvé ");
                                startActivity(promt);
                            } else {
                                Toast.makeText(MainActivity.this, "Fichier d'installation non trouvé", Toast.LENGTH_SHORT).show();
                            }

                            downloading = false;
                            progressBarDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Fermer", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Check();
                                    initializeButtons();
                                }
                            });
                            progressBarDialog.hide();
                            progressBar();

                            // closeapp();
                        }
                    }, 600);
                }

            }
        };
        registerReceiver(receiver, filter);


    }

    private void progressBar() {
        //crée la progressBar de téléchargement
        final ProgressDialog progressBarDialog2 = new ProgressDialog(this);
        progressBarDialog2.setTitle("Téléchargement terminé");
        progressBarDialog2.setMessage("Appuyez sur Fermer");
        progressBarDialog2.setCancelable(false);
        progressBarDialog2.setButton(DialogInterface.BUTTON_NEGATIVE, "Fermer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Check();
                initializeButtons();
            }
        });
        progressBarDialog2.show();

    }

    private void initialiserLocalisation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Log.e("Perms GPS","J'ai les perms");

            if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteres = new Criteria();

            // la précision  : (ACCURACY_FINE pour une haute précision ou ACCURACY_COARSE pour une moins bonne précision)
            criteres.setAccuracy(Criteria.ACCURACY_FINE);

            // l'altitude
            criteres.setAltitudeRequired(true);

            // la direction
            criteres.setBearingRequired(true);

            // la vitesse
            criteres.setSpeedRequired(true);

            // la consommation d'énergie demandée
            criteres.setCostAllowed(true);
            criteres.setPowerRequirement(Criteria.POWER_HIGH);

            fournisseur = locationManager.getBestProvider(criteres, true);
            Log.d("GPS", "fournisseur : " + fournisseur);
            }


            if (fournisseur != null) {
                // dernière position connue
                localisation = locationManager.getLastKnownLocation(fournisseur);
                gpsListener = new GpsLocalisation();

                if (localisation != null) {
                    // on notifie la localisation
                    gpsListener.onLocationChanged(localisation);

                }


                // on configure la mise à jour automatique : au moins 10 mètres et 15 secondes

                locationManager.requestLocationUpdates(fournisseur, 3600000, 10, gpsListener);

                AlertDialog diagCoord = new AlertDialog.Builder(this)
                        .setTitle("Coordonnées")
                        .setMessage("Longitude : "+localisation.getLongitude()+"\nLatitude : "+localisation.getLatitude())
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create();
                diagCoord.show();

            }
        }else{
            Log.e("Perms GPS","J'ai pas les perms");
        }
    }

}