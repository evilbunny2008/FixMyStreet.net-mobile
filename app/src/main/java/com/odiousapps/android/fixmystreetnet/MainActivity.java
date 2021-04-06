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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback
{
	private final int permsRequestCode = 200;
	private GoogleMap mMap;
	private TextView lat, lng;
	private LinearLayout signupin, report, reportProblemsLL, showProblemLL;
	private final DecimalFormat df = new DecimalFormat("#.######");
	private Common common;

	LocationRequest mLocationRequest;
	FusedLocationProviderClient mFusedLocationClient;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		common = new Common(this);

		signupin = findViewById(R.id.signupin);
		report = findViewById(R.id.report);

		reportProblemsLL = findViewById(R.id.reportProblemLL);
		showProblemLL = findViewById(R.id.showProblemsLL);

		lat = findViewById(R.id.lat);
		lng = findViewById(R.id.lng);

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		assert mapFragment != null;
		mapFragment.getMapAsync(this);

		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
			ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]
			{
					Manifest.permission.ACCESS_FINE_LOCATION,
					Manifest.permission.ACCESS_COARSE_LOCATION
			}, permsRequestCode);
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
		Common.LogMessage("lat == " + lat.getText().toString());
		Common.LogMessage("lng == " + lng.getText().toString());

		if(lat.getText().toString().equals(getString(R.string.loading)) ||
				lng.getText().toString().equals(getString(R.string.loading)))
			return;

		if (common.GetStringPref("lastauth", "0").equals("1"))
		{
			signupin.setVisibility(View.GONE);
			report.setVisibility(View.VISIBLE);
		} else {
			report.setVisibility(View.GONE);
			signupin.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		boolean hasPermission = false;
		if(requestCode == permsRequestCode && grantResults.length >= 2
				&& grantResults[0] == PackageManager.PERMISSION_GRANTED
				&& grantResults[1] == PackageManager.PERMISSION_GRANTED)
			hasPermission = true;

		if(!hasPermission)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("You have rejected 1 or more permissions critical to using this app, if you clicked deny you" +
					"may have to go into settings to allow").setCancelable(false)
					.setPositiveButton("Ok", (dialoginterface, i) -> finish());
			builder.create().show();
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		pauseFusedClient();
	}

	private void pauseFusedClient()
	{
		if (mFusedLocationClient != null)
		{
			mFusedLocationClient.removeLocationUpdates(mLocationCallback);
			mFusedLocationClient = null;
		}
	}

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

	public void reportAProblem(View v)
	{
		reportProblemsLL.setVisibility(View.VISIBLE);
		showProblemLL.setVisibility(View.GONE);

//		Marker m = mMap.addMarker(new MarkerOptions().position(myLL).title("Drag this marker to the location of the problem.").draggable(true));
//		m.showInfoWindow();
//		Marker m = mMap.addMarker(new MarkerOptions().position(myLL).title("Drag this marker to the location of the problem.").draggable(true));
//		m.showInfoWindow();
	}

	private void refreshLocation(Location myLocation)
	{
		Common.LogMessage("Location Changed " + myLocation.getLatitude() + " and " + myLocation.getLongitude());
		pauseFusedClient();

		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 16.0f));

		LatLng myLL = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
		mMap.moveCamera(CameraUpdateFactory.newLatLng(myLL));

		String llat = df.format(myLocation.getLatitude());
		String llng = df.format(myLocation.getLongitude());

		lat.setText(llat);
		lng.setText(llng);

		updateButtons();
	}

	LocationCallback mLocationCallback = new LocationCallback()
	{
		@Override
		public void onLocationResult(LocationResult locationResult)
		{
			List<Location> locationList = locationResult.getLocations();
			if(locationList.size() > 0)
			{
				Location location = locationList.get(locationList.size() - 1);
				Common.LogMessage("Location: " + location.getLatitude() + " " + location.getLongitude());

				refreshLocation(location);
			}
		}
	};

	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		mMap = googleMap;

		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-34, 151), 12.0f));

		LatLng myLL = new LatLng(-33.859046, 151.2050339);
		mMap.moveCamera(CameraUpdateFactory.newLatLng(myLL));

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			mLocationRequest = LocationRequest.create();
			mLocationRequest.setInterval(120000); // two minute interval
			mLocationRequest.setFastestInterval(120000);
			mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
			mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
		}

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
				pauseFusedClient();
			}

			@Override
			public void onMarkerDrag(Marker arg0)
			{}
		});
		mMap.getUiSettings().setMapToolbarEnabled(false);
	}
}