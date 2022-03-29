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


public class ConnectionToServer extends AsyncTask<String, String, String>{

    public String packagesNames;

    @SuppressLint("StaticFieldLeak")
    MainActivity actiTest;

    public ConnectionToServer(MainActivity actiTest) {
        this.actiTest = actiTest;
    }

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

        //result = ce que le serveur renvoie (les noms des packages)
        packagesNames = result;

        if (result==null){ //Le serveur ne répond pas
            Log.e("Retour Serveur","Pas de réponse du Serveur");

        }else{ //Le serveur à répondu
            Log.e("Retour Serveur",result);

            try {
                actiTest.writeToFile(result, actiTest.getApplicationContext());
                actiTest.splitString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }






}