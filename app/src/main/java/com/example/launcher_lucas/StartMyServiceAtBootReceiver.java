package com.example.launcher_lucas;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by info-jvs on 12-12-17.
 */
public class StartMyServiceAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

       Log.e(TAG, "BootCompleted 1 ! ");
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
        {

            // Start Service On Boot Start Up
            //Intent service = new Intent(context, services.class);
            //context.startService(service);

            //Start App On Boot Start Up
            Log.e(TAG, "BootCompleted ! ");
            Intent serviceIntent = new Intent(context, CheckRunningApp.class);
           // serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//cette ligne est à ajouter pour une activity , a enlever si on veut démarrer un service
            context.startService(serviceIntent);

        }
    }
}