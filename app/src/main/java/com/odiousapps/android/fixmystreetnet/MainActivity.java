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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback
{
	private final int permsRequestCode = 200;
	private GoogleMap mMap;
	private TextView lat, lng;
	private LinearLayout signupin, report, reportProblemsLL, showProblemLL;
	private final DecimalFormat df = new DecimalFormat("#.######");
	private Common common;
	private final HashMap<String, String> markerMap = new HashMap<>();

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

		SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
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

		String s = common.GetStringPref("lastauth", "0");
		if((s.equals("1") && signupin.getVisibility() == View.GONE) ||
				(s.equals("0") && signupin.getVisibility() == View.VISIBLE))
			return;

		if (s.equals("1"))
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

		lat.setText(df.format(mMap.getCameraPosition().target.latitude));
		lng.setText(df.format(mMap.getCameraPosition().target.longitude));
		LatLng myLL = new LatLng(Float.parseFloat(lat.getText().toString()), Float.parseFloat(lng.getText().toString()));
		Marker m = mMap.addMarker(new MarkerOptions().position(myLL).title("Drag this marker to the location of the problem.").draggable(true));
		m.showInfoWindow();
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLL, 16.0f));
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

	void displayMarkers(String str)
	{
		if(str.equals(""))
			return;

		String[] markers = str.split("\n");
		for (String marker : markers)
		{
			String[] bits = marker.split("\\|");
			float marker_colour = BitmapDescriptorFactory.HUE_RED;

			switch(bits[6])
			{
				case "blue":
					marker_colour = BitmapDescriptorFactory.HUE_AZURE;
					break;
				case "green":
					marker_colour = BitmapDescriptorFactory.HUE_GREEN;
					break;
				case "orange":
					marker_colour = BitmapDescriptorFactory.HUE_ORANGE;
					break;
				case "yellow":
					marker_colour = BitmapDescriptorFactory.HUE_YELLOW;
					break;
			}

			Marker myMarker = mMap.addMarker(new MarkerOptions()
					.position(new LatLng(Float.parseFloat(bits[1]), Float.parseFloat(bits[2])))
					.title(bits[3])
					.snippet(bits[5])
					.icon(BitmapDescriptorFactory.defaultMarker(marker_colour)));
			String id = myMarker.getId();
			markerMap.put(id, "" + bits[0]);
		}
	}

	void failedAuth(String errmsg)
	{
		common.showMessage(errmsg);
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

	private final GoogleMap.OnCameraIdleListener onCameraIdleListener = new GoogleMap.OnCameraIdleListener()
	{
		@Override
		public void onCameraIdle()
		{
			final LatLngBounds ll = mMap.getProjection().getVisibleRegion().latLngBounds;
			Thread t = new Thread(() ->
			{
				try
				{
					String j = common.grabMarkers(ll.northeast.latitude, ll.northeast.longitude, ll.southwest.latitude, ll.southwest.longitude);
					if(j == null)
					{
						runOnUiThread(() -> failedAuth("Unable to download marker locations."));
						return;
					}

					runOnUiThread(() -> displayMarkers(j));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			t.start();
		}
	};

	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		mMap = googleMap;

		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-34, 151), 12.0f));

		LatLng myLL = new LatLng(-33.859046, 151.2050339);
		mMap.moveCamera(CameraUpdateFactory.newLatLng(myLL));

		mMap.setOnCameraIdleListener(onCameraIdleListener);

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

		mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener()
		{
			@Override
			public void onInfoWindowClick(Marker marker)
			{
				String actionId = markerMap.get(marker.getId());
				Intent i = new Intent(MainActivity.this, DetailReport.class);
				i.putExtra("id", actionId);
				startActivity(i);
			}
		});

		mMap.getUiSettings().setMapToolbarEnabled(false);
	}
}