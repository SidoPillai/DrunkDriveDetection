package drunkdrive.siddesh.drunkdrive;


import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class DrunkDriveManager extends Activity implements Runnable, OnClickListener {
    public ShakeListener mShaker;
    SharedPreferences sp;
    DrunkDriveDatabase myDatabase;
    String message, pass;
    String threshold;
    int delay;
    TextView tv;
    public static ArrayList<String> number_list;
    String incoming_number, incoming_message, password;
    IntentFilter filter;
    Thread thread;
    boolean drive = true;
    AlertDialog dialog;
    String phone_number;
    public static TelephonyManager teleMgr;
    AudioManager audiomanager;
    LocationManager Lmanager;
    double latitude, longitude;
    boolean use_gps = false;
    LocationUpdate up;
    GPSTracker gps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audiomanager = (AudioManager) DrunkDriveManager.this.getSystemService(Context.AUDIO_SERVICE);
        up = new LocationUpdate(this);

        gps = new GPSTracker(DrunkDriveManager.this);
        // check if GPS enabled
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            // \n is for new line
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
        final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mShaker = new ShakeListener(this);
        mShaker.setOnShakeListener(new ShakeListener.OnShakeListener() {
            public void onShake() {
                mShaker.pause();
                drive = false;
                filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
                DrunkDriveManager.this.registerReceiver(mReceiver, filter);
                vibe.vibrate(2000);
                thread = new Thread(DrunkDriveManager.this);
                thread.start();
                //ShowDialog();
            }
        });
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        String data = sp.getString("threshold", null);
        if (data == null) {
            Intent it = new Intent(this, DrunkDriveSettings.class);
            startActivity(it);
        }


    }
  
  /*public void ShowDialog()
  {
	 AlertDialog.Builder build= new AlertDialog.Builder(this);
	 build.setPositiveButton("Yes",this);
	 build.setNegativeButton("No",this);
	 build.setMessage("DrunkDrive Message");
	 build.setMessage("Is It A Fall");
	 dialog=build.create();
	 dialog.show();
  }*/

    public void run() {
        String number;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (use_gps) {
            message = message + " ...Location " + "Latitude:" + up.latitude + "Logitude:" + up.longitude;
        }

        if (!drive) {
            SmsManager smanager = SmsManager.getDefault();
            Iterator<String> itr = number_list.iterator();
            while (itr.hasNext()) {
                number = itr.next();
                smanager.sendTextMessage(number, null, message, null, null);
            }
        }

    }

    public void onStart() {
        super.onStart();

        setContentView(R.layout.main);
        tv = (TextView) findViewById(R.id.msg);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        threshold = sp.getString("threshold", "350");
        ShakeListener.FORCE_THRESHOLD = Integer.parseInt(threshold);
        message = sp.getString("message", "Drunk Driving Detection");
        pass = sp.getString("pass", "DrunkDrive");
        delay = Integer.parseInt(sp.getString("delay", "15000"));
        use_gps = sp.getBoolean("gps", false);
        message = message + " Please reply for this Message with the keyword " + pass;
        tv.setText("DrunkDrive Message:\n\n" + message);
        number_list = new ArrayList<String>();
        DrunkDriveDatabase dat = new DrunkDriveDatabase(this);
        SQLiteDatabase dt = dat.getReadableDatabase();
        Cursor c = dt.query(DrunkDriveDatabase.TABLE_NAME, new String[]{DrunkDriveDatabase.COLUMN_NUMBER}, null, null, null, null, null);
        while (c.moveToNext()) {
            number_list.add(c.getString(0));
        }

    }


    public class MyThread extends Thread {
        public void run() {
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            audiomanager.setSpeakerphoneOn(true);

        }

    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String num;
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            android.telephony.gsm.SmsMessage[] msgs = null;
            String str = "";
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new android.telephony.gsm.SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    msgs[i] = android.telephony.gsm.SmsMessage.createFromPdu((byte[]) pdus[i]);
                    incoming_number = msgs[i].getOriginatingAddress();
                    incoming_message = msgs[i].getMessageBody().toString();
                    Iterator<String> itr = number_list.iterator();
                    while (itr.hasNext()) {
                        num = itr.next();
                        if (incoming_number.contains(num)) {
                            if (incoming_message.contains(pass)) {
                                new MyThread().start();
                                Intent ic = new Intent(Intent.ACTION_CALL);
                                ic.setData(Uri.parse("tel:" + incoming_number));
                                startActivity(ic);
                                break;
                            }
                        }
                    }
                }

                //Toast.makeText(context,incoming_number, Toast.LENGTH_SHORT).show();
            }


        }
    };


    public boolean onCreateOptionsMenu(Menu m) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, m);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem itm) {
        if (itm.getItemId() == R.id.isettings) {
            Intent i = new Intent(this, DrunkDriveSettings.class);
            startActivity(i);

        } else if (itm.getItemId() == R.id.iopencontacts) {
            Intent i = new Intent(this, DrunkDriveShowContacts.class);
            startActivity(i);

        }

        return true;
    }


    public void onClick(DialogInterface dialog, int which) {
        // TODO Auto-generated method stub
        if (which == DialogInterface.BUTTON_POSITIVE) {
            drive = false;

        } else {
            drive = true;
        }

    }


}

