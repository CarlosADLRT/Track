package com.uninorte.carlos.track;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class VendorActivity extends AppCompatActivity implements LocationListener {
    public MyTestReceiver receiverForTest;
    Notification notification;
    int mNotificationId = 001;
    int REQUEST_CODE = 1;
    TelephonyManager myTelephonyManager;
    PhoneStateListener callStateListener;
    ArrayList<String> track =new ArrayList<String>();
    //Variables
    private LocationManager mManager;
    private NotificationManager mNotificationManager;
    private boolean enableConnection;
    private boolean enableGPS;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor);
        mManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();



        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }

        myTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        callStateListener = new PhoneStateListener() {
            public void onDataConnectionStateChanged(int state) {
                String stateString;
                switch (state) {
                    case TelephonyManager.DATA_CONNECTED:
                        enableConnection = true;
                        break;
                    case TelephonyManager.DATA_DISCONNECTED:
                        Log.i("State: ", "Offline");
                        stateString = "Offline";
                        enableConnection = false;
                        Toast.makeText(getApplicationContext(),
                                stateString, Toast.LENGTH_LONG).show();
                        break;
                    case TelephonyManager.DATA_SUSPENDED:
                        Log.i("State: ", "IDLE");
                        stateString = "Idle";
                        Toast.makeText(getApplicationContext(),
                                stateString, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
        myTelephonyManager.listen(callStateListener,
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);

        //setupServiceReceiver();

    }

    // Call `launchTestService()` in the activity
    // to startup the service
    public void launchTestService() {
        // Construct our Intent specifying the Service
        Intent i = new Intent(this, MyTestService.class);
        // Add extras to the bundle
        i.putExtra("foo", "bar");
        // Start the service
        startService(i);
    }

    // Setup the callback for when data is received from the service
    public void setupServiceReceiver() {
        receiverForTest = new MyTestReceiver(new Handler());
        // This is where we specify what happens when data is received from the service
        receiverForTest.setReceiver(new MyTestReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == RESULT_OK) {
                    String resultValue = resultData.getString("resultValue");
                    Toast.makeText(VendorActivity.this, resultValue, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(mNotificationId);

    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GPS State");
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            public void onClick(final DialogInterface dialog, final int id)
            {
                launchGPSOptions();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            public void onClick(final DialogInterface dialog, final int id)
            {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    private void launchGPSOptions()
    {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void stopGps() {
        mNotificationManager.cancel(mNotificationId);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mManager.removeUpdates(this);
        String finaltrack = "";
        for (String position : track
                ) {
            finaltrack = finaltrack + position + "\n";
            //aa
            //Obtengo en un string grande todo lo que quedo registrado en el array y lo concateno
        }
        //Aquí se lo asigno a cada man en la base de datos
        String Name = getIntent().getStringExtra("name").toString();
        String Key = myRef.push().getKey();
        myRef.child(Name).child(Key).setValue(finaltrack);
    }
    private void showNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Track Start")
                        .setOngoing(true)
                        .setContentText("The tracking is running");
        // Creates an explicit intent for an Activity in your app
                Intent resultIntent = new Intent(this, VendorActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(VendorActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
                 mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
                mNotificationManager.notify(mNotificationId,mBuilder.build());
    }
    public void startGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;

        }
        boolean enabled = mManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(enabled){
                showNotification();

                mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                launchTestService();
            } else {

            }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startGPS();
                }
                else {
                }
                return;

        }


    }
    @Override
    public void onLocationChanged(Location location) {
        //Aqui guardo en un array las posiciones cada vez que cambia
        Double latitud = location.getLatitude();
        Double altitud = location.getAltitude();
        Double longitude = location.getLongitude();
        track.add(latitud.toString()+"-"+altitud.toString()+"+"+longitude.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getApplicationContext(), "GPS conectado", Toast.LENGTH_LONG).show();
        enableGPS = true;
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getApplicationContext(),"GPS desconectado",Toast.LENGTH_LONG).show();
        enableGPS = false;
    }

    public void OnClick(View view) {
        //Evento del click dependiendo de que button haga click empieza o detiene
        switch (view.getId())
        {
            case R.id.activity_vendor_play:
                startGPS();
                break;
            case R.id.activity_vendor_stop:
                stopGps();
                break;
        }
    }
}
