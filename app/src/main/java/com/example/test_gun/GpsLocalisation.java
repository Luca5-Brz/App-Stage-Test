package com.example.test_gun;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class GpsLocalisation extends MainActivity implements LocationListener
{

    public void onLocationChanged(Location localisation)
    {
        Log.d("GPS", "localisation : " + localisation.toString());
        String coordonnees = String.format("Latitude : %f - Longitude : %f\n", localisation.getLatitude(), localisation.getLongitude());
        Log.d("GPS", "coordonnees : " + coordonnees);

    }

    @Override
    public void onStatusChanged(String fournisseur, int status, Bundle extras)
    {
    }

    @Override
    public void onProviderEnabled(String fournisseur)
    {
    }

    @Override
    public void onProviderDisabled(String fournisseur)
    {
    }
}
