package my.edu.utar.grp_nav;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Marker latestMarker;
    private GoogleMap mMap;
    private SearchView mapSearchView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Button confirmButton;
    private List<Address> addressList = null;
    private SharedPreferences sharedPreferences;
    public static Address address_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //mapSearchView.setQuery("", false);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_maps);

        confirmButton = findViewById(R.id.confirmButton);
        mapSearchView = findViewById(R.id.mapSearch);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        FrameLayout mapContainer = findViewById(R.id.mapContainer);
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.mapContainer, mapFragment).commit();
        mapFragment.getMapAsync(MapsActivity.this);

        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);



        String savedSearchQuery = sharedPreferences.getString("mapSearchText", "");
        if (!savedSearchQuery.isEmpty()) {
            mapSearchView.setQuery(savedSearchQuery, false);
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = mapSearchView.getQuery().toString();
                Intent intent = getIntent();
                String navigate = intent.getStringExtra("from");

                if ("createrides".equals(navigate)) {
                    Intent createridesIntent = new Intent(MapsActivity.this, CreateRides.class);
                    createridesIntent.putExtra("searchQueryKey", searchQuery);
                    startActivity(createridesIntent);
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("mapSearchText", searchQuery);
                    editor.apply();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("selectedLocation", searchQuery);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });

        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = mapSearchView.getQuery().toString().trim();
                if (!location.isEmpty()) {
                    Geocoder geoCoder = new Geocoder(MapsActivity.this);
                    try {
                        addressList = geoCoder.getFromLocationName(location, 1);
                        if (!addressList.isEmpty()) {
                            Address address = addressList.get(0);
                            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                            if (latestMarker != null) {
                                latestMarker.remove();
                            }
                            latestMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                            float zoomLevel = 15.0f;
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                            findViewById(R.id.btn_relativeLayout).setVisibility(View.VISIBLE);
                            address_name = addressList.get(0);
                        } else {
                            Toast.makeText(MapsActivity.this, "Invalid address: " + location, Toast.LENGTH_SHORT).show();
                            confirmButton.setVisibility(View.GONE);
                            findViewById(R.id.btn_relativeLayout).setVisibility(View.GONE);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "Please enter a valid address", Toast.LENGTH_SHORT).show();
                    confirmButton.setVisibility(View.GONE);
                    findViewById(R.id.btn_relativeLayout).setVisibility(View.GONE);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("Main Activity", "Loaded");
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selectedLocation", "");
        setResult(RESULT_CANCELED, resultIntent);
        finish();
    }
}
