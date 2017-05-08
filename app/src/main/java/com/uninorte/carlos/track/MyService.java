package com.uninorte.carlos.track;

/**
 * Created by carlos on 7/05/17.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

public class MyService extends Service {
    static final int MSG_SAY_HELLO = 1;
    /**
     * Command to the service to register a client, receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client where callbacks should be sent.
     */
    static final int MSG_REGISTER_CLIENT = 1;
    /**
     * Command to the service to unregister a client, ot stop receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client as previously given with MSG_REGISTER_CLIENT.
     */
    static final int MSG_UNREGISTER_CLIENT = 2;
    /**
     * Command to service to set a new value.  This can be sent to the
     * service to supply a new value, and will be sent by the service to
     * any registered clients with the new value.
     */
    static final int MSG_SET_VALUE = 3;
    private static final String TAG = "BOOMBOOMTESTGPS";
    private static final int LOCATION_INTERVAL = 10000;
    private static final float LOCATION_DISTANCE = 0;
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    public ArrayList<String> track;
    /**
     * Keeps track of all current registered clients.
     */
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
    /**
     * Holds last value set by a client.
     */
    private int mValue = 0;
    private String data ="";
    private LocationManager mLocationManager = null;
    private String Name;

    @Override
    public IBinder onBind(Intent arg0) {
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        //myRef = database.getReference();
        Name = (String) intent.getExtras().get("data");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        Log.d(TAG, "onDestroy: "+VendorActivity.data);
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

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_VALUE:
                    mValue = msg.arg1;

                    for (int i = mClients.size() - 1; i >= 0; i--) {

                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            Double latitud = location.getLatitude();
            Double altitud = location.getAltitude();
            Double longitude = location.getLongitude();
            data = data + latitud.toString() + "*" + altitud.toString() + "*" + longitude.toString() + "|";
            for (int i = mClients.size() - 1; i >= 0; i--) {
                try {
                    //Send data as a String
                    Bundle b = new Bundle();
                    b.putString("str1", data);
                    Message msg = Message.obtain(null, MSG_SET_VALUE);
                    msg.setData(b);
                    mClients.get(i).send(msg);
                } catch (RemoteException e) {
                    // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                    mClients.remove(i);
                }
            }

        }


        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }
}
