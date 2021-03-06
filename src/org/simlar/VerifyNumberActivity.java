/**
 * Copyright (C) 2013 The Simlar Authors.
 *
 * This file is part of Simlar. (http://www.simlar.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.simlar;

import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class VerifyNumberActivity extends Activity
{
	static final String LOGTAG = VerifyNumberActivity.class.getSimpleName();
	private static final int RESULT_CREATE_ACCOUNT_ACTIVITY = 0;

	ProgressDialog mProgressDialog = null;

	private SimlarServiceCommunicator mCommunicator = new SimlarServiceCommunicatorCall();

	private class SimlarServiceCommunicatorCall extends SimlarServiceCommunicator
	{
		public SimlarServiceCommunicatorCall()
		{
			super(LOGTAG);
		}

		@Override
		void onServiceFinishes()
		{
			Log.i(LOGTAG, "onServiceFinishes");

			mProgressDialog.dismiss();

			// prevent switch to MainActivity but finish
			VerifyNumberActivity.this.moveTaskToBack(true);
			VerifyNumberActivity.this.finish();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verify_number);

		final Integer regionCode = Integer.valueOf(SimlarNumber.readRegionCodeFromSimCardOrConfiguration(this));
		final String number = SimlarNumber.readLocalPhoneNumberFromSimCard(this);

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getString(R.string.progress_finishing));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(false);

		if (!Util.isNullOrEmpty(number)) {
			EditText editNumber = (EditText) findViewById(R.id.editTextPhoneNumber);
			editNumber.setText(number);
		}

		//Country Code Selector
		ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			adapter.addAll(SimlarNumber.getSupportedCountryCodes());
		} else {
			for (final Integer countryCode : SimlarNumber.getSupportedCountryCodes()) {
				adapter.add(countryCode);
			}
		}
		adapter.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer lhs, Integer rhs)
			{
				return lhs.compareTo(rhs);
			}
		});

		Spinner spinner = (Spinner) findViewById(R.id.spinnerCountryCodes);
		spinner.setAdapter(adapter);

		Log.i(LOGTAG, "proposing country code: " + regionCode);
		if (regionCode.intValue() > 0) {
			spinner.setSelection(adapter.getPosition(regionCode));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return true;
	}

	@Override
	protected void onResume()
	{
		Log.i(LOGTAG, "onResume ");
		super.onResume();

		mCommunicator.register(this, VerifyNumberActivity.class);
		if (PreferencesHelper.getCreateAccountStatus() == CreateAccountStatus.WAITING_FOR_SMS) {
			Log.i(LOGTAG, "CreateAccountStatus = WAITING FOR SMS");
			startActivityForResult(new Intent(this, CreateAccountActivity.class), RESULT_CREATE_ACCOUNT_ACTIVITY);
		}
	}

	@Override
	protected void onPause()
	{
		Log.i(LOGTAG, "onPause");
		mCommunicator.unregister(this);
		super.onPause();
	}

	@SuppressWarnings("unused")
	public void createAccount(View view)
	{
		final Spinner spinner = (Spinner) findViewById(R.id.spinnerCountryCodes);
		final EditText editNumber = (EditText) findViewById(R.id.editTextPhoneNumber);

		final Integer countryCallingCode = (Integer) spinner.getSelectedItem();
		if (countryCallingCode == null) {
			Log.e(LOGTAG, "createAccount no country code => aborting");
			return;
		}
		SimlarNumber.setDefaultRegion(countryCallingCode.intValue());

		final String number = editNumber.getText().toString();
		if (Util.isNullOrEmpty(number)) {
			(new AlertDialog.Builder(this))
					.setTitle(R.string.verify_number_activity_alert_no_telephone_number_title)
					.setMessage(R.string.verify_number_activity_alert_no_telephone_number_message)
					.create().show();
			return;
		}

		final String telephoneNumber = "+" + countryCallingCode + number;
		Intent intent = new Intent(this, CreateAccountActivity.class);
		intent.putExtra(CreateAccountActivity.INTENT_EXTRA_NUMBER, telephoneNumber);
		startActivityForResult(intent, RESULT_CREATE_ACCOUNT_ACTIVITY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.i(LOGTAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode);
		if (requestCode == RESULT_CREATE_ACCOUNT_ACTIVITY) {
			if (resultCode == RESULT_OK) {
				Log.i(LOGTAG, "finishing on CreateAccount request");
				finish();
			}
		}
	}

	@SuppressWarnings("unused")
	public void cancelAccountCreation(View view)
	{
		mProgressDialog.setMessage(getString(R.string.progress_finishing));
		mProgressDialog.show();
		mCommunicator.getService().terminate();
	}

	@Override
	public void onBackPressed()
	{
		// prevent switch to MainActivity
		moveTaskToBack(true);
	}
}
