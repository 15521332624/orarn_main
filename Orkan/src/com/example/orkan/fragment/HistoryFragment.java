package com.example.orkan.fragment;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orkan.R;
import com.example.orkan.adapter.HistoryTabAdapter;
import com.example.orkan.util.CharData;
import com.example.orkan.util.GetSharedData;
import com.example.orkan.util.SaveSharedData;
import com.example.orkan.util.Util;
import com.example.orkan.viewpagerindicator.PageIndicator;

public class HistoryFragment extends BaseTabFragment implements
		View.OnClickListener {
	static final int DATA_MODE_HOUR = 1;
	static final int DATA_MODE_DAY = 2;
	static final int DATA_MODE_WEEK = 3;

	static final int MSG_PM = 1;
	static final int MSG_TEMP = 2;
	static final int MSG_HUMI = 3;

	private Context context;
	TextView title_tx;
	View fragmentView;
	TextView history_page_time_tx;
	RadioGroup group;
	RadioButton hourButton;
	RadioButton dayButton;
	RadioButton weekButton;
	String date;
	HistoryTabAdapter adapter;
	private ProbeHandler probeHandler = new ProbeHandler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (null != fragmentView) {
			ViewGroup parent = (ViewGroup) fragmentView.getParent();
			if (null != parent) {
				parent.removeView(fragmentView);
			}
		} else {
			fragmentView = inflater.inflate(R.layout.fragment_history, null);
			init(fragmentView);

		}

		return fragmentView;
	}

	protected void init(View view) {
		super.init(view);

		title_tx = (TextView) view.findViewById(R.id.title_tx);
		title_tx.setText(R.string.title_history);
		adapter = new HistoryTabAdapter(getActivity()
				.getSupportFragmentManager());
		// 视图切换器
		ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
		pager.setOffscreenPageLimit(1);
		pager.setAdapter(adapter);

		// 页面指示器
		PageIndicator indicator = (PageIndicator) view
				.findViewById(R.id.indicator);
		indicator.setViewPager(pager);

		history_page_time_tx = (TextView) view
				.findViewById(R.id.history_page_time_tx);
		history_page_time_tx.setText(R.string.time_slot);
		hourButton = (RadioButton) view.findViewById(R.id.hour);
		dayButton = (RadioButton) view.findViewById(R.id.day);
		weekButton = (RadioButton) view.findViewById(R.id.week);
		group = (RadioGroup) view.findViewById(R.id.history_page_time_radio);
		Util.INITIAL_STATUS_HISTORY_DATAMODE = GetSharedData.GetData(context,
				"historydatamode", Util.DATA_MODE_HOUR);

		// 绑定一个匿名监听器
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				// 获取变更后的选中项的ID

				timeOutControl();
				if (arg1 == hourButton.getId()) {
					Util.d("hour");
					Util.INITIAL_STATUS_HISTORY_DATAMODE = DATA_MODE_HOUR;
					SaveSharedData.SaveData(context, "historydatamode",
							Util.DATA_MODE_HOUR);
				}
				if (arg1 == dayButton.getId()) {
					Util.d("day");
					Util.INITIAL_STATUS_HISTORY_DATAMODE = DATA_MODE_DAY;
					SaveSharedData.SaveData(context, "historydatamode",
							Util.DATA_MODE_DAY);
				}
				if (arg1 == weekButton.getId()) {
					Util.d("week");
					Util.INITIAL_STATUS_HISTORY_DATAMODE = DATA_MODE_WEEK;
					SaveSharedData.SaveData(context, "historydatamode",
							Util.DATA_MODE_WEEK);
				}

				initData();

			}
		});
		if (Util.INITIAL_STATUS_HISTORY_DATAMODE == Util.DATA_MODE_HOUR) {
			hourButton.setChecked(true);
		} else if (Util.INITIAL_STATUS_HISTORY_DATAMODE == Util.DATA_MODE_DAY) {
			dayButton.setChecked(true);
		} else {
			weekButton.setChecked(true);
		}

	}

	protected void initData() {
		if("null".endsWith(Util.MQTT_DEVICE_ID))
			return ;
		if (Util.INITIAL_STATUS_HISTORY_DATAMODE == Util.DATA_MODE_HOUR) {
			date = "Hour";
		} else if (Util.INITIAL_STATUS_HISTORY_DATAMODE == Util.DATA_MODE_DAY) {
			date = "Day";
		} else {
			date = "Week";
		}
		hud.show();
		getpm();
		gettemp();
		gethum();

	}

	private void gethttp(final String type) {
		Util.d("date  " + date);
		Util.d(Util.URL_H + "History/" + date + "/" + Util.MQTT_USER_MAC + "/"
				+ type);
		FinalHttp fh = new FinalHttp();
		AjaxParams params = new AjaxParams();
		params.put("id", Util.USER_ID);
		params.put("token", Util.USER_TOCKEN);

		// params.put("sn", MQTTUtil.byte2hex(Util.MQTT_USER_FENG));
		// MAC : f0fe6b320166
		fh.post(Util.URL + "History/" + date + "/" + Util.MQTT_USER_MAC + "/"
				+ type, params, new AjaxCallBack<String>() {
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				try {
					Util.d(t);
					JSONObject jsonData = new JSONObject(t);
					int code = jsonData.getInt("code");
					if (code == 1) {

						JSONArray data = jsonData.getJSONArray("data");
						if (data.length() <= 1) {
							return;
						}
						if (type.equals("pm25")) {
							Util.CHAR_POINTS_INDOOR_PM.clear();
						} else if (type.equals("temp")) {
							Util.CHAR_INDOOR_TEMPERATURE.clear();
						} else if (type.equals("humi")) {
							Util.CHAR_POINTS_INDOOR_HUMIDITY.clear();
						}

						String minTime = data.getJSONObject(0)
								.getString("time");
						String maxTime = minTime;

						Util.HistoryData = data.length();
						Log.v("snake", "data-length = " + data.length());
						for (int i = 0; i < data.length(); i++) {
							JSONObject obj = data.getJSONObject(i);
							String time = obj.getString("time");
							String value = obj.getString("value");

							if (type.equals("pm25")) {
								if (date.equals("Day")) {
									Util.CHAR_POINTS_INDOOR_PM
											.add(new CharData(Float
													.parseFloat(value), time));
								} else if (date.equals("Week")) {
									Util.CHAR_POINTS_INDOOR_PM
											.add(new CharData(Float
													.parseFloat(value), time
													.replace("月", "/").replace(
															"日", "")));
								} else if (date.equals("Hour")) {
									Util.CHAR_POINTS_INDOOR_PM
											.add(new CharData(Float
													.parseFloat(value), time
													.replace("点", ":").replace(
															"分", "")));
								}

							} else if (type.equals("temp")) {
								if (date.equals("Day")) {
									Util.CHAR_INDOOR_TEMPERATURE
											.add(new CharData(Float
													.parseFloat(value), time));
								} else if (date.equals("Week")) {
									Util.CHAR_INDOOR_TEMPERATURE
											.add(new CharData(Float
													.parseFloat(value), time
													.replace("月", "/").replace(
															"日", "")));
								} else if (date.equals("Hour")) {
									Util.CHAR_INDOOR_TEMPERATURE
											.add(new CharData(Float
													.parseFloat(value), time
													.replace("点", ":").replace(
															"分", "")));
								}
							} else if (type.equals("humi")) {
								if (date.equals("Day")) {
									Util.CHAR_POINTS_INDOOR_HUMIDITY
											.add(new CharData(Float
													.parseFloat(value), time));
								} else if (date.equals("Week")) {
									Util.CHAR_POINTS_INDOOR_HUMIDITY
											.add(new CharData(Float
													.parseFloat(value), time
													.replace("月", "/").replace(
															"日", "")));
								} else if (date.equals("Hour")) {
									Util.CHAR_POINTS_INDOOR_HUMIDITY
											.add(new CharData(Float
													.parseFloat(value), time
													.replace("点", ":").replace(
															"分", "")));
								}
							}

						}
						Util.INITIAL_STATUS_HISTORY_STARTTIME = minTime;
						Util.INITIAL_STATUS_HISTORY_ENDTIME = maxTime;

						Message msg = new Message();
						if (type.equals("pm25")) {
							msg.what = MSG_PM;
						} else if (type.equals("temp")) {
							msg.what = MSG_TEMP;
						} else if (type.equals("humi")) {
							msg.what = MSG_HUMI;
						}
						probeHandler.sendMessage(msg);

					} else {
						Toast.makeText(context, getString(R.string.get_user_fail), Toast.LENGTH_SHORT)
								.show();

					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
				cancelAll();
			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				Util.d("fail->>" + strMsg);
				Toast.makeText(context, getString(R.string.get_user_fail), Toast.LENGTH_SHORT).show();
				cancelAll();
			}
		});
	}

	private void getpm() {
		String type = "pm25";
		gethttp(type);
	}

	private void gettemp() {
		String type = "temp";
		gethttp(type);
	}

	private void gethum() {
		String type = "humi";
		gethttp(type);
	}

	// 按钮点击事件
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		}
	}

	// handler更新UI
	protected class ProbeHandler extends Handler {
		public ProbeHandler() {

		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_PM:
				adapter.notifypm();
				break;
			case MSG_TEMP:
				adapter.notifytemp();
				break;
			case MSG_HUMI:
				adapter.notifyhumi();
				break;
			default:
				break;
			}
		}
	}
}