package com.odiousapps.android.fixmystreetnet;

import com.google.gson.Gson;

import androidx.annotation.NonNull;

class Report
{
	String lat, lng, problem, summary, extra, address, council;
	String wide, close;

	@NonNull
	@Override
	public String toString()
	{
		return new Gson().toJson(this);
	}

	Report fromString(String json)
	{
		Common.LogMessage(json);
		return new Gson().fromJson(json, Report.class);
	}
}