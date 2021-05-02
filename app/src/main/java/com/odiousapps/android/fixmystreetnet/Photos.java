package com.odiousapps.android.fixmystreetnet;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.FragmentActivity;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Photos extends Activity
{
	private Report r = new Report();
	private Common common;

	private static final int REQUEST_IMAGE_CAPTURE1 = 1;
	private static final int REQUEST_IMAGE_CAPTURE2 = 2;
	private static final String TEMP_IMAGE_NAME = "tempImage";

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

	private File createImageFile(boolean isWide) throws IOException
	{
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(imageFileName, ".jpg", storageDir);

		// Save a file: path for use with ACTION_VIEW intents
		if (isWide)
			r.wide = image.getAbsolutePath();
		else
			r.close = image.getAbsolutePath();
		return image;
	}

	public void wideShotView(View v)
	{
		ImageView im = findViewById(R.id.imageView);
		try
		{
			im.setImageBitmap(null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) == null)
			return;

		File photoFile = null;
		try
		{
			photoFile = createImageFile(true);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		if (photoFile == null)
			return;

		Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		Intent chooser = new Intent(Intent.ACTION_CHOOSER);
		chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
		chooser.putExtra(Intent.EXTRA_TITLE, getString(R.string.chooseaction));
		Intent[] intentArray = {takePictureIntent};
		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

		Uri photoURI = FileProvider.getUriForFile(this, "com.odiousapps.android.fixmystreetnet.provider", photoFile);
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
		startActivityForResult(chooser, REQUEST_IMAGE_CAPTURE1);
	}

	public void closeShotView(View v)
	{
		ImageView im = findViewById(R.id.imageView2);
		try
		{
			im.setImageBitmap(null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getPackageManager()) == null)
			return;

		File photoFile = null;
		try
		{
			photoFile = createImageFile(false);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		if (photoFile == null)
			return;

		Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		Intent chooser = new Intent(Intent.ACTION_CHOOSER);
		chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
		chooser.putExtra(Intent.EXTRA_TITLE, getString(R.string.chooseaction));
		Intent[] intentArray = {takePictureIntent};
		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

		Uri photoURI = FileProvider.getUriForFile(this, "com.odiousapps.android.fixmystreetnet.provider", photoFile);
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
		startActivityForResult(chooser, REQUEST_IMAGE_CAPTURE2);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_IMAGE_CAPTURE1 && resultCode == RESULT_OK)
		{
			File imageFile = new File(this.getExternalCacheDir(), TEMP_IMAGE_NAME);
			boolean isCamera = (data == null || data.getData() == null  ||
					data.getData().toString().contains(imageFile.toString()));
			if(isCamera)
			{
				try
				{
					ExifInterface exif = new ExifInterface(r.wide);
					int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
					Bitmap bitmap = BitmapFactory.decodeFile(r.wide);
					int angle = exifToDegrees(orientation);
					bitmap = RotateBitmap(bitmap, angle);

					ImageView im = findViewById(R.id.imageView);
					im.setImageBitmap(bitmap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try
				{
					int result = 0;
					if(Build.VERSION.SDK_INT >= 29)
					{
						String[] columns = {MediaStore.Images.Media.ORIENTATION};
						Cursor cursor;
						cursor = this.getContentResolver().query(data.getData(), columns, null, null, null);
						if(cursor != null && cursor.moveToFirst())
						{
							int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
							result = cursor.getInt(orientationColumnIndex);
						}
						if(cursor != null)
							cursor.close();

						Common.LogMessage("Image rotation: " + result);
					}

					final InputStream ist = this.getContentResolver().openInputStream(data.getData());
					Bitmap bitmap = BitmapFactory.decodeStream(ist);
					ist.close();
					if(Build.VERSION.SDK_INT >= 29 && result != 0)
						bitmap = RotateBitmap(bitmap, result);

					File f = createImageFile(true);
					FileOutputStream fos = new FileOutputStream(f);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
					fos.flush();
					fos.close();

					ImageView im = findViewById(R.id.imageView);
					im.setImageBitmap(bitmap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (requestCode == REQUEST_IMAGE_CAPTURE2 && resultCode == FragmentActivity.RESULT_OK)
		{
			File imageFile = new File(this.getExternalCacheDir(), TEMP_IMAGE_NAME);
			boolean isCamera = (data == null || data.getData() == null  ||
					data.getData().toString().contains(imageFile.toString()));
			if(isCamera)
			{
				try
				{
					ExifInterface exif = new ExifInterface(r.close);
					int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
					Bitmap bitmap = BitmapFactory.decodeFile(r.close);
					int angle = exifToDegrees(orientation);
					bitmap = RotateBitmap(bitmap, angle);

					ImageView im = findViewById(R.id.imageView2);
					im.setImageBitmap(bitmap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try
				{
					int result = 0;
					if(Build.VERSION.SDK_INT >= 29)
					{
						String[] columns = {MediaStore.Images.Media.ORIENTATION};
						Cursor cursor;
						cursor = this.getContentResolver().query(data.getData(), columns, null, null, null);
						if(cursor != null && cursor.moveToFirst())
						{
							int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
							result = cursor.getInt(orientationColumnIndex);
						}
						if(cursor != null)
							cursor.close();

						Common.LogMessage("Image rotation: " + result);
					}

					final InputStream ist = this.getContentResolver().openInputStream(data.getData());
					Bitmap bitmap = BitmapFactory.decodeStream(ist);
					ist.close();
					if(Build.VERSION.SDK_INT >= 29 && result != 0)
						bitmap = RotateBitmap(bitmap, result);

					File f = createImageFile(false);
					FileOutputStream fos = new FileOutputStream(f);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
					fos.flush();
					fos.close();

					ImageView im = findViewById(R.id.imageView2);
					im.setImageBitmap(bitmap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Bitmap RotateBitmap(Bitmap source, float angle)
	{
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		Bitmap rotatedbitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
		source.recycle();
		return rotatedbitmap;
	}

	public static int exifToDegrees(int exifOrientation)
	{
		switch (exifOrientation)
		{
			case ExifInterface.ORIENTATION_ROTATE_90:
				return 90;
			case ExifInterface.ORIENTATION_ROTATE_180:
				return 180;
			case ExifInterface.ORIENTATION_ROTATE_270:
				return 270;
			default:
				return 0;
		}
	}

	public void reportView(View v)
	{
		if(r.wide == null || r.close == null)
		{
			common.showMessage("You must pick or take at least 2 photos, this speeds up f");
			return;
		}

		Thread t = new Thread(() ->
		{
			String errmsg = "There was a problem uploading to the server, please try again";

			try
			{
				final MediaType MEDIA_TYPE = MediaType.parse("image/jpeg");

				File sourceFile1 = new File(r.wide);
				File sourceFile2 = new File(r.close);

				RequestBody requestBody = new MultipartBody.Builder()
						.setType(MultipartBody.FORM)
						.addFormDataPart("serverKey", URLEncoder.encode(getString(R.string.serverKey), "UTF-8"))
						.addFormDataPart("email", URLEncoder.encode(common.GetStringPref("email", ""), "UTF-8"))
						.addFormDataPart("password", URLEncoder.encode(common.GetStringPref("password", ""), "UTF-8"))
						.addFormDataPart("lat", URLEncoder.encode(r.lat, "UTF-8"))
						.addFormDataPart("lng", URLEncoder.encode(r.lng, "UTF-8"))
						.addFormDataPart("address", URLEncoder.encode(r.address, "UTF-8"))
						.addFormDataPart("council", URLEncoder.encode(r.council, "UTF-8"))
						.addFormDataPart("summary", URLEncoder.encode(r.summary, "UTF-8"))
						.addFormDataPart("extra", URLEncoder.encode(r.extra, "UTF-8"))
						.addFormDataPart("defect", URLEncoder.encode(r.defect, "UTF-8"))

						.addFormDataPart("photos[]", sourceFile1.getName(), RequestBody.create(sourceFile1, MEDIA_TYPE))
						.addFormDataPart("photos[]", sourceFile2.getName(), RequestBody.create(sourceFile2, MEDIA_TYPE))
						.build();

				Request request = new Request.Builder()
						.header("User-Agent", Common.UA)
						.url("https://fixmystreet.net/api/upload.php")
						.post(requestBody)
						.build();

				OkHttpClient client = new OkHttpClient();
				Response response = client.newCall(request).execute();

				JSONObject j = new JSONObject(Objects.requireNonNull(response.body()).string());
				Common.LogMessage(j.toString());
				if(j.getString("status").equals("OK"))
					errmsg = "Upload was a-ok...";
			} catch (Exception e) {
				e.printStackTrace();
				errmsg = Arrays.toString(e.getStackTrace());
			}

			String finalErrmsg = errmsg;
			runOnUiThread(() -> updateScreen(finalErrmsg));
		});

		t.start();
	}

	private void updateScreen(String errmsg)
	{
		common.showMessage(errmsg);
	}
}