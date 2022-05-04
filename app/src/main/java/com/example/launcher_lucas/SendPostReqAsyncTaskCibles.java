package com.example.launcher_lucas;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SendPostReqAsyncTaskCibles extends AsyncTask<String, Void, String> {

    @SuppressLint("StaticFieldLeak")
    GetMessageCiblesService actiTest;

    public SendPostReqAsyncTaskCibles(GetMessageCiblesService actiTest) {
        this.actiTest = actiTest;
    }

    @Override
    protected String doInBackground(String... params)
    {
        Log.e("Url Message Ciblés",""+params[0]);
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
        super.onPostExecute(result);

        if (result !=null)
        {
            Log.e("Return Message Ciblés",result);
            actiTest.sendLog(result);

        }
    }
}
