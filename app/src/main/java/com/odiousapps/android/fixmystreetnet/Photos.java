package com.odiousapps.android.fixmystreetnet;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

public class Photos extends Activity
{
	private Report r = new Report();

	private static final int REQUEST_IMAGE_CAPTURE1 = 1;
	private static final int REQUEST_IMAGE_CAPTURE2 = 2;

	private String currentPhotoPath;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photos);

		Intent i = getIntent();
		String json = (String) i.getSerializableExtra("report");
		r = r.fromString(json);
	}

	private File createImageFile() throws IOException
	{
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(imageFileName,  /* prefix */
				".jpg",         /* suffix */
				storageDir      /* directory */
		);

		// Save a file: path for use with ACTION_VIEW intents
		currentPhotoPath = image.getAbsolutePath();
		return image;
	}

	public void wideShotView(View v)
	{
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) == null)
			return;

		File photoFile = null;
		try {
			photoFile = createImageFile();
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
			photoFile = createImageFile();
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
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

				File f = new File(getExternalFilesDir(""), "wide.jpg");
				FileOutputStream fos = new FileOutputStream(f);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
				fos.flush();
				fos.close();

				r.wide = f.getAbsolutePath();

				ImageView im = findViewById(R.id.imageView);
				im.setImageBitmap(bitmap);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if(requestCode == REQUEST_IMAGE_CAPTURE2 && resultCode == FragmentActivity.RESULT_OK)
		{
			try
			{
				ImageView im = findViewById(R.id.imageView2);
				im.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath));
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