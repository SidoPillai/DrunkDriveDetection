package drunkdrive.siddesh.drunkdrive;


import android.app.Activity;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class Speedcalculator extends Activity implements LocationListener {
    private LocationManager locationManager;
    private String bestProvider;
    private long mlastime;
    Location location;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        bestProvider = locationManager.getBestProvider(criteria, false);
        location = locationManager.getLastKnownLocation(bestProvider);
        mlastime = location.getTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(bestProvider, 20000, 1, this);
    }

    public void onLocationChanged(Location loc) {
        // TODO Auto-generated method stub

        long timediff = loc.getTime() - mlastime;

        double speed = (loc.getLatitude() - location.getLatitude()) + (loc.getLongitude() - location.getLongitude()) / (timediff);

        Toast.makeText(this, "The Speed is:" + speed, 600000000).show();

        if (speed >= 0.1) {
            Intent intent = new Intent(this, DrunkDriveManager.class);
            this.finish();
            startActivity(intent);

        }
    }

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

}
