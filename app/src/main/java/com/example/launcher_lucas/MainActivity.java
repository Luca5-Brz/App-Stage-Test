package com.example.launcher_lucas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
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
import androidx.annotation.Nullable;
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
    private final List<Integer> mBlockedKeys = new ArrayList<>(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP)); // liste qui contient les boutons ?? bloquer

    //D??claration des composants Visuels de "activity_main.xml"
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
    public String ipAddr;//Adress IP compl??te du device

    public String BaseUrlSrv = "https://launcher.carrieresduhainaut.com/launcherdev/test_gun";
    public String urlSrv;
    public String[] packagesNames = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
    public String apkName = "";

    private long myDownloadReference;
    private boolean downloading = true;

    ArrayList<Button> buttonList = new ArrayList<>(); //Liste les boutons des applications pour ne pas devoir recopeir 12 fois la m??me chose

    int mBadPassword; //Variable pour compter de mot de passe ??ronn?? ??crit pour acc??der aux Param??tres syst??me
    boolean mBlockPassword = false; //Bool pour checker si on dois blocker l'acc??s au MDP ( false = on ouvre, true = on bloque)
    int multiplyDelay;

    Intent checkRunningApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instanciationXMLComponents();
        checkRunningApp = new Intent(MainActivity.this,CheckRunningApp.class);

        mBadPassword = getSharedPreferences("badPassword", MODE_PRIVATE).getInt("badPassword",0);
        //Log.e("BADPASSWORD",""+mBadPassword);

       if(Build.VERSION.SDK_INT < 23 ){
           startProcess();
       }else {
           //Log.e("Check suite logique", "Je vais demander les perms");
           askPermissions();
       }

        ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        am.killBackgroundProcesses("com.example.updatelauncher");

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.e("OnResume","");

        ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        am.killBackgroundProcesses("com.example.updatelauncher");

        setSeekbarLumin();
        startService(checkRunningApp);
        alertDemarrerCheckRunning();
    }

    public void startProcess() {
        //Log.e("STARTPROCESS","StartProcess() d??marr??");
        setIpTitle();

        initialiserLocalisation();

        try {
            urlSrv = BaseUrlSrv + "/StoreRequest.php?gun=" + deviceId+"&numGun="+deviceTitle+"&ip="+ipAddr+"&launcherVersion="+ getPackageManager().getPackageInfo(getPackageName(),0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.e("URL Store Request", urlSrv);

        ConnectionToServer conn = new ConnectionToServer(this);
        conn.execute(urlSrv);


        //Instanciation des diff??rents composants visuels
        instanciationXMLComponents();

        //Parametre la barre de luminosit??
        setSeekbarLumin();

        //Param??tre l'action des diff??rents boutons pr??sent sur l'ATH
        setOnClick();

        //Verrouille l'orientation du Launcher seulement
        setOrientation();

        if (Build.VERSION.SDK_INT <26){ //API 26 est Android 8
            startService(new Intent(MainActivity.this, HUD.class));
        }

        Intent getMessageIntent= new Intent(MainActivity.this,GetMessageService.class);
        urlSrv=BaseUrlSrv+"/ReadMessages.php?DeviceID="+deviceId;
        getMessageIntent.putExtra("urlSrv",urlSrv);
        //Log.e("Test Url Messages",urlSrv);
        startService(getMessageIntent);

        Intent getMessageCibleIntent= new Intent(MainActivity.this,GetMessageCiblesService.class);
        urlSrv=BaseUrlSrv+"/ReadMessagesCibles.php?DeviceID="+deviceId;
        getMessageCibleIntent.putExtra("urlSrv",urlSrv);
        //Log.e("Test Url Messages",urlSrv);
        startService(getMessageCibleIntent);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        if (android.os.Build.VERSION.SDK_INT >= 22){

            if (!hasFocus) {

                Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                sendBroadcast(closeDialog);

                // Method that handles loss of window focus
                new BlockStatusBar(this,false).collapseNow();

            }
        }
    }

    //M??thode pour avoir la mac Addr
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

        //R??cup??ration adresse IP
        if (Build.VERSION.SDK_INT >= 17/*Build.VERSION_CODES.M*/) {

            //On va stocker la premi??re addresse ip du device dans les SharedPrefereces afin d'avoir toujours le m??me Title
            ipAddr = getSharedPreferences("Adresse IP", MODE_PRIVATE).getString("Addresse Ip", null);

            if (ipAddr == null) //Si c'est la premi??re fois qu'on allume le device
            {
                //On r??cupere l'adresse Ip du device
                WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                ipAddr = Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());
                Log.e("IP", ipAddr);


                String[] ipAddrSplit = ipAddr.split("\\."); //On S??pare l'adresse ip pour r??cup??rer le dernier Octet
                deviceTitle = ipAddrSplit[3]; // On ne r??cup??re que le dernier octet afin de l'afficher

                getSharedPreferences("Adresse IP", MODE_PRIVATE)
                        .edit()
                        .putString("Addresse IP", ipAddr)
                        .apply();

            } else { //S'il existe d??j?? une addresse ip pour ce device, on la reprend et l'affiche
                Log.e("IP", ipAddr);
                String[] ipAddrSplit = ipAddr.split("\\.");
                deviceTitle = ipAddrSplit[3];
            }
            setTitle(deviceTitle);

            //On r??cup??re l'addresse MAC pour en faire un ID
            //deviceId = getMacAddr();
            deviceId= Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
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


        buttonList.add(mButton1);
        buttonList.add(mButton2);
        buttonList.add(mButton3);
        buttonList.add(mButton4);
        buttonList.add(mButton5);
        buttonList.add(mButton6);
        buttonList.add(mButton7);
        buttonList.add(mButton8);
        buttonList.add(mButton9);
        buttonList.add(mButton10);
        buttonList.add(mButton11);
        buttonList.add(mButton12);
    }

    //Verrouille l'orientaiton du launcher seulement
    @SuppressLint("SourceLockedOrientationActivity")
    public void setOrientation() {

        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4) // Check si Tablette
        { //Si large ??cran
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        } else { // Si petits ??crans

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        }

    }

    // Param??trage de la barre de luminosit??
    private void setSeekbarLumin() {

        //On va chercher le mode de luminosit?? syst??me actuel
        try {
            mModeLuminosite = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Exception e) {
            //Log.e("tag Lumin", e.toString());
        }

        if (mModeLuminosite == 0) {
            // luminosit?? auto off

            mSeekbarLumin.setVisibility(View.VISIBLE);
            mTextViewLumin.setVisibility(View.VISIBLE);

            //essaye d'acc??der ?? la luminosit?? actuelle et adapte la barre en fonction
            try {
                mSeekbarLumin.setProgress(Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS));
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Impossible d'acc??der au param lum", Toast.LENGTH_SHORT).show();
            }

            //On suis les changements de la barre et on adapte le niveau de luminosit?? en fonction
            mSeekbarLumin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                //La luminosit?? de l'??cran change en m??me temps qu'on slide sur la barre
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
        // Si la luminosit?? auto est active, on cache la barre et le texte pour un effet visuel cool
        if (mModeLuminosite == 1) {
            //luminosit?? auto on
            mSeekbarLumin.setVisibility(View.GONE);
            mTextViewLumin.setVisibility(View.GONE);
        }
    }

    //Blocage des boutons de volume
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        /*D??s qu'on appui sur l'un des boutons de la liste "mBlockedKeys"
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

        for (Button butt : buttonList) {

            butt.getBackground().setAlpha(255);
            if (Build.VERSION.SDK_INT >= 21){
                butt.setBackgroundTintList(null);
            }
            packageName = packagesNames[numBtn];
            numBtn += incrementNumBtn;
            if (!packageName.isEmpty()) {
                try {
                    //on affiche l'icone de l'application sur le bouton
                    Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                    PackageManager packageManager = getApplicationContext().getPackageManager();
                    String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));

                    //on affiche le nom de l'application
                    butt.setText(appName);
                    butt.setBackground(appIcon);
                    butt.setVisibility(View.VISIBLE);

                } catch (PackageManager.NameNotFoundException e) {
                    //e.printStackTrace();
                    butt.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    //Param??tre l'action ?? effectuer lors d'un appui sur un bouton.
    public void setOnClick() {
        mButton1.setOnClickListener(view -> testInstall(packagesNames[0]));
        mButton2.setOnClickListener(view -> testInstall(packagesNames[1]));
        mButton3.setOnClickListener(view -> testInstall(packagesNames[2]));
        mButton4.setOnClickListener(view -> testInstall(packagesNames[3]));
        mButton5.setOnClickListener(view -> testInstall(packagesNames[4]));
        mButton6.setOnClickListener(view -> testInstall(packagesNames[5]));
        mButton7.setOnClickListener(view -> testInstall(packagesNames[6]));
        mButton8.setOnClickListener(view -> testInstall(packagesNames[7]));
        mButton9.setOnClickListener(view -> testInstall(packagesNames[8]));
        mButton10.setOnClickListener(view -> testInstall(packagesNames[9]));
        mButton11.setOnClickListener(view -> testInstall(packagesNames[10]));
        mButton12.setOnClickListener(view -> testInstall(packagesNames[11]));

        /* Lors ce qu'on appuie sur le bouton "Options"
            on ouvre la page avec les options utilisateurs*/
        mButtonOptions.setOnClickListener(view -> {
            Intent optionActivity = new Intent(MainActivity.this, OptionActivity.class);
            startActivity(optionActivity);
        });

        //Kill l'application "CDH" quand on appui sur le bouton "Stop Application CDH"
        mButtonStopCDH.setOnClickListener(view -> {
            Toast.makeText(this, "Killed", Toast.LENGTH_LONG).show();
            ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
            am.killBackgroundProcesses("com.computerland.cdh.mobile");
        });

        //Acc??s aux param??tres d'Android. Action bloqu??e par un mot de passe
        mButtonParams.setOnClickListener(view -> {

            //Cr??ation de l'EditText et attribution de certains attributs
            EditText mPasswdEditText = new EditText(MainActivity.this);
            mPasswdEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPasswdEditText.setTextColor(0xFFFFFF);

            //D??finition du Mot de passe
            final String mPassword;

            Date date = Calendar.getInstance().getTime();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");
            mPassword = dateFormat.format(date);


            AlertDialog dialogBlockPwd = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Veuillez attendre la fin du d??compte ")
                    .setPositiveButton("OK",null)
                    .setCancelable(false)
                    .create();


            if (mBadPassword < 3 && !mBlockPassword){

                //Affichage de l'AlertBox demandant le mot de passe
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Mot de Passe :")
                        .setView(mPasswdEditText)
                        .setPositiveButton("OK", (dialog1, which) -> {
                            if (mPasswdEditText.getText().toString().equals(mPassword)) {

                                startActivity(new Intent(Settings.ACTION_SETTINGS));

                                getSharedPreferences("badPassword", MODE_PRIVATE)
                                        .edit()
                                        .putInt("badPassword", 0)
                                        .apply();

                                getSharedPreferences("multiplyDelay",MODE_PRIVATE)
                                        .edit()
                                        .putInt("multiplyDelay",1)
                                        .apply();

                                urlSrv = BaseUrlSrv+"/LogPasswordParams.php?gun="+deviceId+"&numGun="+deviceTitle+"&status=succeed";
                                LogPasswordParams conn = new LogPasswordParams();
                                conn.execute(urlSrv);


                                if(checkRunningService(CheckRunningApp.class))
                                {
                                    stopService(checkRunningApp);
                                }




                            } else {
                                Toast.makeText(MainActivity.this, "Mauvais Mot De Passe", Toast.LENGTH_SHORT).show();
                                mBadPassword ++;

                                getSharedPreferences("badPassword", MODE_PRIVATE)
                                        .edit()
                                        .putInt("badPassword", mBadPassword)
                                        .apply();
                            }

                            if(mBadPassword == 3){
                                mBlockPassword=true;
                            }

                            if(mBlockPassword){
                                AlertDialog dialogBad = new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Vous avez entr?? trop de mot de passe erron??s")
                                        .setPositiveButton("OK",null)
                                        .setCancelable(false)
                                        .create();
                                dialogBad.show();



                                long delay = 1000*10;

                                multiplyDelay = getSharedPreferences("multiplyDelay",MODE_PRIVATE).getInt("multiplyDelay",1);

                                new Handler().postDelayed(() -> {

                                    dialogBlockPwd.dismiss();

                                    mBlockPassword = false;

                                    mBadPassword=0;
                                    getSharedPreferences("badPassword", MODE_PRIVATE)
                                            .edit()
                                            .putInt("badPassword", mBadPassword)
                                            .apply();

                                    multiplyDelay ++;
                                    getSharedPreferences("multiplyDelay",MODE_PRIVATE)
                                            .edit()
                                            .putInt("multiplyDelay",multiplyDelay)
                                            .apply();

                                    urlSrv = BaseUrlSrv+"/LogPasswordParams.php?gun="+deviceId+"&numGun="+deviceTitle+"&status=failed";
                                    LogPasswordParams conn = new LogPasswordParams();
                                    conn.execute(urlSrv);


                                },delay*multiplyDelay);


                            }

                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();

                mBadPassword = getSharedPreferences("badPassword", MODE_PRIVATE).getInt("badPassword",0);

            }

            if(mBlockPassword){
                dialogBlockPwd.show();
            }


        });

        //Actualise l'affichage du Launcher
        mButtonMAJ.setOnClickListener(view -> {
            ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
            am.killBackgroundProcesses("com.example.updatelauncher");
            testInstall("com.example.updatelauncher");
        });

    }

    //v??rifie si l'application est install??e ou non
    public void testInstall(String packageName){
        Toast.makeText(this, "Veuillez patienter, nous v??rifions les mise ?? jour.", Toast.LENGTH_SHORT).show();

        ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        am.killBackgroundProcesses("com.example.updatelauncher");

        urlSrv = BaseUrlSrv+"/checkVersionLucas.php?package=";
        PackageInfo pkgInfo = null;
        boolean appInstallee=false;
        try {
            pkgInfo = getApplicationContext().getPackageManager().getPackageInfo(packageName, 0);
            appInstallee=true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (appInstallee){
            Log.e("APP IS INSTALL",packageName+" est install??");
            String ver = pkgInfo.versionName;

            //Requete vers le serveur avec la version et le nom du package
            urlSrv += packageName + "&version="+ver+"&numGun="+deviceTitle+"&IdGun="+deviceId;
            Log.e("TEST URL Version", urlSrv);

            CheckVersionOnServer checkVersionOnServer = new CheckVersionOnServer(this);
            checkVersionOnServer.execute(urlSrv);
        }else{
            Log.e("APP IS INSTALL","C'est PAS install??");
            GetApkNameFromServer(packageName);
        }
    }

    //Check le version installe, si bonne version => lance l'app. Si pas t??l??charge la bonne version
    public void checkVersion(String result){
        String[] resultRequest = result.split("[;]");

        //Log.e("ResultRequest[2]",resultRequest[2]);
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(resultRequest[2]); //resultRequest[2] = nom du package

        if (resultRequest[0].equals("OK")) {
            Log.e("VERSION","Bonne version, c'est bon");
            launchIntent.putExtra("id_gun",deviceId);
            startActivity(launchIntent);

        }else if(resultRequest[0].equals("Pas_OK")){
            Log.e("VERSION","Pas Bonne version, je d??sintalle");
            //on d??sinstall l'app
            desinstallApp(resultRequest[2]);

            Log.e("VERSION","Je r??installe l'app");
            //On r??-install l'app
            GetApkNameFromServer(resultRequest[2]);

            Toast.makeText(this, "Pas bonne version", Toast.LENGTH_SHORT).show();
        }
    }

    public void desinstallApp(String appName){
        Toast.makeText(this, "Je d??sinstalle l'App", Toast.LENGTH_SHORT).show();
        File dir = new File(Environment.getExternalStorageDirectory() + "/Android/data/"+getPackageName()+"/files/Download/");
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (String child : children) {
                new File(dir, child).delete();
            }
        }

        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:"+appName));
        startActivity(intent);
    }

    //Demande au User l'acc??s aux permissions voulues
    public void askPermissions() {
        //Log.e("Perms GENERAL", "Je check toutes les perms");
        String[] mPermissionsTab = {Manifest.permission.ACCESS_FINE_LOCATION, //liste des permissions ?? demander
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.INTERNET};

        String mPermissionsCheck = Manifest.permission.ACCESS_FINE_LOCATION + //Liste des permissions ?? v??rifier si on a
                Manifest.permission.READ_EXTERNAL_STORAGE + Manifest.permission.READ_PHONE_STATE
                + Manifest.permission.INTERNET;

        if (Build.VERSION.SDK_INT >= 23) //On check si Android est sup??rieur ?? Android 6
        // car avant on ne dois pas demander les permissions
        {
            //On check si on ?? d??j?? les permissions
            if (ContextCompat.checkSelfPermission(this, mPermissionsCheck) != 0) // 0 = permission accord??e
            {
                //On demande les permissions d'acc??s
                ActivityCompat.requestPermissions(this, mPermissionsTab, 0);
            } else {
                //On a d??j?? les permissions alors on passe ?? le demande suivante
                askPermissionsWriteSettings();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==0){
            if (resultCode==RESULT_CANCELED){
                askPermissionsOverlay();
            }
        }
        /*if(requestCode==1 && resultCode==RESULT_CANCELED){
            askPermissionsNotifications();
        }*/
        if(requestCode==1 && resultCode==RESULT_CANCELED){
            startProcess();
        }

    }

    //Permission d'acc??s aux param??tres syst??me
    public void askPermissionsWriteSettings() {
        //Log.e("Perms GENERAL", "Je Check Parametres");
        if (Build.VERSION.SDK_INT >= 23) {

            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intentWriteSettings = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intentWriteSettings,0);
            }else{
                askPermissionsOverlay();
            }
        }

    }

    public void askPermissionsOverlay(){
        //Log.e("Perms GENERAL", "Je check overlay");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //Toast.makeText(this,"settings overlay",Toast.LENGTH_LONG).show();
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                Intent intentOverlayPermission = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intentOverlayPermission,1);
            }else{
                startProcess();
            }
        }
    }

    /*public void askPermissionsNotifications(){
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !notificationManager.isNotificationPolicyAccessGranted()) {

            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

            startActivityForResult(intent,2);
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0)//Permissions Location
        {
            if (grantResults[0] == 0)// Permission accord??e
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
            Log.e("Exception", "File write failed: " + e);
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
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("login activity", "Can not read file: ");
        }
        return ret;
    }

    public void splitString() {
        String reader = readFromFile(getApplicationContext());
        String[] readerSplit = reader.split("[,;]");

        int i = 0;
        for (String s : readerSplit) {
            if (i == 0) {
                s = s.replaceAll("^\\s+", "");
            }
            packagesNames[i] = s;
            i++;
        }
        startService(checkRunningApp);
        initializeButtons();

    }

    //Demande au serveur l'APK ?? t??l??charger en fonction du nom de Package qu'on lui transmets
    public void GetApkNameFromServer(String packageName) {

        String urlForApk;
        urlForApk = BaseUrlSrv + "/APKRequest.php?package=" + packageName+"&id="+deviceId+"&numGun="+deviceTitle;
        //Log.e("URL for APK's", urlForApk);

        GestionDownload conn = new GestionDownload(this);
        conn.execute(urlForApk);
    }

    //T??l??charge l'APK sur le serveur
    public void download(String apk) {
        String path;//= Environment.getExternalStorageDirectory().getPath();

        path = Environment.getExternalStorageDirectory().getPath() + "/android/data/" + getPackageName() + "/files/Download/";

        apkName = apk;
        String storeUrl = BaseUrlSrv + "/Store/";

        // Si le fichiers existe d??ja, on le supprime
        File f = new File(path + apkName);
        if (f.exists()) {
            f.delete();
        }

        Toast.makeText(this, "Je t??l??charge l'app " + apkName, Toast.LENGTH_SHORT).show();

        //initialisation du gestionnaire de t??l??chargement
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
        progressBarDialog.setTitle("T??l??chargement en cours ...");
        progressBarDialog.setMessage("?? la fin du t??l??chargement, appuyez sur installer");

        progressBarDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBarDialog.setCancelable(false);

        progressBarDialog.setProgress(0);
        //cr??ation d'un thead diff??rent pour le t??l??chargemet (obligatoire)
        new Thread(new Runnable() {

            @SuppressLint("Range")
            @Override
            public void run() {

                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                //tant que cela t??l??charge, on boucle
                while (downloading) {

                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(myDownloadReference); //filter by id which you have receieved when reqesting download from download manager
                    Cursor cursor = manager.query(q);
                    cursor.moveToFirst();
                    int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    //permet de sortir de la boucle quand le t??l??chargement est fini
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                    }

                    final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //affiche o?? en est le t??l??chargement
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
        // "surveille" la fin du t??l??chargement avant de l'??x??cuter///
        ///////////////////////////////////////////////////////////
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {

                final long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (myDownloadReference == reference) {
                    //attendre 1 seconde avant d'??x??cuter
                    android.os.Handler handler = new android.os.Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            String dir = Environment.getExternalStorageDirectory() + "/android/data/" + getPackageName() + "/files/download/";

                            //Log.e("dir", "data uri: " + dir + apkName);
                            File file = new File(dir, apkName);

                            Intent promt = new Intent(Intent.ACTION_VIEW);
                            if (Build.VERSION.SDK_INT >= 24) {

                                Uri uriFile = FileProvider.getUriForFile(getApplicationContext(), getPackageName()+".provider", file);


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
                                //Log.e("dir", "data trouv?? ");
                                startActivity(promt);
                            } else {
                                Toast.makeText(MainActivity.this, "Fichier d'installation non trouv??", Toast.LENGTH_SHORT).show();
                            }

                            downloading = false;
                            progressBarDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Fermer", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Check();
                                    initializeButtons();
                                }
                            });
                            progressBarDialog.dismiss();
                            progressBar();

                            // closeapp();
                        }
                    }, 500);
                }

            }
        };
        registerReceiver(receiver, filter);


    }

    private void progressBar() {
        //cr??e la progressBar de t??l??chargement
        final ProgressDialog progressBarDialog2 = new ProgressDialog(this);
        progressBarDialog2.setTitle("T??l??chargement termin??");
        progressBarDialog2.setMessage("Appuyez sur Fermer");
        progressBarDialog2.setCancelable(false);
        progressBarDialog2.setButton(DialogInterface.BUTTON_NEGATIVE, "Fermer", (dialog, which) -> {
            // Check();
            progressBarDialog2.dismiss();
            initializeButtons();
        });
        progressBarDialog2.show();

    }

    private void initialiserLocalisation() {
        //Log.e("initialiserLocalisation","Entr??");

        Intent SendLocation = new Intent(MainActivity.this,PositionServiceJason.class);
        SendLocation.putExtra("deviceId",deviceId);
        SendLocation.putExtra("deviceTitle",deviceTitle);
        SendLocation.putExtra("BaseUrlSrv",BaseUrlSrv);
        startService(SendLocation);
    }

    public void alertDemarrerCheckRunning(){

        if(!(checkRunningService(CheckRunningApp.class))){

            AlertDialog diagCheckRun = new AlertDialog.Builder(this)
                    .setTitle("Lancer le service ?")
                    .setPositiveButton("OUI", (dialogInterface, i) -> {
                        startService(checkRunningApp);
                    })
                    .setNegativeButton("NON",(dialogInterface, i) -> {
                        stopService(checkRunningApp);
                    })
                    .setCancelable(false)
                    .create();
            diagCheckRun.show();

        }

    }

    public boolean checkRunningService(Class<?> serviceClass){

        ActivityManager AManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : AManager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceClass.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

}