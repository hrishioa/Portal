/*
 * Copyright (C) 2014 Rainmaker Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.rainmakerlabs.bleepsample;

import com.rainmakerlabs.bleep.bleepservice.BLEepService;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

public class OptionsActivity extends BleepActivity {
	protected static final String TAG = "OptionsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "oncreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);

		final EditText fieldApiKey = (EditText) findViewById(R.id.fieldApiKey);
		fieldApiKey.setText(BLEepService.getApiKey());

		final EditText fieldUserId = (EditText) findViewById(R.id.fieldUserId);
		fieldUserId.setText(BLEepService.getUserId());

		final ToggleButton toggleRespondNearest = (ToggleButton) findViewById(R.id.toggleRespondNearest);
		toggleRespondNearest.setChecked(BLEepService.getShouldOnlyRespondToNearest());

		final ToggleButton toggleLogNearest = (ToggleButton) findViewById(R.id.toggleLogNearest);
		toggleLogNearest.setChecked(BLEepService.getShouldOnlyLogNearest());
		
		final EditText fieldBeaconOutInt = (EditText) findViewById(R.id.fieldBeaconOutInt);
		fieldBeaconOutInt.setText(Float.toString(BLEepService.getTimeBeforeCountedAsOut()));

		final ToggleButton toggleForceSync = (ToggleButton) findViewById(R.id.toggleForceSync);
		toggleForceSync.setChecked(BLEepService.getForceBmsSync());
		
		final ToggleButton toggleBattReporting = (ToggleButton) findViewById(R.id.toggleBattReporting);
		toggleBattReporting.setChecked(BLEepService.getReportsBatteryLevel());
		
		final ToggleButton toggleAmnesia = (ToggleButton) findViewById(R.id.toggleAmnesia);
		toggleAmnesia.setChecked(BLEepService.getAmnesia());
		
		Button btnSaveAndClose = (Button) findViewById(R.id.btnSaveAndClose);
		btnSaveAndClose.setOnClickListener(new Button.OnClickListener() {
			@Override
		    public void onClick(View v) {
				BLEepService.setNewApiKey(fieldApiKey.getText().toString().trim());
				BLEepService.setNewUserId(fieldUserId.getText().toString().trim());
				BLEepService.setShouldOnlyLogNearest(toggleLogNearest.isChecked());
				BLEepService.setShouldOnlyRespondToNearest(toggleRespondNearest.isChecked());
				BLEepService.setTimeBeforeCountedAsOut(Float.valueOf(fieldBeaconOutInt.getText().toString()));
				BLEepService.setForceBmsSync(toggleForceSync.isChecked());
				BLEepService.setReportsBatteryLevel(toggleBattReporting.isChecked());
				amnesia = toggleAmnesia.isChecked();
				BLEepService.setAmnesia(amnesia);
				OptionsActivity.this.finish();
		    }
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (thisBleepService != null)
			thisBleepService.resumeBleepDiscovery();
	}
    
}
