package com.example.launcher_lucas;


import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class CheckRunningApp extends Service {

    public String[] partsOfJsonString = {"", "", "", "", "", "", "", "", "","", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        //Log.e("CHECK APP","Check running app lancé");
        splitIt();

// This schedule a runnable task every 1 second
        scheduleTaskExecutor.scheduleAtFixedRate(() -> {
            //Log.e("SCHEDULE MACHIN","lancé");
            int numberOfTasks = 1;
            ActivityManager m = (ActivityManager)getApplicationContext().getSystemService(ACTIVITY_SERVICE);
            //Log.e("ACTIVITY Manager", ""+m);
//Get some number of running tasks and grab the first one.  getRunningTasks returns newest to oldest
            ActivityManager.RunningTaskInfo task = m.getRunningTasks(numberOfTasks).get(0);
            //Log.e("ACTIVITY",""+task);

            List<ActivityManager.RunningTaskInfo> taskInfo = m.getRunningTasks(1);
            //Log.e("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            componentInfo.getPackageName();
//Build output
            String output  = task.baseActivity.toShortString();
            //Log.e("OUTPUT",output);
            String[] output2 =output.split("[{/]");

            //Log.e("TAG", output2[1] + "--> applicatione !");

            boolean isallowed=isAllowed(output2[1]);

            if ("com.computerland.cdh.mobile".equals(output2[1])) {}
            else if ("com.example.test_gun".equals(output2[1])) { }
            else if ("com.android.settings".equals(output2[1])) { }
            else if ("com.teamviewer.quicksupport.market".equals(output2[1])){}
            else if ("com.android.packageinstaller".equals(output2[1])){}
            else if ("com.google.android.location".equals(output2[1])){}
            else if (isallowed) {}
            else
            {
                Log.e("TAG", output2[1] + "--> Refusé !");
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.test_gun");
                startActivity(launchIntent);
            }
        }, 0, 1, TimeUnit.SECONDS);
        //Log.e("TESTTTTT","Juste apres le schedule machin");
    }

    @Override
    public void onDestroy() {
        //Toast.makeText(getBaseContext(),"onDestroy", Toast.LENGTH_LONG).show();
        //Log.e("TAG2", "onDestroy runningactivity");
        scheduleTaskExecutor.shutdown();
    }


    ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
    public void splitIt() {
        //Log.e("CHECK RUNNING","splitIt lancé");
        String reader = readFromFile(getApplicationContext());
        String[] readerSplit = reader.split("[,;]");
        int i = 0;
        for (String s : readerSplit) {
            if (i == 0) {
                s = s.replaceAll("^\\s+", "");
            }
            partsOfJsonString[i] = s;
            i++;
        }
    }

    private String readFromFile(Context context) {
        //Log.e("CHECK RUNNING","readFromFile lancé");
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("buttonConfig.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
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
            Log.e("login activity", "Can not read file: ");
            e.printStackTrace();
        }

        return ret;
    }

    public boolean isAllowed(String appName) {
        //Log.e("CHECK RUNNING","isAllowed lancé");
        //parcourir l'arraylist à la recherche d'occurence et retourne vrai ou faux

        //utilisé par le service de vérification des taches en cours (checkRunningActivity class)
        boolean Occurence=false;
        for (int i = 0 ; i < partsOfJsonString.length ; i++) {
            String appInArray = partsOfJsonString[i];
            if (appName.equals(appInArray)) {
                Occurence = true;
            }
        }
        return Occurence;
    }
}