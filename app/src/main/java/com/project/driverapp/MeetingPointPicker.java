package com.project.driverapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.FrameLayout.*;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;


import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

public class MeetingPointPicker extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {
    MapView mapView;
    MapboxMap mapboxMap;
    Button selector,back;
    PermissionsManager permissionsManager;
    ImageView hoveringMarker;
    Layer droppedMarkerLayer;
    DatabaseHelper databaseHelper = new DatabaseHelper();
    private FusedLocationProviderClient mFusedLocationClient;

    private static final String DROPPED_MARKER_LAYER_ID = "DROPPED_MARKER_LAYER_ID";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Mapbox.getInstance(MeetingPointPicker.this,getResources().getString(R.string.access_token));
        setContentView(R.layout.activity_meeting_point_picker);
        back = findViewById(R.id.BackButton);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MeetingPointPicker.this,MainActivity.class));
                finish();
            }
        });
        mapView = findViewById(R.id.meetingPoint_mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        selector = findViewById(R.id.selector);
    }
    MarkerViewManager markerViewManager;
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MeetingPointPicker.this.mapboxMap = mapboxMap;
         markerViewManager = new MarkerViewManager(mapView,mapboxMap);
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull final Style style) {
                enableLocationPlugin(style);

                hoveringMarker = new ImageView(MeetingPointPicker.this);
                hoveringMarker.setImageResource(R.drawable.mapbox_marker_icon_default);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        WRAP_CONTENT,
                        WRAP_CONTENT, Gravity.CENTER);

                hoveringMarker.setLayoutParams(params);
                mapView.addView(hoveringMarker);
                markerViewManager = new MarkerViewManager(mapView, mapboxMap);


                // Initialize, but don't show, a SymbolLayer for the marker icon which will represent a selected location.
                initDroppedMarker(style);


                selector = findViewById(R.id.selector);
                selector.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (hoveringMarker.getVisibility() == View.VISIBLE){
                            // Use the map target's coordinates to make a reverse geocoding search
                            final LatLng mapTargetLatLng = mapboxMap.getCameraPosition().target;
                            Query query = databaseHelper.firebaseFirestore.collection("meeting_points").whereEqualTo("latitude",mapTargetLatLng.getLatitude()).whereEqualTo("longitude",mapTargetLatLng.getLongitude());
                            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.getResult().isEmpty()){
                                        hoveringMarkerHide(mapTargetLatLng, style);
                                        LayoutInflater inflater = LayoutInflater.from(MeetingPointPicker.this);
                                        final View dialogview = inflater.inflate(R.layout.mapdatainputlayout, null);
                                        AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(MeetingPointPicker.this);
                                        dialogbuilder.setTitle("Add Meeting Point Data");
                                        dialogbuilder.setView(dialogview);
                                        dialogbuilder.create();
                                        dialogbuilder.setPositiveButton("Add Data", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                EditText placeName = dialogview.findViewById(R.id.inp_meetingPointName);
                                                EditText placeAddress = dialogview.findViewById(R.id.inp_meetingPointAddress);
                                                String place_name, place_address;
                                                if (!placeName.getText().toString().equals("") && !placeAddress.getText().toString().equals("")){
                                                    place_name = placeName.getText().toString();
                                                    place_address = placeAddress.getText().toString();
                                                    HashMap<String, Object> map
                                                            = new HashMap<>();
                                                    map.put("LocationName",place_name);
                                                    map.put("LocationAddress",place_address);
                                                    map.put("latitude",mapTargetLatLng.getLatitude());
                                                    map.put("longitude",mapTargetLatLng.getLongitude());
                                                    map.put("isNull",false);
                                                    databaseHelper.meeting_points.add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                                            Toast.makeText(MeetingPointPicker.this, "Meeting Point Added", Toast.LENGTH_SHORT).show();
                                                            Timber.tag("Success").d("Data Added Finally");
                                                            hoveringMarkerVisible(style);
                                                            reload();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Timber.e("Error Occurred");
                                                        }
                                                    });
                                                }else{
                                                    Toast.makeText(MeetingPointPicker.this, "Empty Field Try Again", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                hoveringMarkerVisible(style);
                                            }
                                        });
                                        dialogbuilder.show();
                                    }else{
                                        Toast.makeText(MeetingPointPicker.this, "Data already exists", Toast.LENGTH_SHORT).show();
                                        hoveringMarkerVisible(style);
                                    }
                                }
                            });
                        }
                        else{
                            hoveringMarkerVisible(style);
                        }
                    }
                });
            }
        });

        mapboxMap.addOnMapClickListener(MeetingPointPicker.this);
    }
    Location location;
    LocationComponent locationComponent;
    private void enableLocationPlugin(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(
                    this, loadedMapStyle).build());
            locationComponent.setLocationComponentEnabled(true);

            if(location == null){
                Log.d("UTAG", "enableLocationPlugin: NULL");
            }else {
                Log.d("UTAG", "enableLocationPlugin: NOT NULL");

            }
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.NORMAL);

            mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    getNearbyMeetingPoints(null,task.getResult());
                }
            });

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    private void initDroppedMarker(@NonNull Style loadedMapStyle) {
        // Add the marker image to map
        loadedMapStyle.addImage("dropped-icon-image", BitmapFactory.decodeResource(
                getResources(), R.drawable.map_marker_dark));
        loadedMapStyle.addSource(new GeoJsonSource("dropped-marker-source-id"));
        loadedMapStyle.addLayer(new SymbolLayer(DROPPED_MARKER_LAYER_ID,
                "dropped-marker-source-id").withProperties(
                iconImage("dropped-icon-image"),
                visibility(NONE),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        ));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted && mapboxMap != null) {
            Style style = mapboxMap.getStyle();
            if (style != null) {
                enableLocationPlugin(style);
            }
        } else {
            Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void updateCameraLocation(Double latitude ,Double longitude){
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude)) // Sets the new camera position
                .zoom(13) // Sets the zoom
                .build(); // Creates a CameraPosition from the builder

        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 2000);

    }

    public void getNearbyMeetingPoints(final Style style, Location location){
        Double latitude = location.getLatitude();
        final Double longitude = location.getLongitude();
        updateCameraLocation(latitude,longitude);
        final Double lessThanLatPoint = latitude  - (20.0 / 111.045);
        final Double gtThanLatPoint = latitude  + (20.0 / 111.045);
        final Double lessThanLonPoint = longitude - (20.0 / 111.045);
        final Double gtThanLonPoint = longitude + (20.0 / 111.045);
        Query q = databaseHelper.meeting_points.whereLessThanOrEqualTo("latitude",gtThanLatPoint).whereGreaterThanOrEqualTo("latitude",lessThanLatPoint);
        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.getResult().isEmpty()){
                    QuerySnapshot snapshots = task.getResult();
                    View customView;
                    MarkerView marker;
                    ImageView imageView;
                    for (final DocumentSnapshot snapshot : snapshots){
                        if ((Double)snapshot.get("longitude") <= gtThanLonPoint ||(Double) snapshot.get("longitude") >= lessThanLonPoint){
                            customView = LayoutInflater.from(MeetingPointPicker.this).inflate(
                                    R.layout.pointerview, null);
                            imageView = customView.findViewById(R.id.img_view);
                            imageView.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(MeetingPointPicker.this);
                                    dialog.setTitle("Location Details");
                                    dialog.setMessage("Location Name: "+snapshot.get("LocationName")+"\nLocation Address: "+snapshot.get("LocationAddress"));
                                    dialog.setNeutralButton("OK got it!", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    dialog.show();
                                }
                            });
                            marker = new MarkerView(new LatLng((Double)snapshot.get("latitude"),(Double)snapshot.get("longitude")),customView);
                            //Log.d("LAT",new LatLng((Double)snapshot.get("latitude"),(Double)snapshot.get("longitude")).toString());
                            markerViewManager.addMarker(marker);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Timber.tag("Error").e(e);
            }
        });
    }


    public void hoveringMarkerVisible(Style style){
        // Switch the button appearance back to select a location.
        selector.setText("Select Meeting Point");
        // Show the red hovering ImageView marker
        hoveringMarker.setVisibility(View.VISIBLE);

        // Hide the selected location SymbolLayer
        droppedMarkerLayer = style.getLayer(DROPPED_MARKER_LAYER_ID);
        if (droppedMarkerLayer != null) {
            droppedMarkerLayer.setProperties(visibility(NONE));
        }
    }

    public void hoveringMarkerHide(LatLng mapTargetLatLng, Style style){


        // Hide the hovering red hovering ImageView marker
        hoveringMarker.setVisibility(View.INVISIBLE);

        // Transform the appearance of the button to become the cancel button
        selector.setText("Cancel");

        // Show the SymbolLayer icon to represent the selected map location
        if (style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
            GeoJsonSource source = style.getSourceAs("dropped-marker-source-id");
            if (source != null) {
                source.setGeoJson(Point.fromLngLat(mapTargetLatLng.getLongitude(), mapTargetLatLng.getLatitude()));
            }
            droppedMarkerLayer = style.getLayer(DROPPED_MARKER_LAYER_ID);
            if (droppedMarkerLayer != null) {
                droppedMarkerLayer.setProperties(visibility(Property.VISIBLE));
            }
        }
    }

    public void addNearbyMeetingPoints(LocationComponent locationComponent, MarkerViewManager markerViewManager){
        Location location = locationComponent.getLastKnownLocation();
        Double latitude = location.getLatitude();
        final Double longitude = location.getLongitude();
        final Double lessThanLatPoint = latitude  - (20.0 / 111.045);
        final Double gtThanLatPoint = latitude  + (20.0 / 111.045);
        final Double lessThanLonPoint = longitude - (20.0 / 111.045);
        final Double gtThanLonPoint = longitude + (20.0 / 111.045);
        /*final List<Feature> symbolLayerIconFeatureList = new ArrayList<>();*/
        Query q = databaseHelper.meeting_points.whereLessThanOrEqualTo("latitude",gtThanLatPoint).whereGreaterThanOrEqualTo("latitude",lessThanLatPoint);
        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.getResult().isEmpty()){
                    QuerySnapshot snapshots = task.getResult();
                    for (DocumentSnapshot snapshot : snapshots){
                        if ((Double)snapshot.get("longitude") <= gtThanLonPoint ||(Double) snapshot.get("longitude") >= lessThanLonPoint){
                            /*symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat((Double)snapshot.get("longitude"),)));*/
                            //MarkerView marker = new MarkerView(new LatLng((Double)snapshot.get("longitude"),(Double)snapshot.get("latitude")));


                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Timber.tag("Error").e(e);
            }
        });
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return false;
    }

    void reload(){
        startActivity(new Intent(MeetingPointPicker.this,MeetingPointPicker.class));
        finish();
    }


}

