package com.example.test_gun;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;


public class PositionServiceJason  extends Service {

    // LocationManager locationManager;
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000 * 60 * 30; // 1 hour
    private static final float LOCATION_DISTANCE = 160935f; // 100 miles
    int count=0;

    public String BaseUrlSrv = "https://launcher.carrieresduhainaut.com/launcherdev/test_gun";
    public String urlSrv;

    public String deviceId;

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }


        public  void  sendLog(double latitude, double longitude)  throws  UnsupportedEncodingException
        {
            urlSrv = BaseUrlSrv + "/LogPositionGPSLucas.php?gun=" + deviceId+"&coordLg="+longitude+"&coordLt="+latitude;

            EnvoieGPSToServer conn = new EnvoieGPSToServer();
            conn.execute(urlSrv);
            /*class SendPostReqAsyncTask extends AsyncTask<String, Void, String>
            {

                @Override
                protected String doInBackground(String... params)
                {
                    String paramdeviceID = params[0];
                    String paramLongitude = String.valueOf(params[2]);
                    String paramLatitude = String.valueOf(params[1]);


                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(
                            "http://212.166.21.236:8080/SaveGpsLocation.php?deviceID="+ paramdeviceID +"&longitude="+paramLongitude +"&latitude="+ paramLatitude);

                    List<NameValuePair> nameValuePairList = new ArrayList<>(3);
                    nameValuePairList.add(new BasicNameValuePair("deviceID", paramdeviceID));
                    nameValuePairList.add(new BasicNameValuePair("longitude", paramLongitude));
                    nameValuePairList.add(new BasicNameValuePair("latitude", paramLatitude));
                    try {
                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList));
                        HttpResponse response = httpClient.execute(httpPost);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(String result) {
                    super.onPostExecute(result);
                }
            }

            SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
            sendPostReqAsyncTask.execute(deviceID, String.valueOf(longitude),String.valueOf(latitude));*/
        }

        @Override
        public void onLocationChanged(Location location)
        {

            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);


            double latitude=location.getLatitude();
            double longitude=location.getLongitude();
            try {
                sendLog(latitude,longitude);
            } catch (UnsupportedEncodingException e) {
                Log.e("Yourapp", "UnsupportedEncodingException");
            }

            Log.e(TAG, urlSrv);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            count=count+1;
            Date currentTime = Calendar.getInstance().getTime();
            Log.e(TAG, "onStatusChanged: "+currentTime+" ; "+ provider);
            if (count==30)
            {
                onStarting();
                count=0;
            }
        }
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        turnGPSOn();
        onStarting();
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        Bundle extras = intent.getExtras();

        deviceId = extras.get("deviceId").toString();

        return START_STICKY;
    }

    public void onStarting () {
        Log.e(TAG, "onStarting");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }


    private boolean checkLocation() {
        if(!isLocationEnabled()) {
            turnGPSOn();
        }
        return isLocationEnabled();
    }

    private boolean isLocationEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    // automatic turn on the gps
    public void turnGPSOn()
    { //mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT == 17){
            Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
            intent.putExtra("enabled", true);
            this.sendBroadcast(intent);

            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if(!provider.contains("gps")){ //if gps is disabled
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                this.sendBroadcast(poke);


            }
        }
    }
    // automatic turn off the gps
    public void turnGPSOff()
    {
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(provider.contains("gps")){ //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            this.sendBroadcast(poke);
        }
    }

}
