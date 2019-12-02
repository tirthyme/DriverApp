package com.project.driverapp.Services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.project.driverapp.Utilities.CommsNotificationManager;

import java.util.Map;

public class FirebaseMessagingInstanceService extends FirebaseMessagingService {

    private static final String TAG = "MessagingInstance";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d(TAG, "onNewToken: "+s);
        sendRegistrationToServer(s);
    }

    private void sendRegistrationToServer(String s){
        FirebaseFirestore.getInstance().collection("user_master").document(FirebaseAuth.getInstance().getUid()).update("FirebaseCloudMessagingID",s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        CommsNotificationManager.getInstance(getApplicationContext()).display(title,body);

    }
}
