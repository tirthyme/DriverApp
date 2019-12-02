package com.project.driverapp;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class DatabaseHelper {

    FirebaseFirestore firebaseFirestore;
    CollectionReference meeting_points;
    DatabaseHelper(){
        firebaseFirestore = FirebaseFirestore.getInstance();
        meeting_points =  firebaseFirestore.collection("meeting_points");
    }
/*
    HashMap<String,String> doesMeetingPointExists(LatLng latLng){
        String lat = String.valueOf(latLng.getLatitude());
        String longi = String.valueOf(latLng.getLongitude());
        Query q = firebaseFirestore.collection("meeting_points").whereEqualTo("latitude",lat).whereEqualTo("longitude",longi).limit(1);
        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                HashMap<String,String> jsonObject = new HashMap<>();
                if (task.getResult().isEmpty()){
                    jsonObject.put("isNull", "true");
                    Log.d("TAG","DATA"+jsonObject.toString());
                }
                else{
                    Log.d("TAG", "IN ON Fail");
                    QuerySnapshot querySnapshot = task.getResult();
                    for (QueryDocumentSnapshot documentSnapshot: querySnapshot){
                        Map<String,Object> map = documentSnapshot.getData();
                        jsonObject.put("isNull","false");
                        jsonObject.put("LocationName", String.valueOf(map.get("place_name")));
                        jsonObject.put("LocationAddress", String.valueOf(map.get("place_address")));
                        Log.d("TAG","DATA"+jsonObject.toString());
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "IN ON Fail");
                jsonObject.put("isNull", "true");
                Timber.e("Errora"+"---"+e.getMessage());
            }
        });
        Log.d("TAG",jsonObject.toString());
        return jsonObject;
    }*/
    class bool_Data{
        boolean b;
        void setValue(boolean b){
            this.b = b;
        }
        boolean getvalue(){
            return b;
        }
    }
    boolean addDatatoDatabase(Map<String,Object> data, String collection_name) {
        {
            final bool_Data b = new bool_Data();
            if (collection_name.equals("meeting_points")){
                 meeting_points.add(data).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                     @Override
                     public void onComplete(@NonNull Task<DocumentReference> task) {
                         Timber.e("Data Added Finally");
                         b.setValue(true);
                     }
                 }).addOnFailureListener(new OnFailureListener() {
                     @Override
                     public void onFailure(@NonNull Exception e) {
                         Timber.e("Error Occurred");
                         b.setValue(false);
                     }
                 });
            }
            return b.getvalue();
        }
    }

}
