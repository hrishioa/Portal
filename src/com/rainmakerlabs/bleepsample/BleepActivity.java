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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.rainmakerlabs.bleep.bleepservice.BLEepService;

public class BleepActivity extends Activity {
	protected static final String TAG = "BleepActivity";

	protected static boolean isBackground;
	public static Activity currentBleepActivity;
	Context contextAct = this;
	final static Class<?> rootClass = MainActivity.class;
	protected static boolean isVideoActivityOpen = false;
	
	static BLEepService thisBleepService;
	Intent intForBleepService;
	
	protected static final boolean isTesting = true;//if you put true, it means you are using testing mode (with more feedback). putting false would use deployment mode. applies primarily to launch communication type, as deployment mode hides confirmation dialogs and fail messages as much as possible where intents are badly formed. testing mode also resets all time interval logs by default upon app launch to allow old triggers to appear again
	protected static final boolean isCustomisable = false;//if you put true, it allows end-user to change options that are normally set by developer, e.g. apikey, username, timebeforecountedasout, etc. this is to make it easy for people who don't want to edit and compile source code to test
	protected static boolean amnesia;
	
	void bleepSplashCreate() {
		if (!isCustomisable) {
			BLEepService.setApiKey(contextAct, "SncMDzEDwVk62cUAghqbHfScD9bpKutaUImX");
		} else {
			BLEepService.setApiKey(contextAct);
			amnesia = BLEepService.getAmnesia();
		}

		BLEepService.setAppPackageName("com.rainmakerlabs.bleepsample");

	    //FYI, all these settings are remembered upon startup of the library. So if you set one of them and then want to change it back to default, simply commenting it out isn't enough, you need to change the value you set here.
		//BLEepService.setReportsBatteryLevel(true); //if not called, set to false by default. you should use this if your beacon is one of our BLEep versions that reports battery level. if you are unsure, please ignore this
		//BLEepService.setShouldOnlyLogNearest(true); //if not called, set to false by default. you should use this if you want the app to send walkin/out logs only for the nearest beacon each time beacons are ranged, so user can only be detecting one beacon at a time. otherwise, user can have walked into multiple beacon regions at once
		//BLEepService.setShouldOnlyRespondToNearest(true); //if not called, set to false by default. you should use this if you want the app to respond only to the nearest beacon each time beacons are ranged. otherwise, it will respond to all beacons from nearest to furthest
		//BLEepService.setAutoStart(false); //if not called, set to true by default. you should use this if you want to manually start beacon detection yourself, say upon login. otherwise, it will start looking for beacons automatically
		//BLEepService.setForceBmsSync(true); //if not called, set to false by default. you should use this if you want to make sure users see only the latest triggers each time they launch the app. this will delay bleep detection until app successfully syncs with server
		//BLEepService.setTimeBeforeCountedAsOut(3.0f); //in seconds. if not called, set to 6.0f by default. you should use this if you want to change the time interval it takes for a beacon not to be ranged before the app decides that beacon is out of range
		BLEepService.setServiceStickiness(android.app.Service.START_STICKY); //if not called, set to Service.START_NOT_STICKY by default. you should use this if you want the service to restart after it is killed by the system, and to allow the app to respond to beacons even after it has been killed from multitasking and across reboots. this is useful, but may drain battery faster. if it is sticky, certain options like autoStart and forceBmsSync may not always work as expected because the service is already running
		//BLEepService.setDistEstOffset(1,0,1,0);//this method is to adjust the estimated distance if it differs widely from other phone models. ideally, different Android models should use different values to make it approximate iOS distance estimates to make it more consistent across models. y=mx+c, y being the new adjusted value and x being the original, m being the multiplier and c the static offset. this formula applies to the RSSI value and the estimated distance/accuracy. the order is: RSSI/m, RSSI/c, accuracy/m, accuracy/c. if not called, uses the defaults 1,0,1,0, which means no change to the calculation

		startService(intForBleepService);
	}
	
