package com.odiousapps.android.fixmystreetnet;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import androidx.fragment.app.FragmentActivity;

public class Photos extends Activity
{
	private Report r = new Report();
	private Common common;

	private static final int REQUEST_IMAGE_CAPTURE1 = 1;
	private static final int REQUEST_IMAGE_CAPTURE2 = 2;

	private String closejpg, widejpg;
	private Uri photoUri;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photos);

		common = new Common(this);

		Intent i = getIntent();
		String json = (String) i.getSerializableExtra("report");
		r = r.fromString(json);
	}

	public void wideShotView(View v)
	{
		try
		{
			Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			if(galleryIntent.resolveActivity(getPackageManager()) != null)
				startActivityForResult(galleryIntent, REQUEST_IMAGE_CAPTURE1);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void closeShotView(View v)
	{
		Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		Intent chooser = new Intent(Intent.ACTION_CHOOSER);
		chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
		chooser.putExtra(Intent.EXTRA_TITLE, getString(R.string.chooseaction));
		Intent[] intentArray = {cameraIntent};
		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
		startActivityForResult(chooser, REQUEST_IMAGE_CAPTURE2);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == REQUEST_IMAGE_CAPTURE1 && resultCode == RESULT_OK)
		{
			if (data.getData() != null)
			{
				try {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

					ImageView im = findViewById(R.id.imageView);
					im.setImageBitmap(bitmap);

				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				// this case will occur when taking a picture with a camera
				Bitmap bitmap = null;
				Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED}, MediaStore.Images.Media.DATE_ADDED,null,"date_added DESC");
				if (cursor != null && cursor.moveToFirst())
				{
					Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
					String photoPath = uri.toString();
					cursor.close();
					if (photoPath != null)
					{
						bitmap = BitmapFactory.decodeFile(photoPath);
						ImageView im = findViewById(R.id.imageView);
						im.setImageBitmap(bitmap);
					}
				}
			}
		}

		if(requestCode == REQUEST_IMAGE_CAPTURE2 && resultCode == FragmentActivity.RESULT_OK)
		{
			if (data.getData() != null)
			{
				try {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

					ImageView im = findViewById(R.id.imageView2);
					im.setImageBitmap(bitmap);

				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				if(Build.VERSION.SDK_INT >= 29)
				{
					Bitmap bitmap = null;
					Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.ImageColumns.ORIENTATION}, MediaStore.Images.Media.DATE_ADDED, null, "date_added DESC");
					if (cursor != null && cursor.moveToFirst())
					{
						Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
						String photoPath = uri.toString();
						cursor.close();
						if (photoPath != null)
						{
							bitmap = BitmapFactory.decodeFile(photoPath);
							ImageView im = findViewById(R.id.imageView2);
							im.setImageBitmap(bitmap);
						}
					}
				} else {
					Bitmap bitmap = null;
					Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED}, MediaStore.Images.Media.DATE_ADDED, null, "date_added DESC");
					if (cursor != null && cursor.moveToFirst())
					{
						Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
						String photoPath = uri.toString();
						cursor.close();
						if (photoPath != null)
						{
							bitmap = BitmapFactory.decodeFile(photoPath);
							ImageView im = findViewById(R.id.imageView2);
							im.setImageBitmap(bitmap);
						}
					}
				}
			}
		}
	}

	public void reportView(View v)
	{
		try
		{
			Common.LogMessage(r.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}