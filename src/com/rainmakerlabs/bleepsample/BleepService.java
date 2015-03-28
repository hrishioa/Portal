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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.applidium.shutterbug.utils.ShutterbugManager;
import com.applidium.shutterbug.utils.ShutterbugManager.ShutterbugManagerListener;
import com.rainmakerlabs.bleep.bleepservice.BLEepService;
import com.rainmakerlabs.bleep.command.APIKeyDefineCommand;
import com.rainmakerlabs.bleep.models.Bleep;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;

public class BleepService extends Service {

	boolean oldcodeon = false;
	private static final String TAG = "BleepService";
	BLEepService thisBleepService;
	AlertDialog BLEalert;
	MediaPlayer mediaPlayer;

	public BleepService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean checkIfClosed() {
		return BleepActivity.currentBleepActivity==null;
	}

	@Override
	public void onCreate() {
		Intent intForBleepService = new Intent(this, BLEepService.class);
		bindService(intForBleepService, bleepConnection, Context.BIND_AUTO_CREATE);
	}

	protected ServiceConnection bleepConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			BLEepService.BleepBinder binder = (BLEepService.BleepBinder) service;
			thisBleepService = binder.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		String actionName = intent.getStringExtra("actionName");
		if (intent==null || intent.getExtras() == null) {//just removing these checks
		} else if (actionName.equalsIgnoreCase(BLEepService.INTENT_BLEEP_PROCESS) && intent.getExtras().containsKey(BLEepService.INTENT_BLEEP_PASSED_LIST)) {
			ArrayList<Bleep> bleeps = (ArrayList<Bleep>)intent.getExtras().getSerializable(BLEepService.INTENT_BLEEP_PASSED_LIST);
			for (final Bleep bleep : bleeps) {
				String msgType = bleep.getType();
				String atts = bleep.getAtts();
				try {
					JSONObject objMsg = new JSONObject(atts);
					//thisBleepService.addExtraLog(bleep, "FEEDBACK", "good");//use this when you want to send a custom log to BMS. for instance, you could display a feedback dialog, and ask users to give a rating, and BMS will help you collate the information. put this where necessary, it's just here for example
					Log.d(TAG,"Message Received of Type: "+msgType);
					if (msgType.equalsIgnoreCase("image")) {
						final String strImgUrl = objMsg.optString(APIKeyDefineCommand.MSG_IMG);
						final String adAspect = objMsg.optString(APIKeyDefineCommand.MSG_IMGASP);
						String strImgMsgTemp = objMsg.optString(APIKeyDefineCommand.MSG_NOTIF);
						//putting the default message above would only work for missing key, not in case of empty string. empty string will prevent the notification, so fix below
						if (strImgMsgTemp.equalsIgnoreCase(""))
							strImgMsgTemp = "No Message";
						final String strImgMsg = strImgMsgTemp;
						if (checkIfClosed()) {
							if(oldcodeon)
								localNotification("", strImgMsg, 0);
							continue;
						}
						if(MainActivity.adlib.get(strImgMsg)==null && !strImgMsg.equalsIgnoreCase("No Message"))
							Log.d(TAG, "image url start download for key: "+strImgMsg+", url = " + strImgUrl);
						else	
						{
							Log.d(TAG,"Image exists, or no message, not downloading..., key: "+strImgMsg);
							continue;
						}



						ShutterbugManager.getSharedImageManager(BleepActivity.currentBleepActivity).download(strImgUrl, new ShutterbugManagerListener() {
							@Override
							public void onImageSuccess(ShutterbugManager imageManager, Bitmap bitmap, String url) {
								Log.d(TAG, "image url end download " + strImgUrl);
								if (strImgUrl.equalsIgnoreCase(AdDialog.getCurrentAdUrl()))
									return;
								if (AdDialog.howManyAdDialogsShowing == 1) {
									AdDialog.closeOnlyAdDialog();
								}
								final Bitmap bitmap2 = bitmap;

								//Custom code
								MainActivity.adlib.put(strImgMsg, bitmap);
								Log.d("Portal","Added an image. Size is now "+MainActivity.adlib.size());
								Log.i("Portal","Image added for key "+strImgMsg);

								if(MainActivity.gal_size<MainActivity.adlib.size() && MainActivity.myGallery!=null) { //new image has been added and the layout is initialized
									LinearLayout layout = new LinearLayout(getApplicationContext());
									layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1000));
									layout.setGravity(Gravity.CENTER);

									ImageView imageview = new ImageView(getApplicationContext());
									imageview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
									imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
									imageview.setImageBitmap(bitmap);

									layout.addView(imageview);
									MainActivity.myGallery.addView(layout);
									MainActivity.gal_size++;
								}

								if(oldcodeon) {
									BleepActivity.currentBleepActivity.runOnUiThread(new Runnable() {
										public void run() {
											if (BleepActivity.isBackground)
												localNotification("", strImgMsg, 0);
											AdDialog.showAdsDialog(BleepActivity.currentBleepActivity, bitmap2, strImgUrl, adAspect);
										}
									});
								}
							}
							@Override
							public void onImageFailure(ShutterbugManager imageManager, String url) {
								thisBleepService.eraseTriggerLog(bleep);//to cancel trigger log when trigger is cancelled
							}
						});
					} else if (oldcodeon == false) {//don't run anything else
					} else if (msgType.equalsIgnoreCase("alert") && oldcodeon) {

						// show alert message
						if (checkIfClosed() || BleepActivity.isBackground) {
							localNotification(objMsg.optString(APIKeyDefineCommand.MSG_TITLE), objMsg.optString(APIKeyDefineCommand.MSG_CONTENT), 1);
						} else {
							showAlert(objMsg.optString(APIKeyDefineCommand.MSG_TITLE), objMsg.optString(APIKeyDefineCommand.MSG_CONTENT));
						}
					} else if (msgType.equalsIgnoreCase("launch")) {
						String intentAction = objMsg.optString(APIKeyDefineCommand.MSG_APP_INTENT);
						String intentUri = objMsg.optString(APIKeyDefineCommand.MSG_APP_URI);
						String intentType = "";
						String intentExtras = objMsg.optString(APIKeyDefineCommand.MSG_APP_EXTRAS);
						String cfmMsg = objMsg.optString(APIKeyDefineCommand.MSG_APP_CFM);
						String failMsg = objMsg.optString(APIKeyDefineCommand.MSG_APP_FAIL);
						processTypeLaunch(intentAction, intentUri, intentType, intentExtras, cfmMsg, failMsg, 2);
					} else if (msgType.equalsIgnoreCase("url")) {
						String intentAction = Intent.ACTION_VIEW;
						String intentUri = objMsg.optString(APIKeyDefineCommand.MSG_MEDIA_URL);
						String intentType = "";
						String intentExtras = "";
						String cfmMsg = objMsg.optString(APIKeyDefineCommand.MSG_APP_CFM);
						String failMsg = objMsg.optString(APIKeyDefineCommand.MSG_APP_FAIL);
						processTypeLaunch(intentAction, intentUri, intentType, intentExtras, cfmMsg, failMsg, 3);
					} else if (msgType.equalsIgnoreCase("webview")) {
						String intentAction = Intent.ACTION_VIEW;
						String intentUri = objMsg.optString(APIKeyDefineCommand.MSG_MEDIA_URL);
						String intentType = "";
						String intentExtras = "";
						String cfmMsg = objMsg.optString(APIKeyDefineCommand.MSG_NOTIF);
						if (cfmMsg.equalsIgnoreCase(""))
							cfmMsg = "View webpage?";
						String failMsg = "";
						processTypeLaunch(intentAction, intentUri, intentType, intentExtras, cfmMsg, failMsg, 4);
					} else if (msgType.equalsIgnoreCase("video")) {
						String strVidUrl = objMsg.optString(APIKeyDefineCommand.MSG_MEDIA_URL);
						Intent vidIntent = new Intent(this, VideoActivity.class);
						vidIntent.putExtra("url", strVidUrl);
						String notifMsg = objMsg.optString(APIKeyDefineCommand.MSG_NOTIF);
						if (notifMsg.equalsIgnoreCase(""))
							notifMsg = "Play video?";
						if (!checkIfClosed() && BleepActivity.isVideoActivityOpen) {
							thisBleepService.eraseTriggerLog(bleep);//to cancel trigger log when trigger is cancelled
						} else if (checkIfClosed() || BleepActivity.isBackground) {
							vidIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							vidIntent.setAction(Intent.ACTION_MAIN);
							vidIntent.addCategory(Intent.CATEGORY_LAUNCHER);
							localNotification("", notifMsg, vidIntent, "Video opening failed!", 5);
						} else {
							BleepActivity.currentBleepActivity.startActivity(vidIntent);
						}
					} else if (msgType.equalsIgnoreCase("audio")) {
						String url = objMsg.optString(APIKeyDefineCommand.MSG_MEDIA_URL);
						mediaPlayer = new MediaPlayer();
						mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
						try {
							mediaPlayer.setDataSource(url);
						} catch (IOException e) {
							e.printStackTrace();
						}
						mediaPlayer.prepareAsync();
						mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
							@Override
							public void onPrepared(MediaPlayer mp) {
								mp.start();
							}
						});
						mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
							@Override
							public boolean onError(MediaPlayer mp, int what, int extra) {
								return false;
							}
						});
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			//		} else if (actionName.equalsIgnoreCase(BLEepService.INTENT_BLEEP_EXIT) && intent.getExtras().containsKey(BLEepService.INTENT_MSG_NAME)) {
			//			HashMap<String, Object> beaconOutInfo = (HashMap<String, Object>)intent.getSerializableExtra(BLEepService.INTENT_MSG_NAME);	
			//			//Log.d("debug!","debug! this beacon just went out "+beaconOutInfo.get("uuid")+" "+beaconOutInfo.get("major")+" "+beaconOutInfo.get("minor")+" "+beaconOutInfo.get("tag")+" "+beaconOutInfo.get("beaconID"));
		} else if (actionName.equalsIgnoreCase(BLEepService.INTENT_BLEEP_STATE) && intent.getExtras().containsKey(BLEepService.INTENT_MSG_NAME)) {
			int beaconState = intent.getIntExtra(BLEepService.INTENT_MSG_NAME, 99);	
		}

		return START_NOT_STICKY;
	}



	private void processTypeLaunch(final String intentAction, final String intentUri, final String intentType, final String intentExtras, final String cfmMsg, String failMsgTemp, final int notifyId) {

		//if you wish, use a default generic fail message whenever undefined. recommended to use this when testing
		if (failMsgTemp.equalsIgnoreCase(""))
			failMsgTemp = "Opening URL failed!";
		final String failMsg = failMsgTemp;

		Pattern pattern = Pattern.compile("\\[ASK:(.*?)\\]");
		final Matcher matcher = pattern.matcher(intentUri);
		int count = 0;
		String askMsg = "";
		while (matcher.find()) {
			count++;
			askMsg = matcher.group(1);
		}
		if (count==1) {
			//confirm alert with text input
			if (BleepActivity.isBackground)
				localNotification("", cfmMsg, notifyId);
			final EditText input = new EditText(BleepActivity.currentBleepActivity);
			DialogInterface.OnClickListener posFunc = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String userInput = input.getText().toString();
					String intentUri2 = matcher.replaceFirst(userInput);
					Intent launchIntent = createIntent(intentAction, intentUri2, intentType, intentExtras);
					startIntent(launchIntent, failMsg, true, cfmMsg, notifyId);
				}
			};
			DialogInterface.OnClickListener negFunc = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Do nothing.
				}
			};
			showAlertDialog(cfmMsg, askMsg, "OK", posFunc, "Cancel", negFunc, input);
		} else if (!cfmMsg.equalsIgnoreCase("")) {
			//confirm alert, message only, no text input
			final Intent launchIntent = createIntent(intentAction, intentUri, intentType, intentExtras);
			if (!BleepActivity.isTesting && !testIntent(launchIntent)) {
				return;
			}
			if (BleepActivity.isBackground) {
				localNotification("", cfmMsg, launchIntent, failMsg, notifyId);
			} else {
				DialogInterface.OnClickListener posFunc = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						startIntent(launchIntent, failMsg, true, cfmMsg, notifyId);
					}
				};
				DialogInterface.OnClickListener negFunc = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				};
				showAlertDialog("", cfmMsg, "OK", posFunc, "Cancel", negFunc, null);
			}
		} else {
			//no confirm alert
			Intent launchIntent = createIntent(intentAction, intentUri, intentType, intentExtras);
			if (!BleepActivity.isTesting && !testIntent(launchIntent)) {
				return;
			}
			String tmpCfmMsg = "You have been sent a URL to view. Open?";
			if (BleepActivity.isBackground) {
				localNotification("", tmpCfmMsg, launchIntent, failMsg, notifyId);
			} else {
				startIntent(launchIntent, failMsg, true, tmpCfmMsg, notifyId);
			}
		}
	}

	private Intent createIntent(String intentAction, String intentUri, String intentType, String intentExtras) {
		Intent launchIntent;
		if (intentUri.equalsIgnoreCase("")) {
			launchIntent = getPackageManager().getLaunchIntentForPackage(intentAction);
			launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		} else {
			launchIntent = new Intent(intentAction, Uri.parse(intentUri));
		}
		if (!intentType.equalsIgnoreCase(""))
			launchIntent.setType(intentType);
		if (!intentExtras.equalsIgnoreCase("")) {
			try {
				JSONObject intentExtrasDict = new JSONObject(intentExtras);
				Iterator<?> keys = intentExtrasDict.keys();
				while( keys.hasNext() ){
					String key = (String)keys.next();
					launchIntent.putExtra(key, intentExtrasDict.get(key).toString());
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return launchIntent;
	}

	private boolean testIntent(Intent launchIntent) {
		if (launchIntent == null || launchIntent.resolveActivity(getPackageManager()) == null) {
			Log.d(TAG,"testing intent: failed! "+launchIntent);
			return false;
		}
		return true;
	}

	private void startIntent(Intent launchIntent, String failMsg, boolean isWithConfirmation, String cfmMsg, int notifyId) {
		if (testIntent(launchIntent)) {
			try {
				if (!checkIfClosed())
					BleepActivity.currentBleepActivity.startActivity(launchIntent);
				else
					localNotification("", cfmMsg, launchIntent, failMsg, notifyId);
			} catch (Exception e) {
				e.printStackTrace();
				reflectIntentFailure(failMsg, launchIntent, isWithConfirmation);
			}
		} else {
			reflectIntentFailure(failMsg, launchIntent, isWithConfirmation);
		}
	}

	private void reflectIntentFailure(String failMsg, Intent intent, boolean isWithConfirmation) {
		Log.d(TAG, "opening intent: failed! "+intent);
		if (!BleepActivity.isTesting && !isWithConfirmation)
			return;
		if (!checkIfClosed() && !BleepActivity.isBackground)
			showAlert("", failMsg);
		else
			localNotification("", failMsg, 99);
	}

	private void showAlert(String title, String msg) {
		showAlertDialog(title, msg, "OK", null, null, null, null);
	}

	private void showAlertDialog(String title, String msg, String posText, DialogInterface.OnClickListener posFunc, String negText, DialogInterface.OnClickListener negFunc, EditText input) {
		if (BLEalert != null && BLEalert.isShowing()) {
			BLEalert.cancel();
		}
		//if (BLEalert == null || !BLEalert.isShowing()) {
		if (title == null)
			title = "";
		if (msg == null)
			msg = "";
		if (title.equalsIgnoreCase("") && msg.equalsIgnoreCase(""))
			return;
		AlertDialog.Builder builder = new AlertDialog.Builder(BleepActivity.currentBleepActivity);
		builder.setTitle(title);
		builder.setMessage(msg);
		if (input != null)
			builder.setView(input);
		if (posText != null && !posText.equalsIgnoreCase(""))
			builder.setPositiveButton(posText, posFunc);
		if (negText != null && !negText.equalsIgnoreCase(""))
			builder.setNegativeButton(negText, negFunc);
		BLEalert = builder.create();
		BLEalert.show();
		//}
	}

	private void localNotification(String title, String message, int notifyId) {
		localNotification(title, message, null, "", notifyId);
	}

	private void localNotification(String title, String message, Intent notificationIntent, String failMsg, int notifyId) {
		if (!testIntent(notificationIntent)) {
			//if (!BleepActivity.isTesting)
			//	return;
			notificationIntent = null;
			if (!failMsg.equalsIgnoreCase(""))
				message = failMsg;
		}
		if (notificationIntent == null) {
			notificationIntent = new Intent(this, BleepActivity.rootClass);
			// set intent so it does not start a new activity
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			notificationIntent.setAction(Intent.ACTION_MAIN);
			notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		}
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		if (message==null || message.equalsIgnoreCase(""))
			return;
		if (title==null || title.equalsIgnoreCase(""))
			title = getString(R.string.app_name);

		NotificationCompat.Builder builder =  
				new NotificationCompat.Builder(this)  
		.setSmallIcon(R.drawable.ic_launcher)
		.setTicker(message)
		.setWhen(System.currentTimeMillis())
		.setAutoCancel(true)
		.setContentTitle(title)  
		.setContentText(message)
		.setContentIntent(contentIntent)
		.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);  

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
		notificationManager.notify(notifyId, builder.build());
	}

}
