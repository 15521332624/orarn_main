package com.example.orkan.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentPagerAdapter;
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
import com.example.orkan.activity.LoginActivity;
import com.example.orkan.activity.MainActivity;
import com.example.orkan.activity.SmartConfigActivity;
import com.example.orkan.adapter.HistoryTabAdapter;
import com.example.orkan.dialog.MessageDialog;
import com.example.orkan.fragment.ControlFragment.ProbeHandler;
import com.example.orkan.net.UDPSocketServer;
import com.example.orkan.net.UDPWatcher;
import com.example.orkan.third.kprogresshud.KProgressHUD;
import com.example.orkan.util.CharData;
import com.example.orkan.util.GetSharedData;
import com.example.orkan.util.MQTTUtil;
import com.example.orkan.util.SaveSharedData;
import com.example.orkan.util.Util;
import com.example.orkan.view.BezierCurveChart;
import com.example.orkan.view.BezierCurveChart.Point;
import com.example.orkan.view.slidedatetimepicker.SlideDateTimeListener;
import com.example.orkan.view.slidedatetimepicker.SlideDateTimePicker;
import com.example.orkan.viewpagerindicator.PageIndicator;

public class HistoryFragment extends BaseTabFragment implements View.OnClickListener{
	 static final int DATA_MODE_HOUR = 1;
	 static final int DATA_MODE_DAY = 2;
	 static final int DATA_MODE_WEEK = 3;
	 
