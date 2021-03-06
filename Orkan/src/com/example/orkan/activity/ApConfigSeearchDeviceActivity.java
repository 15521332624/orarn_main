package com.example.orkan.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.example.orkan.R;
import com.example.orkan.dialog.MessageDialog;
import com.example.orkan.model.MQTTRevMsg;
import com.example.orkan.net.SmartUDPSocketServer;
import com.example.orkan.net.WiFiController;
import com.example.orkan.util.Util;
import com.hiflying.smartlink.ISmartLinker;

public class ApConfigSeearchDeviceActivity extends BaseActivity implements
		SmartUDPSocketServer.FindDevice {

	public static final String EXTRA_SMARTLINK_VERSION = "EXTRA_SMARTLINK_VERSION";

	private static final String TAG = "CustomizedActivity";
	//test for git
	TextView title_tx;
	ImageView title_im;
	ImageView title_left;
	protected Button button_smartconfig;
	protected Button button_udp;
	protected Button button_ap_add;
	WiFiController wiFiController;
	protected Handler mViewHandler = new Handler();
	protected ProgressDialog mWaitingDialog;
	private SmartUDPSocketServer udpServer;
	Handler timeouthandler = new Handler();
	Runnable timeoutrunnable;
	private ProbeHandler probeHandler = new ProbeHandler();
	private ListView deviceListView;
	private List<Map<String, String>> devicelist = new ArrayList<Map<String, String>>(); // 定义显示的内容包
	private SimpleAdapter deviceSimpleAdapter = null;
	private int searchNum = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		udpServer = new SmartUDPSocketServer();
		udpServer.setFindDevie(this);
		timeoutrunnable = new Runnable() {
			@Override
			public void run() {
				
				try {
					if (devicelist.size() < 1) {
						searchNum++;
						if(searchNum < 3){
							devicelist.clear();
							udpServer.startUDPSocketThread();
							udpServer.sendDiscover();
							udpServer.sendDiscover();
							udpServer.sendDiscover();
							timeOutControl();
							return;
						}
						mWaitingDialog.dismiss();
						searchNum = 0;	
						MessageDialog msgDialog = new MessageDialog(
								ApConfigSeearchDeviceActivity.this,
								getString(R.string.device_no_search));
						msgDialog.show();
						return;
					}
					mWaitingDialog.dismiss();
					searchNum = 0;
					final AlertDialog alertDialog = new AlertDialog.Builder(
							ApConfigSeearchDeviceActivity.this).create();

					alertDialog.show();

					Window window = alertDialog.getWindow();
					window.setContentView(R.layout.dialog_list);
					deviceListView = (ListView) window
							.findViewById(R.id.dialog_list);
					TextView dialog_title = (TextView) window
							.findViewById(R.id.dialog_title);
					dialog_title.setText(R.string.select_device);

					deviceSimpleAdapter = new SimpleAdapter(
							ApConfigSeearchDeviceActivity.this, devicelist,
							R.layout.list_dialog, new String[] { "device" } // Map中的key的名称
							, new int[] { R.id.list_tx }); // 是data_list.xml中定义的组件的资源ID
					deviceListView.setAdapter(deviceSimpleAdapter);
					deviceListView
							.setOnItemClickListener(new OnItemClickListener() {
								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {
									// TODO Auto-generated method stub
									alertDialog.dismiss();
									beginAll();
									String mac = devicelist.get(position)
											.get("mac").toLowerCase();

									Util.MQTT_USER_MAC = mac;

									FinalHttp fh = new FinalHttp();
									AjaxParams params = new AjaxParams();

									params.put("id", Util.USER_ID);
									params.put("token", Util.USER_TOCKEN);

									fh.post(Util.URL + "Device/bind/"
											+ Util.MQTT_USER_MAC, params,
											new AjaxCallBack<String>() {
												@Override
												public void onSuccess(String t) {
													super.onSuccess(t);
													try {
														Util.d(t);
														JSONObject jsonData = new JSONObject(
																t);
														int code = jsonData
																.getInt("code");

														Util.d("code  " + code);
														if (code == 1) {
															Toast.makeText(
																	getApplicationContext(),
																	getString(R.string.band_success),
																	Toast.LENGTH_SHORT)
																	.show();
															cancelAll();
															Message msg = new Message();
															msg.what = 1;

															probeHandler
																	.sendMessage(msg);
														} else {
															cancelAll();
															Toast.makeText(
																	getApplicationContext(),
																	jsonData.getString("msg"),
																	Toast.LENGTH_SHORT)
																	.show();
														}
													} catch (JSONException e) {
														// TODO Auto-generated
														// catch block
														e.printStackTrace();
														cancelAll();
													}

												}

												@Override
												public void onFailure(
														Throwable t,
														int errorNo,
														String strMsg) {
													super.onFailure(t, errorNo,
															strMsg);
													Toast.makeText(
															getApplicationContext(),
															getString(R.string.band_fail),
															Toast.LENGTH_SHORT)
															.show();
													cancelAll();
												}
											});

								}

							});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		setContentView(R.layout.activity_apconfig_dialog_list);
		wiFiController = WiFiController.getInstance(this);

		title_tx = (TextView) findViewById(R.id.title_tx);
		title_tx.setText(R.string.title_bund);
		title_left = (ImageView) findViewById(R.id.title_left);
		title_left.setImageResource(R.drawable.back);
		title_left.setVisibility(View.GONE);
		title_left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ApConfigSeearchDeviceActivity.this.finish();
			}
		});

		button_smartconfig = (Button) findViewById(R.id.button_smartconfig);

		button_smartconfig.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(ApConfigSeearchDeviceActivity.this,
						SmartConfigActivity.class));
				ApConfigSeearchDeviceActivity.this.finish();

			}
		});

		button_udp = (Button) findViewById(R.id.button_udp);

		button_udp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (wiFiController.isWifiEnabled()) {

					timeOutControl();
					devicelist.clear();
					mWaitingDialog.show();
					udpServer.startUDPSocketThread();
					udpServer.sendDiscover();
					udpServer.sendDiscover();
					udpServer.sendDiscover();

				} else {

					MessageDialog msgDialog = new MessageDialog(
							ApConfigSeearchDeviceActivity.this, getString(R.string.connect_wifi));
					msgDialog.show();
				}

			}
		});

		button_ap_add = (Button) findViewById(R.id.button_ap_add);
		button_ap_add.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(ApConfigSeearchDeviceActivity.this,
						APConfigFirstActivity.class));
				ApConfigSeearchDeviceActivity.this.finish();
			}
		});

		mWaitingDialog = new ProgressDialog(this);
		mWaitingDialog
				.setMessage(getString(R.string.hiflying_smartlinker_waiting));
		mWaitingDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
				getString(android.R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		mWaitingDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {

			}
		});
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
		udpServer.stopUDPSocketThread();
		udpServer = null;
	}

	@Override
	public void FindDevice(String name, String ip, String mac) {
		// TODO Auto-generated method stub

		Map<String, String> map = new HashMap<String, String>(); // 定义Map集合，保存每一行数据
		map.put("device", name + ":" + mac);
		map.put("mac", mac);
		for (Map<String, String> m : devicelist) {
			String tm = m.get("mac");
			if (tm.equals(mac)) {
				return;
			}
		}

		devicelist.add(map); // 保存了所有的数据行

	}
    
	protected void timeOutControl() {
		timeouthandler.postDelayed(timeoutrunnable, 3000);
	}

	protected class ProbeHandler extends Handler {
		public ProbeHandler() {

		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				Util.START_FROM_LOGIN = 0;
				startActivity(new Intent(ApConfigSeearchDeviceActivity.this,
						DeviceListActivity.class));

				ApConfigSeearchDeviceActivity.this.finish();
			} else if (msg.what == 2) {

			}
		}
	}

}
