package com.odiousapps.android.fixmystreetnet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.net.URLEncoder;

public class SignUp extends Activity
{
	private EditText name, email, mobile, password, repeat;
	private static Report r = new Report();
	private Common common;
	private static String errmsg = "Your signup was unsuccessful. Please check your email and your password.";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);

		common = new Common(this);

		Intent i = getIntent();
		String json = (String)i.getSerializableExtra("report");
		r = r.fromString(json);

		name = findViewById(R.id.name);
		email = findViewById(R.id.email);
		mobile = findViewById(R.id.mobile);
		password = findViewById(R.id.password);
		repeat = findViewById(R.id.repeat);
	}

	public void signupView(View v)
	{
		if(name.getText().toString().equals(""))
		{
			new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle("Failed")
				.setMessage("Name can't be blank.")
				.setNeutralButton("Retry", (dialog, whichButton) ->
				{
				})
				.show();

			return;
		}

		if(email.getText().toString().equals(""))
		{
			new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle("Failed")
				.setMessage("Email can't be blank.")
				.setNeutralButton("Retry", (dialog, whichButton) ->
				{
				})
				.show();

			return;
		}

		if(mobile.getText().toString().equals(""))
		{
			new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle("Failed")
				.setMessage("Mobile can't be blank.")
				.setNeutralButton("Retry", (dialog, whichButton) ->
				{
				})
				.show();

			return;
		}

		if(mobile.getText().toString().length() < 10)
		{
			new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle("Failed")
				.setMessage("Mobile number seems to be invalid")
				.setNeutralButton("Retry", (dialog, whichButton) ->
				{
				})
				.show();

			return;
		}

		if(password.getText().toString().equals("") || repeat.getText().toString().equals(""))
		{
			new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle("Failed")
				.setMessage("Password can't be blank.")
				.setNeutralButton("Retry", (dialog, whichButton) ->
				{
				})
				.show();

			return;
		}

		if(!password.getText().toString().equals(repeat.getText().toString()))
		{
			new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle("Failed")
				.setMessage("Password don't match.")
				.setNeutralButton("Retry", (dialog, whichButton) ->
				{
				})
				.show();

			return;
		}

		if(password.getText().toString().length() < 8)
		{
			new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle("Failed")
				.setMessage("Password wasn't long enough.")
				.setNeutralButton("Retry", (dialog, whichButton) ->
				{
				})
				.show();

			return;
		}

		signup();
	}

	void signup()
	{
		Thread t = new Thread(() ->
		{
			try
			{
				String nm = name.getText().toString();
				String em = email.getText().toString();
				String mo = mobile.getText().toString();
				String pw = password.getText().toString();

				String url = "https://fixmystreet.net/api/signup.php";
				url += "?name=" + URLEncoder.encode(nm, "UTF-8");
				url += "&email=" + URLEncoder.encode(em, "UTF-8");
				url += "&mobile=" + URLEncoder.encode(mo, "UTF-8");
				url += "&password=" + URLEncoder.encode(pw, "UTF-8");

				Connection.Response resultResponse = Jsoup.connect(url).userAgent(Common.UA).maxBodySize(Integer.MAX_VALUE).ignoreContentType(true).execute();
				JSONObject j = new JSONObject(resultResponse.body());
				Common.LogMessage(j.toString());
				if(j.getString("status").equals("OK"))
				{
					common.SetStringPref("lastauth", "1");
					Intent i = new Intent(getBaseContext(), ReportDetails.class);
					i.putExtra("report", r.toString());
					startActivity(i);
					finish();
					return;
				} else
					errmsg = j.getString("errmsg");
			} catch (Exception e) {
				e.printStackTrace();
			}

			runOnUiThread(this::badSignup);
		});

		t.start();
	}

	void badSignup()
	{
		new AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_launcher)
			.setTitle("Failed")
			.setMessage(errmsg)
			.setNeutralButton("Retry", (dialog, whichButton) ->
			{
			})
			.show();
	}
}