	 static final int MSG_PM = 1;
	 static final int MSG_TEMP = 2;
	 static final int MSG_HUMI = 3;
	 //time out
	
	 
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
//	 private SlideDateTimeListener listener = new SlideDateTimeListener() {
//	
//        @Override
//        public void onDateTimeSet(Date date)
//        {	
//        	hud.show();
//    		timeOutControl();
//        	String startTime = Util.ORKAN_SIMPLE_DATE_FORMAT.format(date);
//        	String endTIme;
//            Toast.makeText(getActivity(),
//            		Util.ORKAN_SIMPLE_DATE_FORMAT.format(date), Toast.LENGTH_SHORT).show();
//            if(Util.INITIAL_STATUS_HISTORY_DATAMODE == DATA_MODE_HOUR){
//            	endTIme = Util.getOneHourLaterTime(startTime);
//            }else if(Util.INITIAL_STATUS_HISTORY_DATAMODE == DATA_MODE_DAY){
//            	endTIme = Util.getOneDayLaterTime(startTime);
//            }else{
//            	endTIme = Util.getOneWeekLaterTime(startTime);
//            }
//            try {
//    			JSONObject historyjsonObj = new JSONObject();
//    			historyjsonObj.put("StartTime", startTime);
//    			historyjsonObj.put("EndTime", endTIme);
//    			historyjsonObj.put("Number", Util.HISTORY_POINTS_NUM +"");
//    			mProbeSocketDiscover.sendUDPdata(Util.IP_ADDRESS, historyjsonObj, Util.INSTANCE_CODE_HISTORY_SEND);
//    		} catch (JSONException e) {
//    			// TODO Auto-generated catch block
//    			e.printStackTrace();
//    		}
//            Util.INITIAL_STATUS_HISTORY_STARTTIME = startTime;
//            Util.INITIAL_STATUS_HISTORY_ENDTIME = endTIme;
//            
//        }
//
//        // Optional cancel listener
//	    @Override
//	    public void onDateTimeCancel()
//	    {
////	        Toast.makeText(getActivity(),
////	                "Canceled", Toast.LENGTH_SHORT).show();
//	    }
//	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
		
		}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {	
		 if (null != fragmentView) {
	            ViewGroup parent = (ViewGroup) fragmentView.getParent();
	            if (null != parent) {
	                parent.removeView(fragmentView);
	            }
	        } else {
	    	fragmentView =  inflater.inflate(R.layout.fragment_history, null);
	        init(fragmentView);
	     
	       
	        }
		
        return fragmentView;
	}
    protected void init(View view){
    	super.init(view);
       
    	title_tx = (TextView)view.findViewById(R.id.title_tx);
    	title_tx.setText(R.string.title_history);
    	adapter  = new HistoryTabAdapter(getActivity().getSupportFragmentManager());
         // 视图切换器
         ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
         pager.setOffscreenPageLimit(1);
         pager.setAdapter(adapter);
         
         // 页面指示器
         PageIndicator indicator = (PageIndicator) view.findViewById(R.id.indicator);
         indicator.setViewPager(pager);

         history_page_time_tx = (TextView) view.findViewById(R.id.history_page_time_tx);
         history_page_time_tx.setText("时间段");
         history_page_time_tx.setOnClickListener(new OnClickListener() {

             @Override
             public void onClick(View v)
             {
//                 Date dt = new Date();
//         		 try {
//         			dt = Util.ORKAN_SIMPLE_DATE_FORMAT.parse(Util.INITIAL_STATUS_HISTORY_STARTTIME);
//         			
//         		 } catch (ParseException e) {
//         			// TODO Auto-generated catch block
//         			e.printStackTrace();
//         		 }
//            	 
//                 new SlideDateTimePicker.Builder(getActivity().getSupportFragmentManager())
//                     .setListener(listener)
//                     .setIndicatorColor(getResources().getColor(R.color.theme_color))
//                     .setInitialDate(dt)
//                     //.setMinDate(minDate)
//                     //.setMaxDate(maxDate)
//                     .setIs24HourTime(true)
//                     //.setTheme(SlideDateTimePicker.HOLO_DARK)
//                     //.setIndicatorColor(Color.parseColor("#990000"))
//                     .build()
//                     .show();
             }
         });
         hourButton = (RadioButton) view.findViewById(R.id.hour);
         dayButton = (RadioButton) view.findViewById(R.id.day);
         weekButton = (RadioButton) view.findViewById(R.id.week);
         group = (RadioGroup)view.findViewById(R.id.history_page_time_radio);
         Util.INITIAL_STATUS_HISTORY_DATAMODE = GetSharedData.GetData(context, "historydatamode", Util.DATA_MODE_HOUR);
         

         //绑定一个匿名监听器
          group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(RadioGroup arg0, int arg1) {
                 // TODO Auto-generated method stub
                 //获取变更后的选中项的ID
            	
    	    	timeOutControl();
                 if(arg1==hourButton.getId()){
                	 Util.d("hour"); 
                	 Util.INITIAL_STATUS_HISTORY_DATAMODE = DATA_MODE_HOUR;
                	 SaveSharedData.SaveData(context, "historydatamode",  Util.DATA_MODE_HOUR);
                 }
                 if(arg1==dayButton.getId()){
                	 Util.d("day"); 
                	 Util.INITIAL_STATUS_HISTORY_DATAMODE = DATA_MODE_DAY;
                	 SaveSharedData.SaveData(context, "historydatamode",  Util.DATA_MODE_DAY);
                 }
                 if(arg1==weekButton.getId()){
                	 Util.d("week"); 
                	 Util.INITIAL_STATUS_HISTORY_DATAMODE = DATA_MODE_WEEK;
                	 SaveSharedData.SaveData(context, "historydatamode",  Util.DATA_MODE_WEEK);
                 }
               
                 initData();
               
             }
         });
          if (Util.INITIAL_STATUS_HISTORY_DATAMODE == Util.DATA_MODE_HOUR){
              
  	       	 hourButton.setChecked(true);
  	       	
        }else if(Util.INITIAL_STATUS_HISTORY_DATAMODE == Util.DATA_MODE_DAY){
     	 
  	       	 dayButton.setChecked(true);
  	       	 
        }else{
     	
  	       	 weekButton.setChecked(true);
  	       	 
        }  
         
    }
    protected void initData(){
    	Util.d("initData");
    	if (Util.INITIAL_STATUS_HISTORY_DATAMODE == Util.DATA_MODE_HOUR){
     		 date = "Hour";

	       	
		  }else if(Util.INITIAL_STATUS_HISTORY_DATAMODE == Util.DATA_MODE_DAY){
		   date = "Day";
		       	
		       	 
		  }else{
		   date = "Week";
		       	 
		  }
    	hud.show();
    	
    	
    	getpm();
    	gettemp();
    	gethum();
    	
   }
 
	private void gethttp(final String type){
			Util.d("date  "+date);
			Util.d(Util.URL_H + "History/" + date + "/" + Util.MQTT_USER_MAC + "/" + type);
		   FinalHttp fh = new FinalHttp();
			AjaxParams params = new AjaxParams();
			params.put("id", Util.USER_ID);
			params.put("token", Util.USER_TOCKEN);
		
			//params.put("sn", MQTTUtil.byte2hex(Util.MQTT_USER_FENG));
			//MAC : f0fe6b320166
			fh.post(Util.URL + "History/" + date + "/" + Util.MQTT_USER_MAC + "/" + type, params, new AjaxCallBack<String>() {
			@Override
			public void onSuccess(String t){
				super.onSuccess(t);
				try {
					Util.d(t);
					JSONObject jsonData = new JSONObject(t);
					int code = jsonData.getInt("code");
					if (code == 1){
						
						
						JSONArray data = jsonData.getJSONArray("data");
						if(data.length()<=1){
							return;
						}
						if(type.equals("pm25")){
							//Util.POINTS_INDOOR_PM .clear();
							Util.CHAR_POINTS_INDOOR_PM.clear();
					 	  }else if(type.equals("temp")){
					 		// Util.POINTS_INDOOR_TEMPERATURE .clear();
					 		Util.CHAR_INDOOR_TEMPERATURE.clear();
					 	  }else if (type.equals("humi")){
					 		// Util.POINTS_INDOOR_HUMIDITY .clear();
					 		 Util.CHAR_POINTS_INDOOR_HUMIDITY.clear();
					 	  }
						
						String minTime = data.getJSONObject(0).getString("time");
						String maxTime = minTime;
						
						Util.HistoryData = data.length();
						
						for(int i=0;i<data.length();i++) {
							JSONObject obj = data.getJSONObject(i);
							String time = obj.getString("time");
							String value = obj.getString("value");

							if(type.equals("pm25")){
								if(date.equals("Day")){
									//if(i%4 == 0)
										Util.CHAR_POINTS_INDOOR_PM.add(new CharData(Float.parseFloat(value), time.replace("日", "-").replace("时", "")));
								}else if(date.equals("Week")){
									Util.CHAR_POINTS_INDOOR_PM.add(new CharData(Float.parseFloat(value), time.replace("月", "/").replace("日", "")));
								}else if(date.equals("Hour")){
									Util.CHAR_POINTS_INDOOR_PM.add(new CharData(Float.parseFloat(value), time.replace("点", ":").replace("分", "")));
								}
								
						 	  }else if(type.equals("temp")){
									if(date.equals("Day")){
									//	if(i%4 == 0)
											Util.CHAR_INDOOR_TEMPERATURE.add(new CharData(Float.parseFloat(value), time.replace("日", "-").replace("时", "")));
									}else if(date.equals("Week")){
										Util.CHAR_INDOOR_TEMPERATURE.add(new CharData(Float.parseFloat(value), time.replace("月", "/").replace("日", "")));
									}else if(date.equals("Hour")){
										Util.CHAR_INDOOR_TEMPERATURE.add(new CharData(Float.parseFloat(value), time.replace("点", ":").replace("分", "")));
									}
						 	  }else if (type.equals("humi")){
									if(date.equals("Day")){
									//	if(i%4 == 0)
											Util.CHAR_POINTS_INDOOR_HUMIDITY.add(new CharData(Float.parseFloat(value), time.replace("日", "-").replace("时", "")));
									}else if(date.equals("Week")){
										Util.CHAR_POINTS_INDOOR_HUMIDITY.add(new CharData(Float.parseFloat(value), time.replace("月", "/").replace("日", "")));
									}else if(date.equals("Hour")){
										Util.CHAR_POINTS_INDOOR_HUMIDITY.add(new CharData(Float.parseFloat(value), time.replace("点", ":").replace("分", "")));
									}
						 	  }
							
				  		}
						Util.INITIAL_STATUS_HISTORY_STARTTIME = minTime;
						Util.INITIAL_STATUS_HISTORY_ENDTIME = maxTime;
						
					 	  Message msg = new Message();
					 	  if(type.equals("pm25")){
					 		  msg.what = MSG_PM;
					 	  }else if(type.equals("temp")){
					 		  msg.what = MSG_TEMP;
					 	  }else if (type.equals("humi")){
					 		  msg.what = MSG_HUMI;
					 	  }
		                  probeHandler.sendMessage(msg);
		               //   Log.v("snake", "min = " + minTime + " max = " + maxTime);
						
					}else{
						Toast.makeText(context, "获取数据失败", 
								Toast.LENGTH_SHORT).show();

					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
						e.printStackTrace();
				}
				cancelAll();
				}
				@Override
				public void onFailure(Throwable t, int errorNo, String strMsg){
					super.onFailure(t, errorNo, strMsg);
					Util.d("fail->>"+ strMsg);
					Toast.makeText(context, "获取数据失败", 
							Toast.LENGTH_SHORT).show();
					cancelAll();
				}
			});
	}
	private void getpm(){
		String type = "pm25";
		gethttp(type);
	}
    
	 private void gettemp(){
		 String type = "temp";
		gethttp(type);
	 }
	 private void gethum(){
		 String type = "humi";
		gethttp(type);
	 }
 
    

    // 按钮点击事件
    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
//            case R.id.char_sta_tx:
//                //启动图表界面
//                Intent it = new Intent(getActivity(), LineChartActivity.class);
//                getActivity().startActivityForResult(it, 1);
//                break;
        }
    }
    

     
    
	 //handler更新UI
    protected class ProbeHandler extends Handler {
        public ProbeHandler() {
        
        }

        @Override
        public void handleMessage(Message msg) {
        	// TODO Auto-generated method stub
    		switch(msg.what){
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