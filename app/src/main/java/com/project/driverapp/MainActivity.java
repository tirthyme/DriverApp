package com.project.driverapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.project.driverapp.Services.LocationService;
import com.project.driverapp.Utilities.Constants;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout signin = findViewById(R.id.reglog);
        LinearLayout m = findViewById(R.id.maplog);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            m.setVisibility(View.VISIBLE);
            Log.d(TAG, "onCreate: " + firebaseUser.getDisplayName() + firebaseUser.getEmail());
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    InstanceIdResult res= task.getResult();
                    FirebaseFirestore.getInstance().collection("user_master").document(firebaseUser.getUid()).update("FirebaseCloudMessagingID",res.getToken());
                }
            });
            startLocationService();

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel channel = new NotificationChannel(Constants.channel_id, Constants.channel_name, NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(Constants.channel_desc);
                channel.enableLights(true);
                channel.setLightColor(Color.RED);
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
            }
        }else{

            signin.setVisibility(View.VISIBLE);

        }
    }



    public void meetingPointActivityInt(View view){
        startActivity(new Intent(MainActivity.this,MeetingPointPicker.class));
    }
    public void signOut(View view){
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, MainActivity.class));
            finish();
        }
    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                MainActivity.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.project.driverapp.Services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    public void registrationActivity(View view){
        startActivity(new Intent(MainActivity.this,RegistrationActivity.class));
        finish();
    }

    public void loginActivity(View view){
        startActivity(new Intent(MainActivity.this,LoginActivity.class));
        finish();
    }

    public void viewIncomingRequests(View view){
        startActivity(new Intent(MainActivity.this,ViewUserRequestsActivity.class));
        finish();
    }

}
