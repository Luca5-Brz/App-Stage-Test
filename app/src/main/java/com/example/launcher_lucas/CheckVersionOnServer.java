package com.example.launcher_lucas;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CheckVersionOnServer extends AsyncTask<String, String, String>{

    @SuppressLint("StaticFieldLeak")
    MainActivity actiTest;

    public CheckVersionOnServer(MainActivity actiTest) {
        this.actiTest = actiTest;
    }

    @Override
    protected String doInBackground(String... params) {
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

        //actiTest.resultRequestVersion = result;
        actiTest.checkVersion(result);

    }
}
