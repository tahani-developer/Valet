package com.example.valet;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private NavigationView nDrawerLayout;
    private ActionBarDrawerToggle nToggle;
    int isAvilable = 0;
    String barcodeValue;

    ProgressDialog progressDialog;


    private GoogleMap mMap;
    Button scan;
    public static List<LatLng> LatLngListMarker;
    private LatLngBounds.Builder builder;
    LatLngBounds bounds;
    boolean flag = false;
    Timer timer;
    double v1 = 31.969570, v2 = 35.914191;
    double a1 = 31.968420, a2 = 35.916258;


    private Map<Marker, Map<String, Object>> markers = new HashMap<>();
    private Map<String, Object> dataModel = new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scan = findViewById(R.id.scan);
        nDrawerLayout = findViewById(R.id.nav_view);
        Spinner loc = findViewById(R.id.loc);
        ImageButton map = findViewById(R.id.mapView);
        TextView avi = findViewById(R.id.available);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initialization();
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanDialog();
            }
        });

        List<String> gradeList = new ArrayList<>();
        gradeList.add("");
        gradeList.add("Tag Mall");
        gradeList.add("Pollyvard");
        gradeList.add("Work");

        ArrayAdapter gradeAdapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, gradeList);
        gradeAdapter.setDropDownViewResource(R.layout.spinner_drop_down_layout);
        loc.setAdapter(gradeAdapter);
        loc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                isAvilable = 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


//        MaterialToolbar mTopToolbar = (MaterialToolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(mTopToolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer,
                R.string.common_open_on_phone,
                R.string.common_open_on_phone);

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView)
                findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);


    }

    private void initialization() {

        LatLngListMarker = new ArrayList<>();

        LatLng latLng = new LatLng(v1, v2);

        LatLng latLng2 = new LatLng(a1, a2);
        LatLngListMarker.add(latLng);
        LatLngListMarker.add(latLng2);
        builder = new LatLngBounds.Builder();

        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (flag) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            // globelFunction.getSalesManInfo(SalesmanMapsActivity.this,2);
                            v1 = v1 - 0.000010;
                            v2 = v2 - 0.000010;
                            a1 = 31.968420;
                            a2 = 35.916258;
                            Log.e("Location", "loc" + v1 + "  " + v2 + "   " + a1 + "   " + a2);
                            LatLng latLng = new LatLng(v1, v2);
                            LatLng latLng2 = new LatLng(a1, a2);
                            LatLngListMarker.clear();
                            LatLngListMarker.add(latLng);
                            LatLngListMarker.add(latLng2);
                            location(1);

                        }
                    });

                }
//
            }

        }, 0, 1000);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        location(0);


    }

    public void location(int move) {

        try {

            if (move == 1) {

                mMap.clear();
            }
        } catch (Exception e) {
            Log.e("Problem", "problennnn" + e.getMessage());
        }

        // Add a marker in Sydney and move the camera
        Log.e("mmmmmm", "locationCall");
        LatLng sydney = null;
        for (int i = 0; i < LatLngListMarker.size(); i++) {

            //if (!salesManInfosList.get(i).getLatitudeLocation().equals("0") && !salesManInfosList.get(i).getLongitudeLocation().equals("0")) {
            sydney = LatLngListMarker.get(i);

//            MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconSize())).position(sydney).title("aaa");
//            mMap.addMarker(marker);

            Marker myMarker;

            if (i == 0) {
                myMarker = mMap.addMarker(new MarkerOptions()
                        .position(sydney)
                        .title("My Location")
                        .icon(BitmapDescriptorFactory.fromBitmap(iconSize())));
            } else {
                myMarker = mMap.addMarker(new MarkerOptions()
                        .position(sydney)
                        .title("Tag Mall")
                        //.snippet("This is my spot!")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            }

            mMap.setOnMarkerClickListener(this);

            builder.include(sydney);


//            mMap.setOnMarkerClickListener(MainActivity.this);
//
//            MarkerOptions marker = new MarkerOptions()
//                    .icon(BitmapDescriptorFactory.fromBitmap(iconSize()))
//                    .position(sydney)
//                    .title("aaa");
//
//            builder.include(sydney);


        }
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        mMap.animateCamera(CameraUpdateFactory.newLatLng(sydney));
        if (move == 0) {
            try {
                bounds = builder.build();
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
                mMap.animateCamera(cu);
            } catch (Exception e) {

            }
        }
        flag = true;
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 0));
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {


        Log.e("******", marker.getTitle());

        if (!marker.getTitle().equals("My Location"))
            reqService();

        return false;
    }

    Bitmap iconSize() {
        int height = 50;
        int width = 100;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.car);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        return smallMarker;
    }

    void reqService() {

        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.conferm_req);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme; //style id


        Button req = dialog.findViewById(R.id.request);

        req.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog = new ProgressDialog(MainActivity.this, R.style.MyAlertDialogStyle);
                progressDialog.setMessage("Waiting for valet...");
                progressDialog.show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();

                        Dialog dialog2 = new Dialog(MainActivity.this);
                        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog2.setContentView(R.layout.valet_dialog);
                        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog2.getWindow().getAttributes().windowAnimations = R.style.DialogTheme; //style id


                        Button req = dialog2.findViewById(R.id.request);

                        req.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                dialog2.dismiss();

                                initialization();

                                scan.setVisibility(View.VISIBLE);

/*


 */

                            }
                        });
                        dialog2.show();
                    }
                }, 6000);


                dialog.dismiss();
            }
        });
        dialog.show();

    }

    void scanDialog(){

        Dialog dialog3 = new Dialog(MainActivity.this);
        dialog3.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog3.setContentView(R.layout.waiting_dialog);
        dialog3.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog3.getWindow().getAttributes().windowAnimations = R.style.DialogTheme; //style id


        Button scan = dialog3.findViewById(R.id.scan);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                IntentIntegrator intentIntegrator = new IntentIntegrator(MainActivity.this);
                intentIntegrator.setDesiredBarcodeFormats(intentIntegrator.ALL_CODE_TYPES);
                intentIntegrator.setBeepEnabled(true);
                intentIntegrator.setCameraId(0);
                intentIntegrator.setOrientationLocked(true);
                intentIntegrator.setPrompt("SCAN");
                intentIntegrator.setBarcodeImageEnabled(false);
                intentIntegrator.initiateScan();

                dialog3.dismiss();
            }
        });
        dialog3.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult Result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (Result != null) {
            if (Result.getContents() == null) {
                Log.d("MainActivity", "cancelled scan");
                Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show();
                barcodeValue = "cancelled";


            } else {
                Log.d("MainActivity", "Scanned");
                Toast.makeText(this, "Scanned ! ", Toast.LENGTH_SHORT).show();

                barcodeValue = Result.getContents();

                scan.setVisibility(View.GONE);

                Intent intent = new Intent(MainActivity.this, ParkingInfo.class);
                startActivity(intent);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == android.R.id.home) {
//            finish();
//        }

        if (nToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.item1) {
            // Intent i = new Intent(MainActivity.this, MainScreen.class);
            //startActivity(i);
            Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.profile);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


            dialog.show();
        }

        if (id == R.id.item2) {
            //Intent i = new Intent(DetailActivity.this, Saved.class);
            //startActivity(i);
        }


        // nDrawerLayout.closeDrawers();
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DrawerLayout drawer =
                (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}