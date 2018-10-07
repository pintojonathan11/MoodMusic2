package com.example.jonathan.moodmusic;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.Snackbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
    private Location lastlocation;
    double latitude;
    double longitude;
    boolean paused=true;
    private FusedLocationProviderClient mfc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView temp_tv = findViewById(R.id.tv_temperature);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(temp_tv, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        mfc = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    public void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == 34) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.

            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation();
            } else {
                // Permission denied. Make sure to let the user know that permission is required and that
                //they can click on the settings link to open the app settings where they can approve permission

                showSnackbar(R.string.warn, R.string.settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Build intent that displays the App settings screen.
                        Intent intent = new Intent();
                        intent.setAction(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",
                                BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            }
        }
    }
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 34);
    }



    private void getLastLocation() {
        try {
            mfc.getLastLocation()
                    .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                lastlocation = task.getResult();

                                latitude = lastlocation.getLatitude();
                                longitude = lastlocation.getLongitude();
                            }

                        }
                    });
        }
        catch(SecurityException e){
            Log.e("going","going1");
        }
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            //Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.warn, R.string.settings,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {

            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }





    public void onClick(View v){
        if(v.getId()==R.id.go_button){
            final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
            MyBounceInterpolator myBounceInterpolator = new MyBounceInterpolator(0.2,20);
            myAnim.setInterpolator(myBounceInterpolator);
            v.startAnimation(myAnim);

            String baseURL = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&units=imperial&APPID=588a47c2ff108cc1e4dd31dc1a033a32";
            try{
                Data x = new Data();
                String[] info = x.execute(baseURL).get().split(" ");
                String temp=info[0];
                TextView tv=findViewById(R.id.tv_temperature);
                tv.setText("");
                int i=0;
                for(i=0;i<temp.indexOf('.')-1;i++){
                    tv.append(temp.charAt(i)+"\n");
                }
                tv.append(""+temp.charAt(i));
            }
            catch(Exception e){
                Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();
            }


        }
        else if(v.getId()==R.id.play_button){
            if(paused){
                ((ImageButton)v).setImageResource(R.drawable.ic_pause_black_24dp);
                paused=false;
            }
            else{
                ((ImageButton)v).setImageResource(R.drawable.ic_play_arrow_black_24dp);
                paused=true;
            }

        }
    }
}


