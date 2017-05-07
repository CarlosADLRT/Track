package com.uninorte.carlos.track;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;


/**
 * Created by carlos on 7/05/17.
 */
public class MyTestService extends IntentService {
    final String TAG = "Service_TAG";
    LocationListener callStateListener;
    private LocationManager mManager;
    private NotificationManager mNotificationManager;

    // Must create a default constructor
    public MyTestService() {
        // Used to name the worker thread, important only for debugging.
        super("test-service");
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
        mManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        callStateListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Aqui guardo en un array las posiciones cada vez que cambia
                Double latitud = location.getLatitude();
                Double altitud = location.getAltitude();
                Double longitude = location.getLongitude();
                Tracked.addElemento(latitud.toString() + "-" + altitud.toString() + "+" + longitude.toString());
                Log.d(TAG, "onLocationChanged: " + Tracked.getTrack().get(Tracked.getTrack().size() - 1));
            }


            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    }


}
