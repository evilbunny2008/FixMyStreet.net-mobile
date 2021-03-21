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
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback//, GoogleMap.OnMarkerClickListener
{
	private final int permsRequestCode = 200;
	private GoogleMap mMap;
	private TextView lat, lng;
	private final DecimalFormat df = new DecimalFormat("#.######");
	private Common common;

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

		if (Build.VERSION.SDK_INT < 23)
		{
			doMore2();
		} else {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			{
				ActivityCompat.requestPermissions(this, new String[]
						{
								Manifest.permission.ACCESS_FINE_LOCATION,
						}, permsRequestCode);
			} else {
				doMore2();
			}
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		LinearLayout ll = findViewById(R.id.signupin);
		LinearLayout ll2 = findViewById(R.id.report);

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
		if (requestCode == permsRequestCode)
		{
			if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				hasPermission = true;
			}
		}

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
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		Location myLocation = null;

		if (locationManager != null && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < 23))
		{
			myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (myLocation == null)
				myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}

		if(myLocation != null)
		{
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 16.0f));

			LatLng myLL = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
			Marker m = mMap.addMarker(new MarkerOptions().position(myLL).title("Drag this marker to the location of the problem.").draggable(true));
			m.showInfoWindow();
			mMap.moveCamera(CameraUpdateFactory.newLatLng(myLL));

			String llat = df.format(myLocation.getLatitude());
			String llng = df.format(myLocation.getLongitude());

			lat.setText(llat);
			lng.setText(llng);
		}
	}

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
		r.email = common.GetStringPref("email", "");

		Intent i = new Intent(getBaseContext(), ReportDetails.class);
		i.putExtra("report", r.toString());
		startActivity(i);
	}

	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		mMap = googleMap;
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
			}

			@Override
			public void onMarkerDrag(Marker arg0)
			{}
		});
		mMap.getUiSettings().setMapToolbarEnabled(false);
		doMore();
	}
}