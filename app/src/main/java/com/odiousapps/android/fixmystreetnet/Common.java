package com.odiousapps.android.fixmystreetnet;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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

	void RemovePref(String name)
	{
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(name);
		editor.apply();

		LogMessage("Removing '" + name + "'");
	}

	void commit()
	{
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.apply();
	}

	String GetStringPref(String name, String defval)
	{
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		String value;

		try
		{
			value = settings.getString(name, defval);
		} catch (ClassCastException cce)
		{
			cce.printStackTrace();
			return defval;
		} catch (Exception e)
		{
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
}
