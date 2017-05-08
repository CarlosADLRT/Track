package com.uninorte.carlos.track;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class VendorActivity extends AppCompatActivity implements LocationListener {

    public static String data;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    Notification notification;
    int mNotificationId = 001;
    int REQUEST_CODE = 1;
    TelephonyManager myTelephonyManager;
    PhoneStateListener callStateListener;
    ArrayList<String> track =new ArrayList<String>();
    /**
     * Messenger for communicating with the service.
     */
    Messenger mService = null;
    /**
     * Flag indicating whether we have called bind on the service.
     */
    boolean mBound;
    private MyService myService;
    //Variables
    private LocationManager mManager;
    private NotificationManager mNotificationManager;
    private boolean enableConnection;
    private boolean enableGPS;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private Toolbar toolbar;
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        MyService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);

                // Give it some value as an example.
                msg = Message.obtain(null,
                        MyService.MSG_SET_VALUE, this.hashCode(), 0);
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

            // As part of the sample, tell the user what happened.
            Toast.makeText(VendorActivity.this, "Conectado",
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            // As part of the sample, tell the user what happened.
            Toast.makeText(VendorActivity.this, "Desconectado",
                    Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor);
        toolbar = (Toolbar) findViewById(R.id.activity_vendor_toolbar);
        String nombre = getIntent().getStringExtra("name");
        toolbar.setTitle(nombre);
        setSupportActionBar(toolbar);
        mManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(nombre);
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
        //Detengo el servicio
        Intent ir = new Intent(this, MyService.class);

        stopService(ir);

        unbindService(mConnection);

        //Se crea un nuevo elemento en la db del vnedeor
        database = FirebaseDatabase.getInstance();
        String nombre = getIntent().getStringExtra("name");
        myRef = database.getReference(nombre);
        String key = myRef.push().getKey();           //this returns the unique key generated by firebase
        myRef.child(key).child("Track").setValue(data);



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
                Intent ir = new Intent(this, MyService.class);

                String Name = getIntent().getStringExtra("name").toString();

                ir.putExtra("data", Name);
                // Bind to the service
                bindService(new Intent(this, MyService.class), mConnection,
                        Context.BIND_AUTO_CREATE);                //mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            } else {

            }

    }

    public void sayHello(View v) {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, MyService.MSG_SAY_HELLO, 0, 0);
        try {
            mService.send(msg);

        } catch (RemoteException e) {
            e.printStackTrace();
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

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MyService.MSG_SET_VALUE:
                    String str1 = msg.getData().getString("str1");

                    data = str1;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
