package com.example.launcher_lucas;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by info-jvs on 20-02-18.
 */

public class GetMessageService extends Service {

    String deviceId;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Bundle extras = intent.getExtras();
        deviceId = extras.get("deviceId").toString();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("tagii","start message service");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try
            {
                sendLog(deviceId);

            } catch (UnsupportedEncodingException e) {
                Log.e("Launcher", "UnsupportedEncodingException");
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    public  void  sendLog(String deviceId)  throws  UnsupportedEncodingException
    {
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params)
            {
                BufferedReader buffReader;
                HttpURLConnection connexion;

                try {
                    URL url = new URL(params[0]);
                    connexion = (HttpURLConnection) url.openConnection();
                    connexion.setConnectTimeout(20000);
                    connexion.connect();

                    InputStream inStream = connexion.getInputStream();

                    buffReader = new BufferedReader(new InputStreamReader(inStream));

                    StringBuffer stringBld = new StringBuffer();
                    String inputLine;
                    while(( inputLine = buffReader.readLine())!=null){
                        stringBld.append(inputLine);
                    }
                    buffReader.close();
                    inStream.close();
                    return stringBld.toString();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {

                if (result !=null)
                {
                    String str = result.replaceFirst("^ *", "");

                    AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext())
                            .setTitle("Attention !!")
                            .setMessage(str)
                            .setPositiveButton("Message compris !", null)
                            .create();

                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alertDialog.show();
                }
                else
                {

                }

                super.onPostExecute(result);
            }
        }

        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(deviceId);
    }
}
