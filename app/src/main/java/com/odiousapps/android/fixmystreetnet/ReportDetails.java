package com.odiousapps.android.fixmystreetnet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.net.URLEncoder;

public class ReportDetails extends Activity
{
	TextView address, council;
	EditText summary, extra;
	Spinner problem_dd;
	private static Report r = new Report();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

		try
		{
			Intent i = getIntent();
			String json = (String)i.getSerializableExtra("report");
			Common.LogMessage("json = " + json);
			r = r.fromString(json);
			Common.LogMessage("r = " + r.toString());

			LatLng ll = new LatLng(Double.parseDouble(r.lat), Double.parseDouble(r.lng));
			revgeocode(ll);
		} catch (Exception e) {
			e.printStackTrace();
		}

		problem_dd = findViewById(R.id.problem_dd);
		for(int j = 0; j < problem_dd.getCount(); j++)
		{
			if(problem_dd.getItemAtPosition(j).equals("Pothole"))
			{
				problem_dd.setSelection(j);
				break;
			}
		}

		summary = findViewById(R.id.password);
		extra = findViewById(R.id.extra_details);
		address = findViewById(R.id.address);
		council = findViewById(R.id.council);
	}

	void updateScreen()
	{
		address.setText(r.address);
		council.setText(r.council);
	}

	void revgeocode(LatLng ll) throws Exception
	{
		Thread t = new Thread(() ->
		{
			try
			{
				String url = "https://fixmystreet.net/api/revgeocode.php";
				url += "?serverKey=" + URLEncoder.encode(getString(R.string.serverKey), "UTF-8");
				url += "&lat=" + ll.latitude + "&lng=" + ll.longitude;

				Connection.Response resultResponse = Jsoup.connect(url).userAgent(Common.UA).maxBodySize(Integer.MAX_VALUE).ignoreContentType(true).execute();
				JSONObject j = new JSONObject(resultResponse.body());
				r.address = j.getString("address");
				r.council = j.getString("council");

				runOnUiThread(this::updateScreen);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		t.start();
	}

	public void reportView(View v)
	{
		r.problem = problem_dd.getSelectedItem().toString();
		r.summary = summary.getText().toString();
		r.extra = extra.getText().toString();
		Intent i = new Intent(getBaseContext(), Photos.class);
		i.putExtra("report", r.toString());
		startActivity(i);
	}
}