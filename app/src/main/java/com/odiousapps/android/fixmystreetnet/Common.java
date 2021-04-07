package com.odiousapps.android.fixmystreetnet;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.net.URLEncoder;

class Common
{
	private final Context context;
	private final static boolean debug = true;
	private final static String PREFS_NAME = "FixMyStreetPrefs";
	final static String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36";

	Common(Context c)
	{
		this.context = c;
	}

	static void LogMessage(String value)
	{
		LogMessage(value, false);
	}

	static void LogMessage(String value, boolean showAnyway)
	{
		if(debug || showAnyway)
		{
			int len = value.indexOf("\n");
			if(len <= 0)
				len = value.length();
			Log.i("FixMyStreet.net", "message='" + value.substring(0, len) + "'");
		}
	}

	void SetStringPref(String name, String value)
	{
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(name, value);
		editor.apply();

		LogMessage("Updating '" + name);
	}

//	void RemovePref(String name)
//	{
//		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
//		SharedPreferences.Editor editor = settings.edit();
//		editor.remove(name);
//		editor.apply();
//
//		LogMessage("Removing '" + name + "'");
//	}

//	void commit()
//	{
//		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
//		SharedPreferences.Editor editor = settings.edit();
//		editor.apply();
//	}

	String GetStringPref(String name, String defval)
	{
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		String value;

		try
		{
			value = settings.getString(name, defval);
		} catch (ClassCastException cce) {
			cce.printStackTrace();
			return defval;
		} catch (Exception e) {
			LogMessage("GetStringPref(" + name + ") Err: " + e.toString());
			e.printStackTrace();
			return defval;
		}

		if(!name.equals("password"))
			LogMessage(name + "'='" + value);
		else
			LogMessage(name + "'='**************");

		return value;
	}

	void showMessage(String msg)
	{
		Toast.makeText(this.context, msg, Toast.LENGTH_LONG).show();
	}

	String grabMarkers(double north, double east, double south, double west)
	{
		try
		{
			String url = "https://fixmystreet.net/api/problems.php";
			url += "?serverKey=" + URLEncoder.encode(context.getString(R.string.serverKey), "UTF-8");
			url += "&north=" + north + "&east=" + east + "&south=" + south + "&west=" + west;
			Connection.Response resultResponse = Jsoup.connect(url).userAgent(Common.UA).maxBodySize(Integer.MAX_VALUE).ignoreContentType(true).execute();
			return resultResponse.body();
		} catch(Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
