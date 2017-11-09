package com.example.orkan.activity;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orkan.R;
import com.example.orkan.controller.MQTTController;
import com.example.orkan.controller.MQTTController.MQTTFinish;
import com.example.orkan.net.UDPWatcher;
import com.example.orkan.util.Util;

public class APConfigThirdActivity extends BaseActivity implements UDPWatcher,MQTTFinish{
	
	private Button apbegin_button_wifi_connect;
	private Button apbegin_button_red;
	private ImageView title_left;
	private BroadcastReceiver mWifiChangedReceiver;
	private TextView title_tx;
	private boolean isEnter = false;
	
	protected MQTTController mqttController;
	private ProgressDialog mWaitingDialog;
	private ProbeHandler probeHandler = new ProbeHandler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_apconfig_third);
		
		apbegin_button_wifi_connect = (Button)findViewById(R.id.apbegin_button_wifi_connect);
		title_left = (ImageView) findViewById(R.id.title_left);
		title_tx = (TextView) findViewById(R.id.title_tx);
		title_tx.setText(R.string.title_bund);
		title_left.setImageResource(R.drawable.back);
		apbegin_button_red = (Button)findViewById(R.id.apbegin_button_red);
		title_left.setVisibility(View.VISIBLE);
		mWaitingDialog = new ProgressDialog(this);
		mWaitingDialog.setMessage(getString(R.string.hiflying_smartlinker_waiting));
		apbegin_button_wifi_connect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mqttController = MQTTController.getInstance();
				mqttController.stop();
				mqttController.addWatcher(APConfigThirdActivity.this);
				mqttController.setFinishCallBack(APConfigThirdActivity.this);
				mWaitingDialog.show();
				mqttController.start();
				probeHandler.sendEmptyMessageDelayed(1, 10000);
			}
		});
		title_left.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				APConfigThirdActivity.this.finish();
			}
		});
		
		apbegin_button_red.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(APConfigThirdActivity.this, AddDeviceActivity.class));
				finish();
			}
		});
		
		
		
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

	@Override
	public void MQTTFinish() {
		// TODO Auto-generated method stub
		
	}
	
	  private class ProbeHandler extends Handler {
	      public ProbeHandler() {

	      }
	      
	      @Override
	      public void handleMessage(Message msg) {
	    	  	//mqttController.stop();
	    		if (mWaitingDialog.isShowing()) {
	    			mWaitingDialog.dismiss();
	    		}
	    		Toast.makeText(getApplicationContext(), "请重试", Toast.LENGTH_SHORT).show();
	      }
	  }

	@Override
	public void getUDPMessage(int code, byte[] data, int len) {
		
		if (mWaitingDialog.isShowing()) {
			mWaitingDialog.dismiss();
		}
		
		mqttController.stop();
		//Bggin Bind

		FinalHttp fh = new FinalHttp();
		AjaxParams params = new AjaxParams();
		params.put("id", Util.USER_ID);
		params.put("token", Util.USER_TOCKEN);
		Log.v("snake", "ID = " + Util.USER_ID + " mac = " + Util.MQTT_USER_MAC);
		fh.post(Util.URL + "Device/bind/" + Util.MQTT_USER_MAC, params, new AjaxCallBack<String>() {
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				try {
					
					JSONObject jsonData = new JSONObject(t);
					int code = jsonData.getInt("code");
					if (code == 1) {
						probeHandler.removeMessages(1);
						Toast.makeText(getApplicationContext(), "设备绑定成功", Toast.LENGTH_SHORT).show();
						Util.START_FROM_LOGIN = 0;
						startActivity(new Intent(APConfigThirdActivity.this, DeviceListActivity.class));

						finish();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				Toast.makeText(getApplicationContext(), "设备绑定失败", Toast.LENGTH_SHORT).show();
			}
		});
		
		
	}
	

}
