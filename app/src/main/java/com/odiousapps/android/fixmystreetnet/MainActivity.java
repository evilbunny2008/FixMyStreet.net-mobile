package com.odiousapps.android.fixmystreetnet;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback
{
	private final int permsRequestCode = 200;
	private GoogleMap mMap;
	private TextView lat, lng;
	private final DecimalFormat df = new DecimalFormat("#.######");
	private Common common;
	private LocationRequest mLocationRequest;
	private FusedLocationProviderClient mFusedLocationClient;
	private Location mCurrentLocation;
	private LocationCallback mLocationCallback;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		common = new Common(this);

		lat = findViewById(R.id.lat);
		lng = findViewById(R.id.lng);

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		assert mapFragment != null;
		mapFragment.getMapAsync(this);

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
			ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]
			{
					Manifest.permission.ACCESS_FINE_LOCATION,
					Manifest.permission.ACCESS_COARSE_LOCATION
			}, permsRequestCode);
		} else {
			doMore2();
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		updateButtons();
	}

	private void updateButtons()
	{
		LinearLayout ll = findViewById(R.id.signupin);
		LinearLayout ll2 = findViewById(R.id.report);

		Common.LogMessage("lat == " + lat.getText().toString());
		Common.LogMessage("lng == " + lng.getText().toString());

		if(lat.getText().toString().equals(getString(R.string.loading)) ||
				lng.getText().toString().equals(getString(R.string.loading)))
			return;

		if (common.GetStringPref("lastauth", "0").equals("1"))
		{
			ll.setVisibility(View.GONE);
			ll2.setVisibility(View.VISIBLE);
		} else {
			ll2.setVisibility(View.GONE);
			ll.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		boolean hasPermission = false;
		if (requestCode == permsRequestCode && grantResults.length >= 2
				&& grantResults[0] == PackageManager.PERMISSION_GRANTED
				&& grantResults[1] == PackageManager.PERMISSION_GRANTED)
			hasPermission = true;

		if (hasPermission)
		{
			doMore2();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("You have rejected 1 or more permissions critical to using this app, if you clicked deny you" +
					"may have to go into settings to allow").setCancelable(false)
					.setPositiveButton("Ok", (dialoginterface, i) -> finish());
			builder.create().show();
		}
	}

	void doMore()
	{
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
			SettingsClient mSettingsClient = LocationServices.getSettingsClient(this);

			mLocationCallback = new LocationCallback()
			{
				@Override
				public void onLocationResult(LocationResult result)
				{
					super.onLocationResult(result);
					//mCurrentLocation = locationResult.getLastLocation();
					mCurrentLocation = result.getLocations().get(0);

					if(mCurrentLocation!=null)
					{
						Common.LogMessage(mCurrentLocation.getLatitude() + ". " + mCurrentLocation.getLongitude());
					}

					mFusedLocationClient.removeLocationUpdates(mLocationCallback);
				}

				//Locatio nMeaning that all relevant information is available
				@Override
				public void onLocationAvailability(LocationAvailability availability)
				{
					//boolean isLocation = availability.isLocationAvailable();
				}
			};

			mLocationRequest = new LocationRequest();
			mLocationRequest.setInterval(1000);
			mLocationRequest.setFastestInterval(1000);

			mLocationRequest.setNumUpdates(3);
			mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

			LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
			builder.addLocationRequest(mLocationRequest);

			LocationSettingsRequest mLocationSettingsRequest = builder.build();

			Task<LocationSettingsResponse> locationResponse = mSettingsClient.checkLocationSettings(mLocationSettingsRequest);
			locationResponse.addOnSuccessListener(this, locationSettingsResponse ->
			{
				if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
					return;

				mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
			});

			locationResponse.addOnFailureListener(this, e ->
			{
				int statusCode = ((ApiException) e).getStatusCode();
				switch (statusCode)
				{
					case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
						Common.LogMessage("onFailure: Location environment check");
						break;
					case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
						String errorMessage = "Check location setting";
						Common.LogMessage("onFailure: " + errorMessage);
				}
			});
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();
		stopFusedClient();
	}

	private void stopFusedClient()
	{
		if(mFusedLocationClient != null)
		{
			mFusedLocationClient.removeLocationUpdates(mLocationCallback);
			mFusedLocationClient = null;
		}
	}

//	@Override
//	public void onLocationChanged(@NonNull Location myLocation)
//	{
//		if (myLocation != null)
//		{
//			Common.LogMessage("Location Changed " + myLocation.getLatitude() + " and " + myLocation.getLongitude());
//			mLocationManager.removeUpdates(this);
//
//			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 16.0f));
//
//			LatLng myLL = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
//			Marker m = mMap.addMarker(new MarkerOptions().position(myLL).title("Drag this marker to the location of the problem.").draggable(true));
//			m.showInfoWindow();
//			mMap.moveCamera(CameraUpdateFactory.newLatLng(myLL));
//
//			String llat = df.format(myLocation.getLatitude());
//			String llng = df.format(myLocation.getLongitude());
//
//			lat.setText(llat);
//			lng.setText(llng);
//
//			updateButtons();
//		}
//	}

	void doMore2()
	{}

	public void signInView(View v)
	{
		Report r = new Report();
		r.lat = lat.getText().toString();
		r.lng = lng.getText().toString();

		Intent i = new Intent(getBaseContext(), SignIn.class);
		i.putExtra("report", r.toString());
		startActivity(i);
	}

	public void signUpView(View v)
	{
		Report r = new Report();
		r.lat = lat.getText().toString();
		r.lng = lng.getText().toString();

		Intent i = new Intent(getBaseContext(), SignUp.class);
		i.putExtra("report", r.toString());
		startActivity(i);
	}

	public void reportProblem(View v)
	{
		Report r = new Report();
		r.lat = lat.getText().toString();
		r.lng = lng.getText().toString();

		Intent i = new Intent(getBaseContext(), ReportDetails.class);
		i.putExtra("report", r.toString());
		startActivity(i);
	}

	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		mMap = googleMap;

		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-34, 151), 12.0f));

		LatLng myLL = new LatLng(-33.938287, 151.171844);
		Marker m = mMap.addMarker(new MarkerOptions().position(myLL).title("Drag this marker to the location of the problem.").draggable(true));
		m.showInfoWindow();
		mMap.moveCamera(CameraUpdateFactory.newLatLng(myLL));

		mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener()
		{
			@Override
			public void onMarkerDragStart(Marker arg0)
			{}

			@Override
			public void onMarkerDragEnd(Marker arg0)
			{
				mMap.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));

				LatLng position = arg0.getPosition();

				String llat = df.format(position.latitude);
				String llng = df.format(position.longitude);

				lat.setText(llat);
				lng.setText(llng);

				updateButtons();
				stopFusedClient();
			}

			@Override
			public void onMarkerDrag(Marker arg0)
			{}
		});
		mMap.getUiSettings().setMapToolbarEnabled(false);
		doMore();
	}
}