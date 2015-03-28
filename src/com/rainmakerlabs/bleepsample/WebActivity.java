package com.rainmakerlabs.bleepsample;

import com.rainmakerlabs.bleepsample.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebActivity extends Activity {
	WebView webview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		
		webview = (WebView) findViewById(R.id.webview);
		webview.setHorizontalScrollBarEnabled(false);
		webview.setVerticalScrollBarEnabled(false);
		WebSettings websettings = webview.getSettings();
		websettings.setJavaScriptEnabled(true);
		
		String url = getIntent().getExtras().getString("URL");
		
		webview.loadUrl("https://portal-battlehack.herokuapp.com/");
		
		//onBackPressed();
	}
}
