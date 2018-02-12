package com.example.orkan.activity;

import com.example.orkan.R;
import com.example.orkan.controller.MQTTController;
import com.example.orkan.net.WiFiController;
import com.example.orkan.util.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class APConfigFirstActivity extends BaseActivity {
	
	private Button apbegin_button_wifi_connect;
	private ImageView title_left;
	private BroadcastReceiver mWifiChangedReceiver;
	private TextView title_tx;
	private boolean isEnter = false;
	private TextView apbegin_help_text;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_apconfig_begin);
		
		apbegin_button_wifi_connect = (Button)findViewById(R.id.apbegin_button_wifi_connect);
		title_left = (ImageView) findViewById(R.id.title_left);
		title_tx = (TextView) findViewById(R.id.title_tx);
		title_tx.setText(R.string.title_bund);
		title_left.setImageResource(R.drawable.back);
		title_left.setVisibility(View.VISIBLE);
		apbegin_help_text = (TextView)findViewById(R.id.apbegin_help_text);
		if(Util.AP_MODULE_CONFIG == Util.MODULE_DASK) {
			apbegin_help_text.setText(R.string.hiflying_aplinker_step_1);
		}else {
			apbegin_help_text.setText(R.string.hiflying_aplinker_insert_step_1);
		}
		InitButton();
		apbegin_button_wifi_connect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
			
				if(isEnter) {
					startActivity(new Intent(APConfigFirstActivity.this,
							APConfigSecondTcpActivity.class));
					APConfigFirstActivity.this.finish();
				}else {
					Toast.makeText(getApplicationContext(), getString(R.string.connect_hf), Toast.LENGTH_SHORT).show();
				}
			}
		});
		title_left.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				APConfigFirstActivity.this.finish();
			}
		});
	}
	
	private void InitButton() {
		if(!WiFiController.getInstance(getApplicationContext()).isWifiEnabled()) {
			apbegin_button_wifi_connect.setText(R.string.hiflying_smartlinker_connect_wifi);
			Util.d("wifi noteable");
			isEnter = false;
			//return;
		}else{
			if("HF-LPT120".equals(getSSid())) {
				apbegin_button_wifi_connect.setText(R.string.hiflying_smartlinker_connect_wifi_ok);
				isEnter = true;
				
			}else {
				Util.d("wifi enable,not wifi");
				apbegin_button_wifi_connect.setText(R.string.hiflying_smartlinker_connect_wifi);
			}
		}
		
		mWifiChangedReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
						Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				Log.v("snake", "SSid = " + getSSid());
				if (networkInfo != null && networkInfo.isConnected()) {
					if("HF-LPT120".equals(getSSid())) {
						isEnter = true;
						apbegin_button_wifi_connect.setText(R.string.hiflying_smartlinker_connect_wifi_ok);
					}else {
						isEnter = false;
						Util.d("receiver,not wifi ok");
						apbegin_button_wifi_connect.setText(R.string.hiflying_smartlinker_connect_wifi);
					}
				} else {
					Util.d("receiver,not wifi");
					isEnter = false;
					apbegin_button_wifi_connect.setText(R.string.hiflying_smartlinker_connect_wifi);
				}
			}
		};
		registerReceiver(mWifiChangedReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unregisterReceiver(mWifiChangedReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//获取wifi名字
	private String getSSid() {

		WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
		if (wm != null) {
			WifiInfo wi = wm.getConnectionInfo();
			if (wi != null) {
				String ssid = wi.getSSID();
				if (ssid.length() > 2 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
					return ssid.substring(1, ssid.length() - 1);
				} else {
					return ssid;
				}
			}
		}

		return "";
	}
}
