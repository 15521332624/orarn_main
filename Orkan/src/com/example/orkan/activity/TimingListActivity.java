package com.example.orkan.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orkan.R;
import com.example.orkan.adapter.TimingListAdapter;
import com.example.orkan.controller.MQTTController;
import com.example.orkan.dialog.MessageDialog;
import com.example.orkan.net.UDPSocketServer;
import com.example.orkan.net.UDPWatcher;
import com.example.orkan.third.kprogresshud.KProgressHUD;
import com.example.orkan.util.GetSharedData;
import com.example.orkan.util.MQTTUtil;
import com.example.orkan.util.Util;

public class TimingListActivity extends BaseActivity implements UDPWatcher {
	ListView timinglistView;
	TimingListAdapter adapter;
	String[] levelStrs = { "一级", "二级", "三级" };
	private MQTTController mqttController;
	TextView title_tx;
	ImageView title_im;
	ImageView title_left;
	KProgressHUD hud;
	TextView timing_delete;
	boolean isTiming = false;

	protected UDPSocketServer mProbeSocketDiscover;
	// private ProbeHandler probeHandler = new ProbeHandler();
	private List<Map<String, String>> timingList = new ArrayList<Map<String, String>>();
	private String[] timingModeList = { "周一", "周二", "周三", "周四", "周五", "周六", "周日", "周一到周五", "每天" };

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timinglist);
		init();
		initData();

		byte[] b1 = Util.MQTT_USER_FENG;
		// 获取新风机状态
		mqttController.sendMsg((byte) 0x15, b1);
	}

	private void init() {
		mqttController = MQTTController.getInstance();
		mqttController.addWatcher(this);
		hud = KProgressHUD.create(this).setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
		title_tx = (TextView) findViewById(R.id.title_tx);
		title_tx.setText(R.string.title_timing);
		title_im = (ImageView) findViewById(R.id.title_im);
		title_im.setImageResource(R.drawable.timing_selected);
		title_im.setVisibility(View.VISIBLE);
		title_im.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 定时下发
				if (Util.MQTT_USER_FENG.length < 12) {
					return;
				}

				sendTiming();

			}
		});
		title_left = (ImageView) findViewById(R.id.title_left);
		title_left.setImageResource(R.drawable.back);
		title_left.setVisibility(View.VISIBLE);
		title_left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TimingListActivity.this.finish();
			}
		});

		timinglistView = (ListView) findViewById(R.id.timing_list);

		// 点击事件
		// 点击事件
		timinglistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				Intent intent = new Intent(TimingListActivity.this, TimingSetActivity.class);
				Bundle bundle = new Bundle();
				// bundle.putString("Id", Util.CONTROL_TIMING_LIST.get(position).get("Id"));
				bundle.putString("StartTime", Util.CONTROL_TIMING_LIST.get(position).get("StartTime"));
				bundle.putString("EndTime", Util.CONTROL_TIMING_LIST.get(position).get("EndTime"));
				bundle.putString("level", Util.CONTROL_TIMING_LIST.get(position).get("level"));
				bundle.putString("Mode", position + "");
				intent.putExtras(bundle);
				startActivityForResult(intent, 1);

			}
		});

		timing_delete = (TextView) findViewById(R.id.timing_delete);
		timing_delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 删除定时

				if (Util.MQTT_USER_FENG.length < 12) {
					return;
				}

				List<HashMap<String, String>> gettimingList = Util.CONTROL_TIMING_LIST;
				byte[] data1 = Util.MQTT_USER_FENG;
				byte[] data2 = { (byte) 0x00 };
				byte[] data3 = new byte[35];

				for (int i = 0; i < 7; i++) {
					int index = i * 5;
					HashMap<String, String> map = gettimingList.get(i);

					int startHour = Integer.parseInt(map.get("StartTime").substring(0, 2));
					int startMin = Integer.parseInt(map.get("StartTime").substring(3, 5));

					int endHour = Integer.parseInt(map.get("EndTime").substring(0, 2));
					int ednMin = Integer.parseInt(map.get("EndTime").substring(3, 5));
					int level = Integer.parseInt(map.get("level"));

					data3[index] = (byte) startHour;
					data3[index + 1] = (byte) startMin;
					data3[index + 2] = (byte) endHour;
					data3[index + 3] = (byte) ednMin;
					data3[index + 4] = (byte) (level + 1);
					if (startHour == 0 && startMin == 0 && endHour == 0 && ednMin == 0) {
						data3[index] = (byte) 0xff;
						data3[index + 1] = (byte) 0xff;
						data3[index + 2] = (byte) 0xff;
						data3[index + 3] = (byte) 0xff;
						data3[index + 4] = (byte) 0xff;
					}
				}

				int len = data1.length + data2.length + data3.length;
				byte[] data = MQTTUtil.addBytes(data1, data2, data3);
				beginAll();
				mqttController.sendMsg((byte) 0x07, data);

			}
		});

	}

	private void initData() {
		String[] from = { "list_title", "list_content" };
		int[] to = { R.id.client_mac_txt, R.id.client_ap_name_txt };

		List<HashMap<String, String>> gettimingList = Util.CONTROL_TIMING_LIST;
		timingList.clear();
		for (int i = 0; i < gettimingList.size(); i++) {
			if (i >= 7) {
				return;
			}
			HashMap<String, String> getmap = gettimingList.get(i);
			HashMap<String, String> setmap = new HashMap<String, String>();
			// setmap.put("Id", getmap.get("Id"));
			setmap.put("list_title", timingModeList[i]);
			String startTime = getmap.get("StartTime");
			String endTime = getmap.get("EndTime");
			Util.d(i + "  " + startTime + "  " + endTime);
			String level = getmap.get("level");
			int l = Integer.parseInt(level);
			if (startTime.equals("00:00") && endTime.equals("00:00")) {
				setmap.put("list_content", getString(R.string.close));
			} else {
				setmap.put("list_content", startTime + "~" + endTime + " " + levelStrs[l]);
			}
			timingList.add(setmap);
		}
		// timingList.clear();
		// for(int i=0;i<4;i++) {
		// Map<String, String> map = new HashMap<String, String>();
		// // map.put("list_im", R.drawable.clients_list_im);
		// map.put("list_title", "每天");
		// map.put("list_content", "3：00~6:00");
		// timingList.add(map);
		// }

		adapter = new TimingListAdapter(this, timingList, R.layout.list_timing, from, to);

		// 设置适配器
		timinglistView.setAdapter(adapter);

	}

	private void sendTiming() {

		List<HashMap<String, String>> gettimingList = Util.CONTROL_TIMING_LIST;
		byte[] data1 = Util.MQTT_USER_FENG;
		byte[] data2 = { (byte) 0x01 };
		byte[] data3 = new byte[35];

		for (int i = 0; i < 7; i++) {
			int index = i * 5;
			HashMap<String, String> map = gettimingList.get(i);

			int startHour = Integer.parseInt(map.get("StartTime").substring(0, 2));
			int startMin = Integer.parseInt(map.get("StartTime").substring(3, 5));

			int endHour = Integer.parseInt(map.get("EndTime").substring(0, 2));
			int ednMin = Integer.parseInt(map.get("EndTime").substring(3, 5));
			int level = Integer.parseInt(map.get("level"));

			data3[index] = (byte) startHour;
			data3[index + 1] = (byte) startMin;
			data3[index + 2] = (byte) endHour;
			data3[index + 3] = (byte) ednMin;
			data3[index + 4] = (byte) (level + 1);
			if (startHour == 0 && startMin == 0 && endHour == 0 && ednMin == 0) {
				data3[index] = (byte) 0xff;
				data3[index + 1] = (byte) 0xff;
				data3[index + 2] = (byte) 0xff;
				data3[index + 3] = (byte) 0xff;
				data3[index + 4] = (byte) 0xff;
			}
		}

		int len = data1.length + data2.length + data3.length;
		byte[] data = MQTTUtil.addBytes(data1, data2, data3);
		beginAll();
		mqttController.sendMsg((byte) 0x07, data);

	}

	@Override
	public void getUDPMessage(int code, byte[] data, int len) {

		if (code == 0x04) {
			cancelAll();
			Toast.makeText(getApplicationContext(), R.string.setting_success, Toast.LENGTH_SHORT).show();
			finish();
		} else if (code == 0x16) {
			cancelAll();
			if (data.length < 48) {
				return;
			}
			int it = data[12] & 0xff;
			if (it == 0) {
				isTiming = false;
				return;
			}
			for (int i = 0; i < 7; i++) {
				int index = i * 5;
				int sH = data[13 + index] & 0xff;
				int sM = data[13 + index + 1] & 0xff;
				int eH = data[13 + index + 2] & 0xff;
				int eM = data[13 + index + 3] & 0xff;
				int wind = data[13 + index + 4] & 0xff;
				HashMap<String, String> map = Util.CONTROL_TIMING_LIST.get(i);
				String shs;
				String sms;
				String seh;
				String sem;
				if (sH < 10) {
					shs = "0" + sH;
				} else {
					shs = "" + sH;
				}
				if (sM < 10) {
					sms = "0" + sM;
				} else {
					sms = "" + sM;
				}
				if (eH < 10) {
					seh = "0" + eH;
				} else {
					seh = "" + eH;
				}
				if (eM < 10) {
					sem = "0" + eM;
				} else {
					sem = "" + eM;
				}

				String StartTime = shs + ":" + sms;
				String EndTime = seh + ":" + sem;
				Util.d("level " + wind);
				if (sH == 0xff && sM == 0xff && eH == 0xff && eM == 0xff) {
					map.put("StartTime", "00:00");
					map.put("EndTime", "00:00");
					map.put("level", "0");
				} else {

					map.put("StartTime", StartTime);
					map.put("EndTime", EndTime);
					int level = wind - 1;
					map.put("level", level + "");
				}
			}

			initData();
		}

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
		mqttController.removeWatcher(this);
		super.finish();
	}

}
