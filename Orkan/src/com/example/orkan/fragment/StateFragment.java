package com.example.orkan.fragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.orkan.R;
import com.example.orkan.controller.MQTTController;
import com.example.orkan.controller.MainController;
import com.example.orkan.net.UDPWatcher;
import com.example.orkan.third.citypicker.CityDB;
import com.example.orkan.third.citypicker.CityPickerActivity;
import com.example.orkan.third.citypicker.LocateState;
import com.example.orkan.third.citypicker.StringUtils;
import com.example.orkan.third.citypicker.CityPickerActivity.BDLocationListenerImpl;
import com.example.orkan.third.citypicker.CityProvider;
import com.example.orkan.third.citypicker.CityProvider.CityConstants;
import com.example.orkan.util.GetSharedData;
import com.example.orkan.util.SaveSharedData;
import com.example.orkan.util.ScreeShoot;
import com.example.orkan.util.Util;
import com.example.orkan.view.SwitchView;
import com.example.orkan.view.SwitchView.OnStateChangedListener;

/**
 * Ap information fragment
 *
 * @author libo
 */

public class StateFragment extends BaseTabFragment implements View.OnClickListener, UDPWatcher {
	protected ContentResolver mContentResolver;
	private FinalHttp fh;
	TextView title_tx;
	TextView indoor_pm_count;
	TextView indoor_pm_value;
	TextView indoor_pm_grade;
	TextView indoor_pm_quality;
	TextView indoor_state_humidity_value;
	TextView indoor_state_temperature_value;
	TextView outdoor_state_pm_value;
	TextView outdoor_state_humidity_value;
	TextView outdoor_state_temperature_value;
	TextView outdoor_state_city;
	ImageView outdoor_state_location;
	ImageView indoor_count_image;
	View fragmentView;
	SwitchView control_screen;
	ImageView title_share;
	ImageView indoor_logo_image;
	String image;
	LocationClient mLocClient;  

	protected MQTTController mqttController;

	// pm2.5图片资源
	private int mImageViewArray[] = { R.drawable.pm_count1, R.drawable.pm_count2, R.drawable.pm_count3,
			R.drawable.pm_count4, R.drawable.pm_count5, R.drawable.pm_count6, R.drawable.pm_count7,
			R.drawable.pm_count8, R.drawable.pm_count9, R.drawable.pm_count10, R.drawable.pm_count11,
			R.drawable.pm_count12, R.drawable.pm_count13, R.drawable.pm_count14, R.drawable.pm_count15,
			R.drawable.pm_count16, R.drawable.pm_count17, R.drawable.pm_count18, R.drawable.pm_count19,
			R.drawable.pm_count20, R.drawable.pm_count21, R.drawable.pm_count22, R.drawable.pm_count23,
			R.drawable.pm_count24, R.drawable.pm_count25, R.drawable.pm_count26, R.drawable.pm_count27,
			R.drawable.pm_count28, };
	private String pmtipsArray[] = { "质量棒棒哒，继续保持~", "质量还可以，继续加油~", "轻度污染了，要努力哦~", "中度污染了，需要净化～", "重度污染了，急需净化～", "严重污染了，急需净化～" };
	private String pmgradArray[] = { "优", "良", "中", "差","重度","严重" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 注册观察者

		mContentResolver = getActivity().getContentResolver();
		mqttController = MQTTController.getInstance();
		mqttController.addWatcher(this);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentView = inflater.inflate(R.layout.fragment_state, null);

		init(fragmentView);
		Log.v("orkan", "state on create view");
		getOutdoorData();
		initData();

		return fragmentView;
	}

