package com.odiousapps.android.fixmystreetnet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class DetailReport extends Activity
{
	private Common common;
	private TextView lat;
	private TextView lng;
	private TextView address;
	private TextView council;
	private TextView problem;
	private TextView summary;
	private TextView extra;
	private WebView pics;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detailreport);

		common = new Common(this);

		lat = findViewById(R.id.lat);
		lng = findViewById(R.id.lng);
		address = findViewById(R.id.address);
		council = findViewById(R.id.council);
		problem = findViewById(R.id.problem);
		summary = findViewById(R.id.summary);
		extra = findViewById(R.id.extra_details);
		pics = findViewById(R.id.pics);

		Intent i = getIntent();
		final String id = (String)i.getSerializableExtra("id");

		Thread t = new Thread(() ->
		{
			try
			{
				String j = common.grabInfo(id);
				if(j == null)
				{
					runOnUiThread(() -> failedAuth("Unable to download marker locations."));
					return;
				}

				runOnUiThread(() -> displayInfo(j));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		t.start();
	}

	void displayInfo(String str)
	{
		try
		{
			if (str.equals(""))
				return;

			JSONObject j = new JSONObject(str);
			lat.setText(j.getString("latitude"));
			lng.setText(j.getString("longitude"));
			address.setText(j.getString("address"));
			council.setText(j.getString("council"));
			problem.setText(j.getString("defect"));
			summary.setText(j.getString("summary"));
			extra.setText(j.getString("extra"));

			StringBuilder pichtml = new StringBuilder("<head><body><table style='width:100%'><tr>");
			JSONArray ja = j.getJSONArray("photos");
			for(int i = 0; i < ja.length(); i++)
			{
				JSONObject pic = ja.getJSONObject(i);
				pichtml.append("<td align='center'><img src='https://fixmystreet.net/")
					   .append(pic.getString("thumb"))
				       .append("' /></td>");
			}
			pichtml.append("</tr></table></body></html>");
			pics.loadDataWithBaseURL("file:///android_res/drawable/", pichtml.toString().trim(), "text/html", "utf-8", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void failedAuth(String errmsg)
	{
		common.showMessage(errmsg);
	}
}
