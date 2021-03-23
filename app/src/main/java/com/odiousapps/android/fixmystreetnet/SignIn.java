package com.odiousapps.android.fixmystreetnet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.net.URLEncoder;

public class SignIn extends Activity
{
	private Report r;
	private Common common;
	private EditText email, password;
	private Button loginButton;
	private LinearLayout ll;
	private static String errmsg = "Your login was unsuccessful. Please check your email is registered and your password is correct.";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin);

		common = new Common(this);

		String em = common.GetStringPref("email", "");
		String pw = common.GetStringPref("password", "");

		email = findViewById(R.id.email);
		password = findViewById(R.id.password);
		loginButton = findViewById(R.id.loginButton);
		ll = findViewById(R.id.showWaiting);

		email.setText(em);
		password.setText(pw);

		Intent i = getIntent();
		String json = (String)i.getSerializableExtra("report");
		r = new Report();
		r.fromString(json);
	}

	public void nextScreenView(View v)
	{
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

		if(email.getText().toString().length() < 8)
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

		if(password.getText().toString().equals(""))
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

		common.SetStringPref("email", email.getText().toString());
		common.SetStringPref("password", password.getText().toString());

		loginButton.setEnabled(false);
		ll.setVisibility(View.VISIBLE);

		checkAuth();
	}


	void checkAuth()
	{
		Thread t = new Thread(() ->
		{
			try
			{
				String url = "https://fixmystreet.net/api/auth.php";
				url += "?email=" + URLEncoder.encode(email.getText().toString(), "UTF-8");
				url += "&password=" + URLEncoder.encode(password.getText().toString(), "UTF-8");

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

			runOnUiThread(this::badLogin);
		});

		t.start();
	}

	void badLogin()
	{
		new AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_launcher)
			.setTitle("Failed")
			.setMessage(errmsg)
			.setNeutralButton("Retry", (dialog, whichButton) ->
			{
				loginButton.setEnabled(true);
				ll.setVisibility(View.GONE);
			})
			.show();
	}
}