	protected void init(View view) {
		super.init(view);
		title_tx = (TextView) view.findViewById(R.id.title_tx);
		title_tx.setText(R.string.title_state);
		indoor_count_image = (ImageView) view.findViewById(R.id.indoor_count_image);
		indoor_pm_count = (TextView) view.findViewById(R.id.indoor_pm_count);
		indoor_pm_count.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 下划线
		indoor_pm_count.getPaint().setAntiAlias(true);// 抗锯齿

		indoor_pm_value = (TextView) view.findViewById(R.id.indoor_pm_value);
		indoor_pm_grade = (TextView) view.findViewById(R.id.indoor_pm_grade);
		indoor_logo_image = (ImageView) view.findViewById(R.id.indoor_logo_image);

		indoor_pm_quality = (TextView) view.findViewById(R.id.indoor_pm_quality);

		control_screen = (SwitchView) view.findViewById(R.id.control_screen);
		title_share = (ImageView) view.findViewById(R.id.title_share);

		indoor_state_humidity_value = (TextView) view.findViewById(R.id.indoor_state_humidity_value);
		indoor_state_temperature_value = (TextView) view.findViewById(R.id.indoor_state_temperature_value);

		outdoor_state_pm_value = (TextView) view.findViewById(R.id.outdoor_state_pm_value);
		outdoor_state_humidity_value = (TextView) view.findViewById(R.id.outdoor_state_humidity_value);
		outdoor_state_temperature_value = (TextView) view.findViewById(R.id.outdoor_state_temperature_value);

		outdoor_state_city = (TextView) view.findViewById(R.id.outdoor_state_city);
		String shareCity = GetSharedData.GetData(getActivity(), "city", Util.INITIAL_STATUS_CITY);
		if("null".equals(shareCity)){
			initLocation();
		}else{
			outdoor_state_city.setText(shareCity);
		}
		
		outdoor_state_location = (ImageView) view.findViewById(R.id.outdoor_state_location);
		outdoor_state_location.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getActivity(), CityPickerActivity.class), 1);
			}
		});
		outdoor_state_city.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getActivity(), CityPickerActivity.class), 1);
			}
		});
		MainController mainController = MainController.getInstance(getActivity());
		mainController.setCityInterface(new MainController.CityInterface() {

			@Override
			public void setcity(String city) {
				if (city != null && (!city.equals(""))) {
					Util.d("choose city: " + city);
					outdoor_state_city.setText(city);
					SaveSharedData.SaveData(getActivity(), "city", city);
					getOutdoorData();
				}

			}
		});

		title_share.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				image = ScreeShoot.shoot(getActivity());
				String _photoPath = image;
				Uri imageUri = Uri.fromFile(new File(_photoPath));
				Intent shareIntent = new Intent();
				shareIntent.setType("*/*");
				shareIntent.setAction(Intent.ACTION_SEND);
				shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
				shareIntent.putExtra(Intent.EXTRA_TEXT, "新风相伴，自然为邻\\nhttp://www.orkan.com.cn/");
				startActivity(Intent.createChooser(shareIntent, "分享到"));

			}
		});

		control_screen.setOnStateChangedListener(new OnStateChangedListener() {

			@Override
			public void toggleToOn(View view) {
				// control_screen.toggleSwitch(true);
				beginAll();
				byte[] b1 = { 0x01 };
				mqttController.sendMsg((byte) 0x18, b1);
			}

			@Override
			public void toggleToOff(View view) {
				// control_screen.toggleSwitch(false);
				beginAll();
				byte[] b1 = { 0x00 };
				mqttController.sendMsg((byte) 0x18, b1);
			}
		});
	}
	
	private void  initLocation(){
	 	mLocClient = new LocationClient(getActivity());  
        mLocClient.registerLocationListener(new BDLocationListenerImpl());//注册定位监听接口  
          
        /** 
         * LocationClientOption 该类用来设置定位SDK的定位方式。 
         */  
        LocationClientOption option = new LocationClientOption();  
        option.setOpenGps(true); //打开GPRS  
        option.setAddrType("all");//返回的定位结果包含地址信息  
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02  
        option.setPriority(LocationClientOption.GpsFirst); // 设置GPS优先  
        option.setScanSpan(5000); //设置发起定位请求的间隔时间为5000ms  
        option.disableCache(false);//禁止启用缓存定位  
        mLocClient.setLocOption(option);  //设置定位参数  
          
          
        mLocClient.start();  // 调用此方法开始定位  
	}
	
	
	@Override
	public void onResume() {
		// TODO 自动生成的方法存根
		animation = null;
		super.onResume();
	}

    public class BDLocationListenerImpl implements BDLocationListener {  
  	  
        /** 
         * 接收异步返回的定位结果，参数是BDLocation类型参数 
         */  
        @Override  
        public void onReceiveLocation(BDLocation location) {  
            String city_temp = location.getCity();
            String city = city_temp.replace("市","").trim();
            outdoor_state_city.setText(city);
            mLocClient.stop();
            SaveSharedData.SaveData(getActivity(), "city", city);
            getOutdoorData();
        }  
  
    }  
	
	protected void initData() {


		indoor_pm_value.setText(Util.INITIAL_STATUS_PM_VALUE + "");
		indoor_state_humidity_value.setText(Util.INITIAL_STATUS_HUMIDITY_VALUE + "%");
		indoor_state_temperature_value.setText(Util.INITIAL_STATUS_TEMP_VALUE + "℃");
		if (Util.INITIAL_STATUS_PM_VALUE.equals("--")) {

		} else {

			int pm = Integer.parseInt(Util.INITIAL_STATUS_PM_VALUE);
			if (pm < 50) {
				indoor_pm_grade.setText(pmgradArray[0]);
				indoor_pm_quality.setText(pmtipsArray[0]);
				indoor_pm_grade.setTextColor(Color.rgb(255, 255, 255));
				indoor_pm_value.setTextColor(Color.rgb(255, 255, 255));
				indoor_pm_quality.setTextColor(Color.rgb(255, 255, 255));
				
			} else if (pm < 100) {
				indoor_pm_grade.setText(pmgradArray[1]);
				indoor_pm_quality.setText(pmtipsArray[1]);
				indoor_pm_grade.setTextColor(Color.rgb(232, 255, 77));
				indoor_pm_value.setTextColor(Color.rgb(232, 255, 77));
				indoor_pm_quality.setTextColor(Color.rgb(232, 255, 77));
			} else if (pm < 150) {
				indoor_pm_grade.setText(pmgradArray[2]);
				indoor_pm_quality.setText(pmtipsArray[2]);
				indoor_pm_grade.setTextColor(Color.rgb(254, 115, 35));
				indoor_pm_value.setTextColor(Color.rgb(254, 115, 35));
				indoor_pm_quality.setTextColor(Color.rgb(254, 115, 35));
			}else if (pm < 200) {
				indoor_pm_grade.setText(pmgradArray[3]);
				indoor_pm_quality.setText(pmtipsArray[3]);
				indoor_pm_grade.setTextColor(Color.rgb(255, 63, 37));
				indoor_pm_value.setTextColor(Color.rgb(255, 63, 37));
				indoor_pm_quality.setTextColor(Color.rgb(255, 63, 37));
			}else if (pm < 250) {
				indoor_pm_grade.setText(pmgradArray[4]);
				indoor_pm_quality.setText(pmtipsArray[4]);
				indoor_pm_grade.setTextColor(Color.rgb(182, 82, 237));
				indoor_pm_value.setTextColor(Color.rgb(182, 82, 237));
				indoor_pm_quality.setTextColor(Color.rgb(182, 82, 237));
			} else {
				indoor_pm_grade.setText(pmgradArray[5]);
				indoor_pm_quality.setText(pmtipsArray[5]);
				indoor_pm_grade.setTextColor(Color.rgb(191, 59, 41));
				indoor_pm_value.setTextColor(Color.rgb(191, 59, 41));
				indoor_pm_quality.setTextColor(Color.rgb(191, 59, 41));
			}

			int pm2 = pm / 10;
			if (pm2 > 27) {
				pm2 = 27;
			}
			if (pm2 < 0) {
				pm2 = 0;
			}
			indoor_count_image.setImageResource(mImageViewArray[pm2]);
			//indoor_count_image.setBackgroundResource(mImageViewArray[pm2]);
			
			setFlickerAnimation(indoor_logo_image);
		}

		outdoor_state_pm_value.setText(Util.INITIAL_STATUS_OUTDOOR_PM_VALUE);
		outdoor_state_humidity_value.setText(Util.INITIAL_STATUS_OUTDOOR_HUMIDITY_VALUE);
		outdoor_state_temperature_value.setText(Util.INITIAL_STATUS_OUTDOOR_TEMP_VALUE + "℃");
	}

	private Animation animation = null;

	private void setFlickerAnimation(ImageView iv_chat_head) {
		if (animation == null) {
			animation = new AlphaAnimation(1, 0);
			animation.setDuration(1500);// 闪烁时间间隔
			animation.setInterpolator(new AccelerateDecelerateInterpolator());
			animation.setRepeatCount(Animation.INFINITE);
			animation.setRepeatMode(Animation.REVERSE);
			iv_chat_head.setAnimation(animation);
		}
	}

	protected void getOutdoorData() {
		String cityname = outdoor_state_city.getText().toString();
		String cityId = getLocationCityFromDB(cityname).getPostID();
		Util.d(outdoor_state_city.getText() + "：" + cityId);
		FinalHttp fh = new FinalHttp();

		Date d = new Date();
		System.out.println(d);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String dateNowStr = sdf.format(d);
		System.out.println("格式化后的日期：" + dateNowStr);

		AjaxParams params = new AjaxParams();
		params.put("date", dateNowStr);
		params.put("citykey", cityId);
		fh.get(Util.WEATHER_URL, params, new AjaxCallBack<String>() {
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				try {
					Util.d(t);
					JSONObject jsonData = new JSONObject(t);
					JSONObject realtimeData = jsonData.getJSONObject("observe");
					String temp = realtimeData.getString("temp");
					String humi = realtimeData.getString("shidu");
					JSONObject aqiData = jsonData.getJSONObject("evn");
					String pm = aqiData.getString("pm25");
					Util.INITIAL_STATUS_OUTDOOR_PM_VALUE = pm;
					Util.INITIAL_STATUS_OUTDOOR_HUMIDITY_VALUE = humi;
					Util.INITIAL_STATUS_OUTDOOR_TEMP_VALUE = temp;
					outdoor_state_pm_value.setText(Util.INITIAL_STATUS_OUTDOOR_PM_VALUE);
					outdoor_state_humidity_value.setText(Util.INITIAL_STATUS_OUTDOOR_HUMIDITY_VALUE);
					outdoor_state_temperature_value.setText(Util.INITIAL_STATUS_OUTDOOR_TEMP_VALUE + "℃");
					Util.d(jsonData.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				if (getActivity() != null) {
					// Toast.makeText(getActivity(), "获取室外数据错误",
					// Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	protected CityDB getLocationCityFromDB(String name) {
		CityDB city = new CityDB();
		city.setName(name);
		Cursor c = mContentResolver.query(CityProvider.CITY_CONTENT_URI, new String[] { CityConstants.POST_ID },
				CityConstants.NAME + "=?", new String[] { name }, null);
		if (c != null && c.moveToNext())
			city.setPostID(c.getString(c.getColumnIndex(CityConstants.POST_ID)));
		return city;
	}

	// 按钮点击事件
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// case R.id.char_sta_tx:
		// //启动图表界面
		// Intent it = new Intent(getActivity(), LineChartActivity.class);
		// getActivity().startActivityForResult(it, 1);
		// break;
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			// TODO

			Intent intent = new Intent(Intent.ACTION_SEND);
			File f = new File(image);
			if (f != null && f.exists() && f.isFile()) {
				intent.setType("image/jpg");
				Uri u = Uri.fromFile(f);
				intent.putExtra(Intent.EXTRA_STREAM, u);
			}
			intent.putExtra(Intent.EXTRA_TEXT, "新风相伴，自然为邻\\nhttp://www.orkan.com.cn/");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(Intent.createChooser(intent, "xxx"));
		};
	};

	// handler更新UI
	protected class ProbeHandler extends Handler {
		public ProbeHandler() {

		}

		@Override
		public void handleMessage(Message msg) {

			// TODO Auto-generated method stub
			switch (msg.what) {
			case Util.INSTANCE_CODE_GETSTATUS_REV:

				break;
			}

		}
	}

	@Override
	public void getUDPMessage(int code, byte[] data, int len) {
		if (code == 0x02) {
			// 状态;
			int temperature = data[0];

			int wamp = data[1] & 0xff;
			int pm1 = data[5];
			int pm2 = data[6] & 0xff;
			int pm = pm1 * 256 + pm2;
			Util.INITIAL_STATUS_PM_VALUE = pm + "";
			Util.INITIAL_STATUS_HUMIDITY_VALUE = wamp + "";
			Util.INITIAL_STATUS_TEMP_VALUE = temperature + "";
			Util.d("wamp  " + wamp);

			if (pm1 == (byte) 0xa5 && pm2 == 0xa5) {
				Util.INITIAL_STATUS_PM_VALUE = "--";
			}

			if (wamp == 0xa5) {
				Util.INITIAL_STATUS_HUMIDITY_VALUE = "--";
			}
			Util.d("temperature  " + temperature);
			if (temperature == (byte) 0xa5) {
				Util.INITIAL_STATUS_TEMP_VALUE = "--";
			}
			initData();

		} else if (code == 0x04) {
			cancelAll();
			// if(data.length < 15 || len < 15){
			// return;
			// }

			// Util.MQTT_USER_FENG = MQTTUtil.subBytes(data, 0, 12);
			Util.INITIAL_STATUS_OPEN = data[12] & 0xff;
			Util.INITIAL_STATUS_SWITCHMODE = (data[13] & 0xff);
			// //错误数据时当1处理
			// if(Util.INITIAL_STATUS_SWITCHMODE == 0){
			// Util.INITIAL_STATUS_SWITCHMODE =1;
			// }
			int data14 = data[14] & 0xff;

			if (data14 == 0) {
				Util.INITIAL_STATUS_XINFENG_OPEN = 0;

			} else {
				Util.INITIAL_STATUS_XINFENG_OPEN = 1;
			}

			Util.d("Util.INITIAL_STATUS_SWITCHMODE " + Util.INITIAL_STATUS_SWITCHMODE);
			Util.INITIAL_STATUS_AUTOMODE = data[15] & 0xff;
			Util.INITIAL_STATUS_TIMING = data[16] & 0xff;

			if (data.length > 17) {
				Util.INITIAL_STATUS_LIGHTMODE = data[17] & 0xff;

			}
			if (Util.INITIAL_STATUS_LIGHTMODE == 1) {
				// fr get the data open or close
				control_screen.toggleSwitch(true);
			} else {
				// fr get the data open or close
				control_screen.toggleSwitch(false);
			}
			initData();
		}

	}

}
