package com.example.test_gun;

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

    public String BaseUrlSrv = "https://launcher.carrieresduhainaut.com/launcherdev/test_gun";
    public String urlSrv;
    public String[] packagesNames = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
    public String apkName = "";

    private long myDownloadReference;
    private boolean downloading = true;

    ArrayList<Button> buttonList = new ArrayList<>(); //Liste les boutons des applications pour ne pas devoir recopeir 12 fois la même chose

    int mBadPassword; //Variable pour compter de mot de passe éronné écrit pour accéder aux Paramétres système
    boolean mBlockPassword = false; //Bool pour checker si on dois blocker l'accés au MDP ( false = on ouvre, true = on bloque)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBadPassword = getSharedPreferences("badPassword", MODE_PRIVATE).getInt("badPassword",0);
        Log.e("BADPASSWORD",""+mBadPassword);

       if(Build.VERSION.SDK_INT < 23 ){
           startProcess();
       }else {
           Log.e("Check suite logique", "Je vais demander les perms");
           askPermissions();
       }

    }

    public void startProcess(){
        Log.e("STARTPROCESS","StartProcess() démarré");
        setIpTitle();

        initialiserLocalisation();

        urlSrv = BaseUrlSrv + "/StoreRequest.php?gun=" + deviceId;
        Log.e("URL", urlSrv);

        ConnectionToServer conn = new ConnectionToServer(this);//this);
        conn.execute(urlSrv);


        //Instanciation de différents composants visuels
        instanciationXMLComponents();

        //Parametre la barre de luminosité
        setSeekbarLumin();

        //Paramètre l'action des différents boutons présent sur l'ATH
        setOnClick();



        //Verrouille l'orientation du Launcher seulement
        setOrientation();

        if (Build.VERSION.SDK_INT <26){ //API 26 est Android 8
            startService(new Intent(MainActivity.this, HUD.class));
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            if (!hasFocus) {

                Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                sendBroadcast(closeDialog);

                // Method that handles loss of window focus
                new BlockStatusBar(this,false).collapseNow();
            }
        }
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
        if (Build.VERSION.SDK_INT >= 17/*Build.VERSION_CODES.M*/) {
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
        { //Si large écran
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        } else { // Si petits écrans

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        }

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

        for (Button butt : buttonList) {

            butt.getBackground().setAlpha(255);
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

    //Paramètre l'action à effectuer lors d'un appui sur un bouton.
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


                            } else {
                                Toast.makeText(MainActivity.this, "Mauvais Mot De Passe", Toast.LENGTH_SHORT).show();
                                mBadPassword ++;

                                getSharedPreferences("badPassword", MODE_PRIVATE)
                                        .edit()
                                        .putInt("badPassword", mBadPassword)
                                        .apply();
                            }
                            if(mBadPassword == 3){
                                Toast.makeText(this, "Mauvais MDP 3X", Toast.LENGTH_SHORT).show();
                                mBlockPassword=true;
                            }

                            if(mBlockPassword){
                                AlertDialog dialogBad = new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Mauvais Mot de Passe 3fois")
                                        .setPositiveButton("OK",null)
                                        .setCancelable(false)
                                        .create();
                                dialogBad.show();
                            }

                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();

                mBadPassword = getSharedPreferences("badPassword", MODE_PRIVATE).getInt("badPassword",0);

            }

            if(mBlockPassword){

                AlertDialog dialogBlockPwd = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Veuillez attendre la fin du décompte")
                        .setPositiveButton("OK",null)
                        .setCancelable(false)
                        .create();
                dialogBlockPwd.show();

                long delay = 1000*10;

                new Handler().postDelayed(() -> {

                    dialogBlockPwd.dismiss();


                    //Toast.makeText(this, "Veuillez attendre encore", Toast.LENGTH_SHORT).show();
                    mBlockPassword = false;

                    mBadPassword=0;
                    getSharedPreferences("badPassword", MODE_PRIVATE)
                            .edit()
                            .putInt("badPassword", mBadPassword)
                            .apply();

                },delay); // 1 min
            }


        });

        //Actualise l'affichage du Launcher
        mButtonMAJ.setOnClickListener(view -> {
            /*Intent intent = new Intent(MainActivity.this, MainActivity.class);
            Bundle b = new Bundle();
            b.putString("packageName","com.example.info_jvs.launcher");
            b.putString("apkName","launcher.apk");
            intent.putExtras(b); //Put your id to your next Intent
            startActivity(intent);*/
        });

    }

    //vérifie si l'application est installée ou non
    public void testInstall(String packageName){
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
            Log.e("APP IS INSTALL",packageName+" est installé");
            String ver = pkgInfo.versionName;

            //Requete vers le serveur avec la version et le nom du package
            urlSrv += packageName + "&version="+ver;
            //urlSrv += packageName + "&version=2.2.2";
            Log.e("TEST URL", urlSrv);

            CheckVersionOnServer checkVersionOnServer = new CheckVersionOnServer(this);
            checkVersionOnServer.execute(urlSrv);
        }else{
            Log.e("APP IS INSTALL","C'est PAS installé");
            GetApkNameFromServer(packageName);
        }
    }

    //Check le version installe, si bonne version => lance l'app. Si pas télécharge la bonne version
    public void checkVersion(String result){
        String[] resultRequest = result.split("[;]");

        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(resultRequest[2]); //resultRequest[2] = nom du package

        if (resultRequest[0].equals("OK")) {
            Log.e("VERSION","Bonne version, c'est bon");
            startActivity(launchIntent);

        }else if(resultRequest[0].equals("Pas_OK")){
            Log.e("VERSION","Pas Bonne version, je désintalle");
            //on désinstall l'app
            desinstallApp(resultRequest[2]);

            Log.e("VERSION","Je réinstalle l'app");
            //On ré-install l'app
            GetApkNameFromServer(resultRequest[2]);

            Toast.makeText(this, "Pas bonne version", Toast.LENGTH_SHORT).show();
        }
    }

    public void desinstallApp(String appName){
        Toast.makeText(this, "Je désinstalle l'App", Toast.LENGTH_SHORT).show();
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

    //Demande au User l'accés aux permissions voulues
    public void askPermissions() {
        Log.e("Perms GENERAL", "Je check toutes les perms");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==0){
            if (resultCode==RESULT_CANCELED){
                askPermissionsOverlay();
            }
        }
        if(requestCode==1 && resultCode==RESULT_CANCELED){
            startProcess();
        }

    }

    //Permission d'accés aux paramétres système
    public void askPermissionsWriteSettings() {
        Log.e("Perms GENERAL", "Je Check Parametres");
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
        Log.e("Perms GENERAL", "Je check overlay");

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
        startService(new Intent(MainActivity.this,CheckRunningApp.class));
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

            @SuppressLint("Range")
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
                            progressBarDialog.dismiss();
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
        Log.e("initialiserLocalisation","Entré");

        Intent SendLocation = new Intent(MainActivity.this,PositionServiceJason.class);
        SendLocation.putExtra("deviceId",deviceId);
        startService(SendLocation);
    }

}