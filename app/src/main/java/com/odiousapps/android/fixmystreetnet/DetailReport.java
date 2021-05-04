package com.odiousapps.android.fixmystreetnet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

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
	private CarouselView carouselView;
	private final ArrayList<Bitmap> bitmapArray = new ArrayList<>();

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
		carouselView = findViewById(R.id.carouselView);

		Intent i = getIntent();
		final String id = (String)i.getSerializableExtra("id");

		Thread t = new Thread(() ->
		{
			try
			{
				String str = common.grabInfo(id).trim();
				if(str == null)
				{
					runOnUiThread(() -> failedAuth("Unable to download marker locations."));
					return;
				}

				JSONObject j = new JSONObject(str);
				final String tmpstr1 = j.getString("latitude");
				runOnUiThread(() -> lat.setText(tmpstr1));
				final String tmpstr2 = j.getString("longitude");
				runOnUiThread(() -> lng.setText(tmpstr2));
				final String tmpstr3 = j.getString("address");
				runOnUiThread(() -> address.setText(tmpstr3));
				final String tmpstr4 = j.getString("council");
				runOnUiThread(() -> council.setText(tmpstr4));
				final String tmpstr5 = j.getString("defect");
				runOnUiThread(() -> problem.setText(tmpstr5));
				final String tmpstr6 = j.getString("summary");
				runOnUiThread(() -> summary.setText(tmpstr6));
				final String tmpstr7 = j.getString("extra");
				runOnUiThread(() -> extra.setText(tmpstr7));

				JSONArray ja = j.getJSONArray("photos");
				for(int k = 0; k < ja.length(); k++)
				{
					JSONObject pic = ja.getJSONObject(k);
//					String url = "https://fixmystreet.net/" + pic.getString("thumb");
					String url = "https://fixmystreet.net/" + pic.getString("file_path");

					Bitmap bitmap = common.downloadImage(url);
					runOnUiThread(() -> addBitmap(bitmap));
				}

				runOnUiThread(this::updateCarousel);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		t.start();
	}

	void addBitmap(Bitmap bitmap)
	{
		bitmapArray.add(bitmap);
	}

	void updateCarousel()
	{
		ImageListener imageListener = (position, imageView) ->
		{
			imageView.setImageBitmap(bitmapArray.get(position));
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		};
		Common.LogMessage("bitmapArray.size() == " + bitmapArray.size());
		carouselView.setImageListener(imageListener);
		carouselView.setPageCount(bitmapArray.size());

	}

	void failedAuth(String errmsg)
	{
		common.showMessage(errmsg);
	}
}
