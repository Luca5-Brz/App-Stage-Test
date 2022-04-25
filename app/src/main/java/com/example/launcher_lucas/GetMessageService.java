package com.example.launcher_lucas;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.WindowManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GetMessageService extends Service {

    MediaPlayer mp;

    String urlSrv;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Bundle extras = intent.getExtras();
        urlSrv = extras.get("urlSrv").toString();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.e("tagii","start message service");

        mp = MediaPlayer.create(getApplicationContext(), R.raw.alarm);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {

            SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask(this);
            sendPostReqAsyncTask.execute(urlSrv);

            //Log.e("Url Message SendLog",urlSrv);

        }, 0, 15, TimeUnit.SECONDS);
    }

    public void sendLog(String result)
    {
        String str = result.replaceFirst("^ *", "");

        if(Build.VERSION.SDK_INT<=26){
            if(str.equals("Alarm")){
                mp.setOnCompletionListener(mp1 -> {
                    // TODO Auto-generated method stub
                    mp1.reset();
                    mp1.release();
                    mp1 =null;
                });
                mp.start();

            }else if(!(str.equals("Plus de messages"))){
                AlertDialog alertMsg = new AlertDialog.Builder(getApplicationContext())
                        .setTitle("Attention !!")
                        .setMessage(str)
                        .setPositiveButton("Message Compris !",null)
                        .create();
                alertMsg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                alertMsg.show();
            }
        }else{
            if(str.equals("Alarm")){
                mp.setOnCompletionListener(MediaPlayer::start);

                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 15, 0);

                AlertDialog music = new AlertDialog.Builder(getApplicationContext())
                        .setTitle("Gun Perdu !!")
                        .setPositiveButton("Retrouvé!", (dialogInterface, i) -> {
                            mp.stop();
                            mp.reset();
                            mp.release();
                        })
                        .setCancelable(false)
                        .setMessage("Ce gun a été perdu")
                        .create();
                music.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                music.show();
                mp.start();

            }else if(!(str.equals("Plus de messages"))){
                AlertDialog alertMsg = new AlertDialog.Builder(getApplicationContext())
                        .setTitle("Attention !!")
                        .setMessage(str)
                        .setPositiveButton("Message Compris !",null)
                        .create();
                alertMsg.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                alertMsg.show();
            }

        }
    }
}