	void bleepSplashAfterServiceConnection() {
        thisBleepService.ActivityCreated();
        thisBleepService.setUserId("get the user id from your app");//if not called, uses a random alphanumeric string, consistent for the lifetime of your app. this is for beacon detection logs
        if ((!isCustomisable && isTesting) || (isCustomisable && amnesia))
        		thisBleepService.clearTimeIntervalLogsForBeaconDetection();//call this for debug purposes, to reset all detection logs everytime app is launched. for convenience, this includes 1) normal 2) entry 3) live, but instead you can choose to call it separately with the methods below

        //thisBleepService.clearTimeIntervalLogForBeaconDetection();//normal time interval, related to the 'Time Interval' rule condition. we recommend that you don't comment this out for deployment purposes, but have included the method here for your reference if it fits your special use case
        thisBleepService.clearEntryTimeIntervalLogForBeaconDetection();//entry time interval, related to the 'Event Type' rule condition (value 'Entry'). we recommend calling this even for deployment. if the user kills the app while detecting a beacon, they will not be able to detect that beacon's 'entry' upon app relaunch unless this method is called
        thisBleepService.clearLiveTimeIntervalLogForBeaconDetection();//live time interval, related to the 'Response Type' rule condition (value 'Static'). we recommend calling this even for deployment. if the user kills the app while it is waiting for a response from the server to retrieve the live communication data, it will not be able to re-request a response for the same communication for a while upon app relaunch unless this method is called
        
        //thisBleepService.startBleepDiscovery();//use this when you want to manually start scanning for beacons (either after you have manually stopped scanning, or if you have disabled autosync). put this where necessary, it's just here for example
        //thisBleepService.stopBleepDiscovery();//use this to manually stop scanning for beacons, say upon logout. may not work if called too soon after startBleepDiscovery or after it is autostarted. put this where necessary, it's just here for example
        //thisBleepService.pauseBleepDiscovery();//use this when you want to pause reactions to beacons for a while, but don't actually want to stop it. (stopping and starting may take a few seconds or more, but pausing and resuming is practically instant.) detection logs will still work, just that nothing will be reflected in the UI. put this where necessary, it's just here for example
        //thisBleepService.resumeBleepDiscovery();//use this to resume reactions to beacons after pausing. put this where necessary, it's just here for example
	}
	
	void bleepMainStart() {
		IntentFilter statusFilter = new IntentFilter();
		statusFilter.addAction(BLEepService.INTENT_BLEEP_STATUS);
		statusFilter.addAction(BLEepService.INTENT_BLEEP_LIST);
		registerReceiver(statusReceiver, statusFilter);
		if (thisBleepService != null)
			thisBleepService.ActivityStarted();
	}

	void bleepMainStop() {
		unregisterReceiver(statusReceiver);
	}

	void bleepMainDestroy() {
	}

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "oncreate");
		super.onCreate(savedInstanceState);

		intForBleepService = new Intent(contextAct, BLEepService.class);
		onCreateForBleep();
		bindService(intForBleepService, bleepConnection, Context.BIND_AUTO_CREATE);
	}
	
	protected void onCreateForBleep() {
		//ignore. for splash to override
	}
	
    protected ServiceConnection bleepConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BLEepService.BleepBinder binder = (BLEepService.BleepBinder) service;
            thisBleepService = binder.getService();
            
            doAfterServiceConnection();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
    
    protected void doAfterServiceConnection() {
    		//ignore. for splash to override
    }

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		isBackground = false;
		currentBleepActivity = BleepActivity.this;
		//BLEepService.tryToEnableBluetooth();//if you feel diabolical and want to force Bluetooth to turn on whenever the app is foregrounded
	}

	@Override
	protected void onPause() {
		super.onPause();
		isBackground = true;
		if (thisBleepService != null)
			thisBleepService.ActivityPaused();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
    @Override
	protected void onDestroy() {
		unbindService(bleepConnection);
		super.onDestroy();
	}

    protected void displayStatusMsg(String msg) {
    		//ignore. for status displaying activity to override
    }

	BroadcastReceiver statusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getExtras() != null && intent.getExtras().containsKey(BLEepService.INTENT_MSG_NAME)) {
				displayStatusMsg(intent.getStringExtra(BLEepService.INTENT_MSG_NAME));
			}
		}
	};
}