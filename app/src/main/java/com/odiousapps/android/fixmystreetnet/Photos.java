package com.odiousapps.android.fixmystreetnet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

public class Photos extends Activity
{
	private Report r = new Report();

	private static final int REQUEST_IMAGE_CAPTURE1 = 1;
	private static final int REQUEST_IMAGE_CAPTURE2 = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photos);

		Intent i = getIntent();
		String json = (String) i.getSerializableExtra("report");
		r = r.fromString(json);
	}

	private File createImageFile(boolean isWide) throws IOException
	{
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(imageFileName,".jpg", storageDir);

		// Save a file: path for use with ACTION_VIEW intents
		if(isWide)
			r.wide = image.getAbsolutePath();
		else
			r.close = image.getAbsolutePath();
		return image;
	}

	public void wideShotView(View v)
	{
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) == null)
			return;

		File photoFile = null;
		try {
			photoFile = createImageFile(true);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		if(photoFile == null)
			return;

		Uri photoURI = FileProvider.getUriForFile(this,"com.odiousapps.android.fixmystreetnet.provider", photoFile);
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
		startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE1);
	}

	public void closeShotView(View v)
	{
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) == null)
			return;

		File photoFile = null;
		try {
			photoFile = createImageFile(false);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		if(photoFile == null)
			return;

		Uri photoURI = FileProvider.getUriForFile(this,"com.odiousapps.android.fixmystreetnet.provider", photoFile);
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
		startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE2);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == REQUEST_IMAGE_CAPTURE1 && resultCode == RESULT_OK)
		{
			try
			{
				ImageView im = findViewById(R.id.imageView);
				im.setImageBitmap(BitmapFactory.decodeFile(r.wide));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if(requestCode == REQUEST_IMAGE_CAPTURE2 && resultCode == FragmentActivity.RESULT_OK)
		{
			try
			{
				ImageView im = findViewById(R.id.imageView2);
				im.setImageBitmap(BitmapFactory.decodeFile(r.close));
			} catch (Exception e) {
				e.printStackTrace();
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