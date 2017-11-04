package com.example.orkan.activity;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.orkan.R;
import com.example.orkan.controller.MQTTController;
import com.example.orkan.controller.MQTTController.MQTTFinish;
import com.example.orkan.net.FrUDPConnectListener;
import com.example.orkan.net.FrUDPSocketServer;
import com.example.orkan.net.UDPWatcher;
import com.example.orkan.net.WiFiController;
import com.example.orkan.util.Util;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

public class APConfigSecondActivity extends BaseActivity implements UDPWatcher,MQTTFinish{


	private TextView title_tx;
	private ImageView title_im;
	private ImageView title_left;

	private EditText mSsidEditText;
	private EditText mPasswordEditText;
	private Button mStartButton;
	
	
	private LinearLayout text_li_ssid;
	private LinearLayout text_li_password;
	private TextView help_text;
	private TextView error_tex;

	private FrUDPSocketServer socket;
	
	private ProgressDialog mWaitingDialog;
	//protected MQTTController mqttController;
	private ProbeHandler probeHandler = new ProbeHandler();
	private Button button_green;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_apconfig);
		text_li_ssid = (LinearLayout)findViewById(R.id.text_li_ssid);
		text_li_password = (LinearLayout)findViewById(R.id.text_li_password);
		help_text = (TextView)findViewById(R.id.help_text);
		title_tx = (TextView) findViewById(R.id.title_tx);
		error_tex = (TextView)findViewById(R.id.error_text);
		title_tx.setText(R.string.title_bund);
		title_left = (ImageView) findViewById(R.id.title_left);
		title_left.setImageResource(R.drawable.back);
		title_left.setVisibility(View.VISIBLE);
		button_green = (Button)findViewById(R.id.button_green);
		title_left.setOnClickListener(click);
		button_green.setOnClickListener(click);
		//UI
		mSsidEditText = (EditText) findViewById(R.id.editText_hiflying_smartlinker_ssid);
		mPasswordEditText = (EditText) findViewById(R.id.editText_hiflying_smartlinker_password);
		mStartButton = (Button) findViewById(R.id.button_hiflying_smartlinker_start);
		mSsidEditText.setHint("请输入WIFI账号");
		mPasswordEditText.setHint("请输入WIFI密码");
		//mSsidEditText.setText("302");
		//mPasswordEditText.setText("chenxiaoxia");
		mStartButton.setOnClickListener(click);
		
		
		mWaitingDialog = new ProgressDialog(this);
		mWaitingDialog.setMessage(getString(R.string.hiflying_smartlinker_waiting));
		socketInit();

