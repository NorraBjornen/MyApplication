package com.kostanay.alex.pegasdriver.Controller.MapScreen;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen.AuthorizationActivity;
import com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen.AuthorizationFragment;
import com.kostanay.alex.pegasdriver.Controller.DistrictScreen.DistrictActivity;
import com.kostanay.alex.pegasdriver.Controller.Find;
import com.kostanay.alex.pegasdriver.Controller.ListScreen.ListActivity;
import com.kostanay.alex.pegasdriver.Model.NetWork;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends SupportMapFragment {
    private static int MapZoom = 17;
    private GoogleMap GoogleMap;
    public static boolean firstConnection;
    private Marker marker;
    public static Handler handler;
    public static boolean active;

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(com.kostanay.alex.pegasdriver.R.menu.menu_map, menu);
    }

    public void onResume(){
        super.onResume();
        active = true;
    }

    public void onPause(){
        super.onPause();
        active = false;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                GoogleMap = googleMap;
                LatLng myPoint = new LatLng(53.220735, 63.629149);

                String geo = getGeo();
                if(geo != null){
                    myPoint = new LatLng(Double.parseDouble(geo.split("\\,")[0]), Double.parseDouble(geo.split("\\,")[1]));
                }

                GoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPoint, MapZoom));
                marker = GoogleMap.addMarker(new MarkerOptions().position(myPoint));
                GoogleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    public void onCameraMove() {
                        LatLng centerOfMap = GoogleMap.getCameraPosition().target;
                        marker.setPosition(centerOfMap);
                    }
                });
            }
        });
        handler = new Handler() {
            public void handleMessage(Message msg) {
                try {
                    String geo = (String) msg.obj;
                    if(geo == "NULL") geo = getGeo();
                    if(geo == null) geo = "53.220735,63.629149";
                    LatLng myPoint = new LatLng(Double.parseDouble(geo.split(",")[0]), Double.parseDouble(geo.split(",")[1]));
                    GoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPoint, MapZoom));
                    marker.setPosition(myPoint);
                } catch (Exception e){
                    if(MapFragment.active)
                    Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                }
            }
        };


        if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        String geo;
        switch (item.getItemId()){
            case com.kostanay.alex.pegasdriver.R.id.menu_item_done:
                geo = String.valueOf(marker.getPosition().latitude) + "," + String.valueOf(marker.getPosition().longitude);
                final String finalGeo = geo;
                new Thread(new Runnable() {
                    public void run() {
                        String message;
                        if(firstConnection) {
                            //message = "[online~" + arg_status + "$" + finalGeo + "]";
                            message = "[geo~" + AuthorizationFragment.DriverId + "|" + finalGeo + "~" + AuthorizationFragment.DriverId + "]";
                            firstConnection = false;
                        } else {
                            message = "[geo~" + AuthorizationFragment.DriverId + "|" + finalGeo + "~" + AuthorizationFragment.DriverId + "]";
                        }
                        NetWork.get().send(message);
                        Intent Intent = new Intent(getActivity(), ListActivity.class);
                        startActivity(Intent);
                    }
                }).start();
                return true;
            case com.kostanay.alex.pegasdriver.R.id.menu_item_location:
                Intent intent = new Intent(getActivity(), DistrictActivity.class);
                startActivityForResult(intent, 1);
                return true;
            case com.kostanay.alex.pegasdriver.R.id.menu_item_auto:
                geo = getGeo();
                if(geo != null) {
                    marker.setPosition(new LatLng(Double.parseDouble(geo.split("\\,")[0]), Double.parseDouble(geo.split("\\,")[1])));
                    GoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), MapZoom));
                } else {
                    Toast.makeText(getActivity(), "Не удалось определить координаты по GPS", Toast.LENGTH_SHORT).show();
                }
                return true;
            case com.kostanay.alex.pegasdriver.R.id.menu_item_find:
                FragmentManager manager = getFragmentManager();
                Find dialog = new Find();
                dialog.setTargetFragment(MapFragment.this, 0);
                dialog.show(manager, "Find");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        String geo = data.getStringExtra("geo");
        if(geo != null) {
            marker.setPosition(new LatLng(Double.parseDouble(geo.split("\\,")[0]), Double.parseDouble(geo.split("\\,")[1])));
            GoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), MapZoom));
        }
    }

    private String getGeo(){
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            return null;
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            } else {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Location location1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                if (location != null) {
                    String latitude = String.valueOf(location.getLatitude());
                    String longitude = String.valueOf(location.getLongitude());
                    return latitude + "," + longitude;
                }else if (location1 != null) {
                    String latitude = String.valueOf(location1.getLatitude());
                    String longitude = String.valueOf(location1.getLongitude());
                    return latitude + "," + longitude;
                }else if (location2 != null) {
                    String latitude = String.valueOf(location2.getLatitude());
                    String longitude = String.valueOf(location2.getLongitude());
                    return latitude + "," + longitude;
                }else{
                    return null;
                }
            }
        } else {
            return null;
        }
    }

}
