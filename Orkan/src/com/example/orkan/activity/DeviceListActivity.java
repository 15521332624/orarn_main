package com.example.orkan.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orkan.R;
import com.example.orkan.adapter.DeviceListAdapter;
import com.example.orkan.adapter.TimingListAdapter;
import com.example.orkan.controller.MQTTController;
import com.example.orkan.dialog.MessageDialog;
import com.example.orkan.net.UDPSocketServer;
import com.example.orkan.net.UDPWatcher;
import com.example.orkan.third.kprogresshud.KProgressHUD;
import com.example.orkan.util.GetSharedData;
import com.example.orkan.util.MQTTUtil;
import com.example.orkan.util.Util;

public class DeviceListActivity extends BaseActivity {
	ListView devicelistView;
	DeviceListAdapter adapter;
	AlertDialog loginMsgDialog;
	TextView title_tx;
	ImageView title_im;
	ImageView title_left;
	KProgressHUD hud;
	// TextView timing_delete;
	boolean isTiming = false;
	int[] device_type_pic = { R.drawable.orkan, R.drawable.xiangfan };
	List<Map<String, Object>> deviceList = new ArrayList<Map<String, Object>>();;
	protected UDPSocketServer mProbeSocketDiscover;
	// private ProbeHandler probeHandler = new ProbeHandler();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_devicelist);
		init();

	}

	private void init() {
		loginMsgDialog = new AlertDialog.Builder(this).create();
		hud = KProgressHUD.create(this).setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
		title_tx = (TextView) findViewById(R.id.title_tx);
		title_tx.setText(R.string.title_devicelist);
		title_im = (ImageView) findViewById(R.id.title_im);
		title_im.setImageResource(R.drawable.timing_add);
		title_im.setVisibility(View.VISIBLE);
		title_im.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// startActivity(new Intent(DeviceListActivity.this,AddDeviceActivity.class));
				startActivity(new Intent(DeviceListActivity.this, APConfigTypeActivity.class));
				DeviceListActivity.this.finish();
			}
		});
		title_left = (ImageView) findViewById(R.id.title_left);
		title_left.setImageResource(R.drawable.refresh);
		title_left.setVisibility(View.VISIBLE);
		title_left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO
				deviceList.clear();
				adapter.notifyDataSetChanged();
				beginAll();
				getDeviceList();
			}
		});

		devicelistView = (ListView) findViewById(R.id.device_list);

		// 点击事件
		// 点击事件
		devicelistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

				Util.MQTT_DEVICE_NAME = (String) deviceList.get(position).get("deviceName");
				Util.MQTT_USER_MAC = (String) deviceList.get(position).get("mac");
				Util.MQTT_DEVICE_ID = (String) deviceList.get(position).get("id");
				startActivity(new Intent(DeviceListActivity.this, MainActivity.class));
				DeviceListActivity.this.finish();
			}
		});


		initData();
		deviceList.clear();
		beginAll();
		getDeviceList();

	}

	private void initData() {

		String[] from = { "deviceName", "mac", "state", "type" };
		int[] to = { R.id.device_name_txt, R.id.device_mac_txt, R.id.device_online_txt, R.id.device_list_im };
		Util.d("devicelist size " + deviceList.size());
		// HashMap<String, Object> dmap = (HashMap<String, Object>)deviceList.get(0);
		// Util.d("state "+ dmap.get("state"));
		adapter = new DeviceListAdapter(this, deviceList, R.layout.list_device, from, to);

		// 设置适配器
		devicelistView.setAdapter(adapter);

	}

	private void getDeviceList() {

		FinalHttp fh = new FinalHttp();
		AjaxParams params = new AjaxParams();
		params.put("id", Util.USER_ID);
		params.put("token", Util.USER_TOCKEN);
		fh.post(Util.URL + "Device/getList", params, new AjaxCallBack<String>() {
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				try {
					Util.d("getDeviceList  " + t);
					JSONObject jsonData = new JSONObject(t);
					int code = jsonData.getInt("code");
					if (code == 1) {
						JSONArray data = jsonData.getJSONArray("data");
						int length = data.length();
						if (length < 1) {
							cancelAll();
							return;
						}

						for (int i = 0; i < length; i++) {

							JSONObject deviceObj = data.getJSONObject(i);
							String mac = deviceObj.getString("mac");
							String deviceName = deviceObj.getString("deviceName");
							String id = deviceObj.getString("id");
							String type = deviceObj.getString("type");
							HashMap<String, Object> dmap = new HashMap<String, Object>();
							dmap.put("mac", mac);
							dmap.put("deviceName", deviceName);
							dmap.put("id", id);
							if ("1".equals(type)) {
								dmap.put("type", device_type_pic[0]);
							} else {
								dmap.put("type", device_type_pic[1]);
							}

							deviceList.add(dmap);

						}

						// 获取在线状态
						FinalHttp fh = new FinalHttp();
						AjaxParams params = new AjaxParams();
						params.put("id", Util.USER_ID);
						params.put("token", Util.USER_TOCKEN);

						String stateMac;
						stateMac = data.getJSONObject(0).getString("mac");
						for (int i = 1; i < length; i++) {
							stateMac += "," + data.getJSONObject(i).getString("mac");
						}
						params.put("mac", stateMac);
						Util.d("params  " + params);
						fh.post(Util.URL + "Device/line", params, new AjaxCallBack<String>() {
							@Override
							public void onSuccess(String t) {
								super.onSuccess(t);
								try {
									Util.d("getstate  " + t);
									JSONObject jsonData = new JSONObject(t);
									int code = jsonData.getInt("code");
									if (code == 1) {
										JSONArray data = jsonData.getJSONArray("data");
										int length = data.length();

										// 此版本仅有一个设备
										for (int i = 0; i < length; i++) {

											JSONObject deviceObj = data.getJSONObject(i);
											String mac = deviceObj.getString("mac");
											Boolean state = deviceObj.getBoolean("state");
											String dstate;
											if (state == true) {
												dstate = getString(R.string.on_line);
											} else {
												dstate = getString(R.string.out_line);
											}
											int dl = deviceList.size();
											for (int j = 0; j < dl; j++) {
												HashMap<String, Object> dmap = (HashMap<String, Object>) deviceList
														.get(j);
												// Util.d(dmap.get("mac"));
												Util.d("222 " + mac);
												if (dmap.get("mac").equals(mac)) {
													dmap.put("state", dstate);
													Util.d(dstate);
												}
											}
											
										}
										//deviceList.clear();
									
										adapter.notifyDataSetChanged();

									} else {
										MessageDialog msgDialog = new MessageDialog(DeviceListActivity.this,
												jsonData.getString("msg"));
										msgDialog.show();
										return;
									}
									cancelAll();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									cancelAll();
								}
							}

							@Override
							public void onFailure(Throwable t, int errorNo, String strMsg) {
								Util.d("faild-->" + strMsg);
								super.onFailure(t, errorNo, strMsg);
								cancelAll();

							}
						});
						cancelAll();

						// LoginActivity.this.finish();

					} else {
						cancelAll();
						if (!loginMsgDialog.isShowing()) {

							loginMsgDialog.show();
							Window window = loginMsgDialog.getWindow();
							window.setContentView(R.layout.dialog_quit);
							TextView alert_btn_title = (TextView) window.findViewById(R.id.alert_btn_title);
							alert_btn_title.setText(R.string.re_login);
							Button reboot_cancel_alert_btn = (Button) window.findViewById(R.id.reboot_cancel_alert_btn);
							reboot_cancel_alert_btn.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									loginMsgDialog.cancel();
									startActivity(new Intent(DeviceListActivity.this, LoginActivity.class));
									DeviceListActivity.this.finish();
								}
							});
							Button reboot_confirm_alert_btn = (Button) window
									.findViewById(R.id.reboot_confirm_alert_btn);
							reboot_confirm_alert_btn.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									loginMsgDialog.cancel();
								}
							});
						}
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					cancelAll();
				}

			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				MessageDialog msgDialog = new MessageDialog(DeviceListActivity.this,getString(R.string.device_fail));
				msgDialog.show();
				cancelAll();
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		initData();

		// switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
		// case RESULT_OK:
		// //重新刷新数据
		// init();
		// break;
		// default:
		// break;
		// }
	}

	public void finish() {

		super.finish();
	}

	public void onBackPressed() {

		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.show();
		Window window = alertDialog.getWindow();
		window.setContentView(R.layout.dialog_quit);
		Button reboot_cancel_alert_btn = (Button) window.findViewById(R.id.reboot_cancel_alert_btn);
		reboot_cancel_alert_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				alertDialog.dismiss();
				finish();
				System.exit(0);
			}
		});
		Button reboot_confirm_alert_btn = (Button) window.findViewById(R.id.reboot_confirm_alert_btn);
		reboot_confirm_alert_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.cancel();
			}
		});

	}
}