//		mqttController = MQTTController.getInstance();
//		mqttController.stop();
//		mqttController.addWatcher(this);
//		mqttController.setFinishCallBack(this);
		//mqttController.start();

	}
	
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
	}
	
	private View.OnClickListener click = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			
			int id = arg0.getId();
			if(id == R.id.title_left){
				APConfigSecondActivity.this.finish();
			}else if(id == R.id.button_hiflying_smartlinker_start){
				//TODO
				if(WiFiController.getInstance(getApplicationContext()).isWifiEnabled()){
					socket.startUDPSocketThread();
					probeHandler.sendEmptyMessageDelayed(2, 5000);
					mWaitingDialog.show();
					
				}else{
					Toast.makeText(getApplicationContext(), "请连接WIFI", Toast.LENGTH_SHORT).show();
				}
				
			}else if(id == R.id.button_green) {
				startActivity(new Intent(APConfigSecondActivity.this, AddDeviceActivity.class));
				finish();
			}
		}
	};
	
	  private class ProbeHandler extends Handler {
	      public ProbeHandler() {
	      
	      }
	      
	      @Override
	      public void handleMessage(Message msg) {
	      		if(msg.what == 1){
	      			mWaitingDialog.dismiss();
	      			socket.stopUDPSocketThread();
	      			mStartButton.setVisibility(View.GONE);
	      			text_li_ssid.setVisibility(View.GONE);
	      			text_li_password.setVisibility(View.GONE);
	      			help_text.setVisibility(View.GONE);
	      			error_tex.setVisibility(View.VISIBLE);
	      			button_green.setVisibility(View.VISIBLE);
	      			//Toast.makeText(getApplicationContext(), "设备绑定wifi超时", Toast.LENGTH_SHORT).show();
	      			//TODO
	      			
	      		}else if(msg.what == 2){
	      			
	      			String s = ">>" + mSsidEditText.getText().toString().trim() +  " " + mPasswordEditText.getText().toString().trim();
	      			socket.sendSocketData(s);
	      			Log.v("snake", "send Data : " + s);
	      			probeHandler.sendEmptyMessageDelayed(1, 10000);
	      		}else if(msg.what == 3) {
	      			//Wifi and 4g
	      			Log.v("snake", "mqttstart");
	      		//	mqttController.start();
	      			startActivity(new Intent(APConfigSecondActivity.this, APConfigThirdActivity.class));
	      			finish();
	      		}
	      }
	  }
	
	private String macString = "";
	private void socketInit(){
		
		socket = new FrUDPSocketServer("10.10.100.254", 20001, 20000);
		socket.addUDPListener(new FrUDPConnectListener() {
			
			@Override
			public void reportReceivedPacket(String receivedString) {
				if(receivedString.contains("<<mac")){
					macString = receivedString.replace("<<mac=", "").substring(0, 12).trim();
					//TODO
					Log.v("snake", "receive mac : " + macString);
					Util.MQTT_USER_MAC = macString;
					probeHandler.removeMessages(1);
					probeHandler.sendEmptyMessageDelayed(3, 1000);
					socket.stopUDPSocketThread();
	    
				}else {
				   if(receivedString.contains("<<set failed")){
					   Log.v("snake", "receive error : " + receivedString);
//						if (mWaitingDialog.isShowing()) {
//							mWaitingDialog.dismiss();
					//}
					//probeHandler.removeMessages(1);
					socket.stopUDPSocketThread();
				}
				}
			}
			
			@Override
			public void onSuccessConnect() {
				
			}
			
			@Override
			public void onDistoryConnect() {
				
				
			}

			@Override
			public void reportSendError() {
				socket.stopUDPSocketThread();
				if (mWaitingDialog.isShowing()) {
					mWaitingDialog.dismiss();
				}
				//probeHandler.removeMessages(1);
				//Toast.makeText(getApplicationContext(), "网络异常", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void getUDPMessage(int code, byte[] data, int len) {
		//TODO
		Log.v("snake", "getMessage");
		probeHandler.removeMessages(1);
		if (mWaitingDialog.isShowing()) {
			mWaitingDialog.dismiss();
		}
		socket.stopUDPSocketThread();
		//mqttController.stop();
		//Bggin Bind

		FinalHttp fh = new FinalHttp();
		AjaxParams params = new AjaxParams();
		params.put("id", Util.USER_ID);
		params.put("token", Util.USER_TOCKEN);

		fh.post(Util.URL + "Device/bind/" + Util.MQTT_USER_MAC, params, new AjaxCallBack<String>() {
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				try {
					
					JSONObject jsonData = new JSONObject(t);
					int code = jsonData.getInt("code");
					Log.v("snake", "band success Code = " + code);
					if (code == 1) {
						Toast.makeText(getApplicationContext(), "设备绑定成功", Toast.LENGTH_SHORT).show();
						Util.START_FROM_LOGIN = 0;
						startActivity(new Intent(APConfigSecondActivity.this, DeviceListActivity.class));

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
		
		//startActivity(new Intent(APConfigActivity.this,DeviceListActivity.class));
			
	//	finish();
		
	}

	@Override
	public void MQTTFinish() {
		// TODO Auto-generated method stub
		
	}

}
