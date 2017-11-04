package com.example.orkan.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.orkan.R;
import com.example.orkan.activity.LoginActivity;
import com.example.orkan.activity.MainActivity;
import com.example.orkan.activity.TimingListActivity;
import com.example.orkan.controller.MQTTController;
import com.example.orkan.dialog.MessageDialog;
import com.example.orkan.fragment.StateFragment.ProbeHandler;
import com.example.orkan.net.UDPSocketServer;
import com.example.orkan.net.UDPWatcher;
import com.example.orkan.third.kprogresshud.KProgressHUD;
import com.example.orkan.util.MQTTUtil;
import com.example.orkan.util.Util;
import com.example.orkan.view.CustomProgressBar;
import com.example.orkan.view.SwitchView;
import com.example.orkan.view.SwitchView.OnStateChangedListener;
import com.example.orkan.view.slidedatetimepicker.CustomTimePicker;
import com.example.orkan.view.slidedatetimepicker.SlideDateTimeListener;
import com.example.orkan.view.slidedatetimepicker.SlideDateTimePicker;

public class ControlFragment extends BaseTabFragment implements View.OnClickListener,UDPWatcher{
	private View fragmentView;
	TextView title_tx;
	KProgressHUD hud;
	private Button test_button;
	CustomProgressBar inside_filter_progressbar;
	CustomProgressBar middle_filter_progressbar;
	CustomProgressBar outside_filter_progressbar;
	TextView inside_filter_warning;
	TextView middle_filter_warning;
	TextView outside_filter_warning;
	TextView control_timing_open;
	RelativeLayout control_timing_li;
	TextView fengsu_tx;
	ToggleButton control_opendoor;
	ImageView control_wind_im2;
	ImageView control_wind_im;
	ImageView control_wind_im1;
	private Handler control_handler = new Handler();
	private Handler control_ver_handler = new Handler();
	ImageView feng_up;
	ImageView feng_down;
	
	boolean autoclick = false;
	
	private int control_feng_num = 0;
	private int control_ver_feng_num = 0;
	
	ImageView switchmodeIm;
	//protected UDPSocketServer mProbeSocketDiscover;
	
	protected MQTTController mqttController;
	
	ImageView newwindmodeIm;
	private int newwindmodeImArray[] = {R.drawable.control_newopen0,R.drawable.control_newopen1,R.drawable.control_newopen2};	
	private int switchmodeImArray[] = {R.drawable.control_open0,	R.drawable.control_open1,R.drawable.control_open2,R.drawable.control_open3};	
	 
	private int fengStateArray[] = {R.id.feng_state_1,R.id.feng_state_2,R.id.feng_state_3,R.id.feng_state_4,
			   R.id.feng_state_5,R.id.feng_state_6};
			 private View[] fengStateViewArray = new View[6];
			 
	private ProbeHandler probeHandler = new ProbeHandler();
    private SimpleDateFormat mFormatter = new SimpleDateFormat("HH:mm");
	private SlideDateTimeListener listener = new SlideDateTimeListener() {

       @Override
       public void onDateTimeSet(Date date)
       {
           Toast.makeText(getActivity(),
                   mFormatter.format(date), Toast.LENGTH_SHORT).show();
       }

       // Optional cancel listener
	    @Override
	    public void onDateTimeCancel()
	    {
//	        Toast.makeText(getActivity(),
//	                "Canceled", Toast.LENGTH_SHORT).show();
	    }
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//注册观察者
		mqttController = MQTTController.getInstance();
    	mqttController.addWatcher(this);
		
    	
    	byte[] b1 = Util.MQTT_USER_FENG;
		//获取新风机状态
		mqttController.sendMsg((byte)0x03, b1);
		getLvxin();
//    	final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//            	//每两秒获取一次
//            	byte[] b1 = Util.MQTT_USER_FENG;
//        		//获取新风机状态
//        		mqttController.sendMsg((byte)0x03, b1);
//                handler.postDelayed(this, 2000);
//            }
//        }, 2000);
    	
    	
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
    	fragmentView =  inflater.inflate(R.layout.fragment_control, null);
    
    	
        init(fragmentView);
        initData();
	    
       return fragmentView;
	}
    protected void init(View view){
    	super.init(view);
    	for(int i=0;i<6;i++){
    	      fengStateViewArray[i] = (View)view.findViewById(fengStateArray[i]);
    	     }
    	title_tx = (TextView)view.findViewById(R.id.title_tx);
    	title_tx.setText(R.string.title_state);
    	inside_filter_warning = (TextView)view.findViewById(R.id.inside_filter_warning);
    	middle_filter_warning = (TextView)view.findViewById(R.id.middle_filter_warning);
    	outside_filter_warning = (TextView)view.findViewById(R.id.outside_filter_warning);  
    	control_wind_im2 = (ImageView)view.findViewById(R.id.control_wind_im2);
    	control_wind_im = (ImageView)view.findViewById(R.id.control_wind_im);
    	control_wind_im1 = (ImageView)view.findViewById(R.id.control_wind_im1);
    	fengsu_tx = (TextView)view.findViewById(R.id.fengsu_tx);
    	control_timing_open = (TextView)view.findViewById(R.id.control_timing_open);
    	control_opendoor  = (ToggleButton)view.findViewById(R.id.control_door_im);
    	control_opendoor.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {	
            	 if (!autoclick){
 					byte[] b1 = Util.MQTT_USER_FENG;
 					byte[] b2 = {(byte)0x01};	
 					byte[] b = MQTTUtil.addBytes(b1, b2);
 					mqttController.sendMsg((byte)0x08, b);
 					
 					beginAll();
     				
                 }else{
                 
     	    		byte[] b1 = Util.MQTT_USER_FENG;
 					byte[] b2 = {(byte)0x00};
 					byte[] b = MQTTUtil.addBytes(b1, b2);
 					mqttController.sendMsg((byte)0x08, b);
 					beginAll();
                 }
            	
            }
        });

 
    	
    	switchmodeIm = (ImageView)view.findViewById(R.id.control_open_im);
    	switchmodeIm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {


                if (Util.INITIAL_STATUS_OPEN == 0){
                	//需要开机
					byte[] b1 = Util.MQTT_USER_FENG;
					byte[] b2 = {(byte)0x01};
					byte[] b = MQTTUtil.addBytes(b1, b2);
					mqttController.sendMsg((byte)0x05, b);
					
					beginAll();
    				
                }else{
                	//关机
    	    		byte[] b1 = Util.MQTT_USER_FENG;
					byte[] b2 = {(byte)0x00};
					byte[] b = MQTTUtil.addBytes(b1, b2);
					mqttController.sendMsg((byte)0x05, b);
					beginAll();
                }
            	
            	
               
            }
        });
    	
    	feng_up = (ImageView)view.findViewById(R.id.feng_up);
    	feng_up.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	Util.d("INITIAL_STATUS_SWITCHMODE "+ Util.INITIAL_STATUS_SWITCHMODE);
            	if (Util.INITIAL_STATUS_OPEN ==0 ){
            		MessageDialog msgDialog = new MessageDialog(getActivity(),
							"请先打开总开关");
					msgDialog.show();
            		
            		return;
            	}
            	 if (Util.INITIAL_STATUS_SWITCHMODE >= 3){
                 	
 	    	    	}else{
                 	byte[] b1 = Util.MQTT_USER_FENG;
             		byte[] b2 = new byte[1];
             		b2[0] = (byte)(Util.INITIAL_STATUS_SWITCHMODE+1);
             		byte[] b = MQTTUtil.addBytes(b1, b2);
             		mqttController.sendMsg((byte)0x06, b);
     				
             		beginAll();
 	    	    	}
            }
        });
    	feng_down = (ImageView)view.findViewById(R.id.feng_down);
    	feng_down.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	if (Util.INITIAL_STATUS_OPEN ==0 ){
            		MessageDialog msgDialog = new MessageDialog(getActivity(),
							"请先打开总开关");
					msgDialog.show();
            		return;
            	}
            	 if (Util.INITIAL_STATUS_SWITCHMODE <= 1){
              
 	    	    	}else{
                 	byte[] b1 = Util.MQTT_USER_FENG;
             		byte[] b2 = new byte[1];
             		b2[0] = (byte)(Util.INITIAL_STATUS_SWITCHMODE-1);
             		byte[] b = MQTTUtil.addBytes(b1, b2);
             		mqttController.sendMsg((byte)0x06, b);
     				
             		beginAll();
 	    	    	}
            }
        });
    	
    	newwindmodeIm = (ImageView)view.findViewById(R.id.control_newopen);
    	newwindmodeIm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	if (Util.INITIAL_STATUS_OPEN == 0){
            		MessageDialog msgDialog = new MessageDialog(getActivity(),
							"请先打开总开关");
					msgDialog.show();
            		return;
            	}
            	
            	if (Util.INITIAL_STATUS_XINFENG_OPEN == 0){
                	byte[] b1 = Util.MQTT_USER_FENG;
            		byte[] b2 = {0x01};
            		byte[] b = MQTTUtil.addBytes(b1, b2);
            		mqttController.sendMsg((byte)0x11, b);
					
					beginAll();
    				
                }else{
                  	byte[] b1 = Util.MQTT_USER_FENG;
            		byte[] b2 = {0x00};
            		byte[] b = MQTTUtil.addBytes(b1, b2);
            		mqttController.sendMsg((byte)0x11, b);
            		beginAll();
                }
            }
        });
    	
    	//内层
    	inside_filter_progressbar = (CustomProgressBar) view.findViewById(R.id.inside_filter_progressbar);
    	inside_filter_progressbar.setMaxProgress(100);
    	inside_filter_progressbar.setProgressColor(Color.parseColor("#31c6bd"));
    	
    	inside_filter_progressbar.setClickable(true);
    	inside_filter_progressbar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();  
            	alertDialog.show();  
            	Window window = alertDialog.getWindow();  
            	window.setContentView(R.layout.dialog_quit);  
            	TextView alert_btn_title = (TextView)window.findViewById(R.id.alert_btn_title);
            	alert_btn_title.setText("真的要重置内层滤网时间么？");
            	Button reboot_cancel_alert_btn = (Button)window.findViewById(R.id.reboot_cancel_alert_btn);
            	reboot_cancel_alert_btn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v)
                    {
                    	updateLvxin(3);
                    	alertDialog.cancel(); 
                    }
                });
            	Button reboot_confirm_alert_btn = (Button)window.findViewById(R.id.reboot_confirm_alert_btn);
            	reboot_confirm_alert_btn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v)
                    {
                    	alertDialog.cancel();  	
                    }
                });
            
            }
        });
    	
    	//inside_filter_progressbar.setCurProgress(70,2000);
    	//中层
    	middle_filter_progressbar = (CustomProgressBar) view.findViewById(R.id.middle_filter_progressbar);
    	middle_filter_progressbar.setMaxProgress(100);
    	middle_filter_progressbar.setProgressColor(Color.parseColor("#31c6bd"));
    	
    	middle_filter_progressbar.setClickable(true);
    	middle_filter_progressbar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();  
            	alertDialog.show();  
            	Window window = alertDialog.getWindow();  
            	window.setContentView(R.layout.dialog_quit);  
            	TextView alert_btn_title = (TextView)window.findViewById(R.id.alert_btn_title);
            	alert_btn_title.setText("真的要重置中层滤网时间么？");
            	Button reboot_cancel_alert_btn = (Button)window.findViewById(R.id.reboot_cancel_alert_btn);
            	reboot_cancel_alert_btn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v)
                    {
                    	updateLvxin(2);
                    	alertDialog.cancel(); 
                    }
                });
            	Button reboot_confirm_alert_btn = (Button)window.findViewById(R.id.reboot_confirm_alert_btn);
            	reboot_confirm_alert_btn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v)
                    {
                    	alertDialog.cancel();  	
                    }
                });
            }
        });
    	
    	//middle_filter_progressbar.setCurProgress(60,2000);
    	//外层
    	outside_filter_progressbar = (CustomProgressBar) view.findViewById(R.id.outside_filter_progressbar);
    	outside_filter_progressbar.setMaxProgress(100);
    	outside_filter_progressbar.setProgressColor(Color.parseColor("#31c6bd"));
    	//outside_filter_progressbar.setCurProgress(70,2000);
    	outside_filter_progressbar.setClickable(true);
    	outside_filter_progressbar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();  
            	alertDialog.show();
            	Window window = alertDialog.getWindow();  
            	window.setContentView(R.layout.dialog_quit);  
            	TextView alert_btn_title = (TextView)window.findViewById(R.id.alert_btn_title);
            	alert_btn_title.setText("真的要重置外层滤网时间么？");
            	Button reboot_cancel_alert_btn = (Button)window.findViewById(R.id.reboot_cancel_alert_btn);
            	reboot_cancel_alert_btn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v)
                    {
                    	updateLvxin(1);
                    	alertDialog.cancel(); 
                    }
                });
            	Button reboot_confirm_alert_btn = (Button)window.findViewById(R.id.reboot_confirm_alert_btn);
            	reboot_confirm_alert_btn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v)
                    {
                    	alertDialog.cancel();  	
                    }
                });
            }
        });

    	control_timing_li = (RelativeLayout)view.findViewById(R.id.control_timing_li);
    	control_timing_li.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	 Intent intent = new Intent(getActivity(), TimingListActivity.class);
                 startActivity(intent);
            }
        });
    }
    
    protected void updateLvxin(int ln){
    	 beginAll();
    	 FinalHttp fh = new FinalHttp();
         AjaxParams params = new AjaxParams();
         params.put("id", Util.USER_ID);
 		 params.put("token", Util.USER_TOCKEN);
 		 params.put("sn", MQTTUtil.byte2hexNospace(Util.MQTT_USER_FENG));
 		 params.put("num", ln+"");
 		//params.put("sn", "48ff70067577525009352581");
 		Util.d(MQTTUtil.byte2hexNospace(Util.MQTT_USER_FENG));
 		fh.post(Util.URL+"/FreshAir/timeToZero", params, new AjaxCallBack<String>() {
 			@Override
 			public void onSuccess(String t){
 				super.onSuccess(t);
 				try {
 					Util.d("timeToZero  " +t);
 					JSONObject jsonData = new JSONObject(t);
 					int code = jsonData.getInt("code");
 					if (code == 1){
 						MessageDialog msgDialog = new MessageDialog(getActivity(),
								jsonData.getString("msg"));
						msgDialog.show();
						getLvxin();
						
 					}else{
 						MessageDialog msgDialog = new MessageDialog(getActivity(),
								jsonData.getString("msg"));
						msgDialog.show();
 						
 					}
 					cancelAll();
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				@Override
 				public void onFailure(Throwable t, int errorNo, String strMsg){
 					Util.d("getFreshAir faild->>" + strMsg);
 					super.onFailure(t, errorNo, strMsg);
 				}
 		});
    }
    
    
    protected void getLvxin(){
    	
//   	 	if (Util.MQTT_USER_FENG.length < 12){
//   	 		Util.d("error MQTT_USER_FENG length");
//   	 		return;
//   	 	}
   	 Util.d("getLvxin");
        FinalHttp fh = new FinalHttp();
        AjaxParams params = new AjaxParams();
        params.put("id", Util.USER_ID);
		params.put("token", Util.USER_TOCKEN);
		params.put("sn", MQTTUtil.byte2hexNospace(Util.MQTT_USER_FENG));
		//params.put("sn", "48ff70067577525009352581");
		Util.d("MQTT_USER_FENG  "+MQTTUtil.byte2hexNospace(Util.MQTT_USER_FENG));
		fh.post(Util.URL+"/FreshAir/getFreshAir", params, new AjaxCallBack<String>() {
			@Override
			public void onSuccess(String t){
				super.onSuccess(t);
				try {
					Util.d("getFreshAir  " +t);
					JSONObject jsonData = new JSONObject(t);
					int code = jsonData.getInt("code");
					if (code == 1){
						JSONArray data = jsonData.getJSONArray("data");
						if (data.length()<1){
							return;
						}
						JSONObject obj = data.getJSONObject(0);
						Util.INITIAL_STATUS_INSIDEFILTER_VALUE = obj.getInt("time3");
						Util.INITIAL_STATUS_MIDDLEFILTER_VALUE = obj.getInt("time2");
						Util.INITIAL_STATUS_OUTSIDEFILTER_VALUE = obj.getInt("time1");
						initData();
					}else{
						Toast.makeText(getActivity(), "获取滤网信息失败", 
								Toast.LENGTH_SHORT).show();
						return;
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				@Override
				public void onFailure(Throwable t, int errorNo, String strMsg){
					Util.d("getFreshAir faild->>" + strMsg);
					super.onFailure(t, errorNo, strMsg);
				}
		});
   }
    
    protected void initData(){

    	//初始化数据
    	
    	int v3 = (int)(((double)(Util.INITIAL_STATUS_INSIDEFILTER_VALUE_MAX - Util.INITIAL_STATUS_INSIDEFILTER_VALUE)/Util.INITIAL_STATUS_INSIDEFILTER_VALUE_MAX)*100);
    	int v2 = (int)(((double)(Util.INITIAL_STATUS_MIDDLEFILTER_VALUE_MAX - Util.INITIAL_STATUS_MIDDLEFILTER_VALUE)/Util.INITIAL_STATUS_MIDDLEFILTER_VALUE_MAX)*100);
    	int v1 = (int)(((double)(Util.INITIAL_STATUS_OUTSIDEFILTER_VALUE_MAX - Util.INITIAL_STATUS_OUTSIDEFILTER_VALUE)/Util.INITIAL_STATUS_OUTSIDEFILTER_VALUE_MAX)*100);
    	if (v1 < 0){
    		v1 =0;
    	}
    	if (v2 < 0){
    		v2 =0;
    	}
    	if (v3 < 0){
    		v3 =0;
    	}
		inside_filter_progressbar.setCurProgress(v1,100);
		middle_filter_progressbar.setCurProgress(v2,100);
		outside_filter_progressbar.setCurProgress(v3,100);
		if ( Util.INITIAL_STATUS_AUTOMODE == 1){
			autoclick = true;
			control_opendoor.setChecked(true);
		}else{
			autoclick = false;
			control_opendoor.setChecked(false);
		}
		
		if (Util.INITIAL_STATUS_TIMING == 1){
			control_timing_open.setText("开启");
			control_timing_open.setTextColor(getActivity().getResources().getColor(R.color.theme_color));
		}else{
			control_timing_open.setText("关闭");
			control_timing_open.setTextColor(getActivity().getResources().getColor(R.color.lb_tx));
		}
		
	   	Util.d(Util.INITIAL_STATUS_OPEN + "Util.INITIAL_STATUS_OPEN");
		if (Util.INITIAL_STATUS_OPEN == 0){
			switchmodeIm.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.switch_close));
			
        }else{
        	switchmodeIm.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.switch_open));
        }
		
		if(Util.INITIAL_STATUS_XINFENG_OPEN == 0){
			newwindmodeIm.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.switch_close));
		}else{
			newwindmodeIm.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.switch_open));
		}
		control_feng_num = 0;
		control_ver_feng_num = 0;
		control_handler.removeCallbacks(runnablefeng);
		control_handler.removeCallbacks(runnablefengleft);
		control_ver_handler.removeCallbacks(runnablefengver);
		if(Util.INITIAL_STATUS_OPEN == 1 && Util.INITIAL_STATUS_XINFENG_OPEN == 1){
			control_handler.postDelayed(runnablefeng, 500);
			control_ver_handler.postDelayed(runnablefengver,500);
		}else if (Util.INITIAL_STATUS_OPEN == 1 && Util.INITIAL_STATUS_XINFENG_OPEN == 0){
			control_handler.postDelayed(runnablefengleft, 500);
			control_ver_handler.postDelayed(runnablefengver,500);
		}else {
			control_wind_im2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.control_feng0));	
		}
		
		if ( Util.INITIAL_STATUS_SWITCHMODE ==1){
			fengsu_tx.setText("风速:一级");
		}
		if ( Util.INITIAL_STATUS_SWITCHMODE ==2){
			fengsu_tx.setText("风速:二级");
		}
		if ( Util.INITIAL_STATUS_SWITCHMODE ==3){
			fengsu_tx.setText("风速:三级");
		}

		if (v3 < 20){
			inside_filter_progressbar.setProgressColor(Color.parseColor("#FF5E2B"));
			inside_filter_warning.setVisibility(View.VISIBLE);
		}else if (v3 < 60){
			inside_filter_progressbar.setProgressColor(Color.parseColor("#F5BC20"));
		}else {
			inside_filter_progressbar.setProgressColor(Color.parseColor("#31c6bd"));
		}
		
		if (v2 < 20){
			middle_filter_progressbar.setProgressColor(Color.parseColor("#FF5E2B"));
			middle_filter_warning.setVisibility(View.VISIBLE);
		}else if (v2 < 60){
			middle_filter_progressbar.setProgressColor(Color.parseColor("#F5BC20"));
		}else {
			middle_filter_progressbar.setProgressColor(Color.parseColor("#31c6bd"));
		}
		
		if (v1 < 20){
			outside_filter_progressbar.setProgressColor(Color.parseColor("#FF5E2B"));
			outside_filter_warning.setVisibility(View.VISIBLE);
		}else if (v1 < 60){
			outside_filter_progressbar.setProgressColor(Color.parseColor("#F5BC20"));
		}else {
			outside_filter_progressbar.setProgressColor(Color.parseColor("#31c6bd"));
		}
		
		//temp
		  if(Util.INITIAL_STATUS_FENG_NUM > 6){
		   Util.INITIAL_STATUS_FENG_NUM = 6;
		  }
		//  Util.INITIAL_STATUS_FENG_NUM = 5;
		  for(int i =0;i<Util.INITIAL_STATUS_FENG_NUM;i++){
		   fengStateViewArray[i].setVisibility(View.VISIBLE);
		   Util.d("i " + i + " " + Util.INITIAL_STATUS_FENG_STATE[i]);
		   if(Util.INITIAL_STATUS_FENG_STATE[i] !=0){
		    fengStateViewArray[i].setBackground(getActivity().getResources().getDrawable(R.drawable.feng_state_rect));
		   }else{
		    fengStateViewArray[i].setBackground(getActivity().getResources().getDrawable(R.drawable.feng_state_rect_un));
		   }
		  }
   }
    //handler更新UI
    protected class ProbeHandler extends Handler {
        public ProbeHandler() {
        
        }
        
        @Override
        public void handleMessage(Message msg) {
        	JSONObject jsonData= (JSONObject)msg.obj;
        	Util.d("control rev :" +jsonData);
        	// TODO Auto-generated method stub
    		switch(msg.what){
    		case Util.INSTANCE_CODE_GETSTATUS_REV:
    			
    			break;
    		}
        }
    }
	@Override
	public void getUDPMessage(int code, byte[] data, int len) {
		
		if(code == 0x04){
			cancelAll();
//			if(data.length < 15 || len < 15){
//				return;
//			}
			
			//Util.MQTT_USER_FENG = MQTTUtil.subBytes(data, 0, 12);
			Util.INITIAL_STATUS_OPEN = data[12] & 0xff;
			Util.INITIAL_STATUS_SWITCHMODE = (data[13] & 0xff);
//			//错误数据时当1处理
//			if(Util.INITIAL_STATUS_SWITCHMODE == 0){
//				Util.INITIAL_STATUS_SWITCHMODE  =1;
//			}
			int data14 = data[14] & 0xff;
			
		
			if(data14 == 0){
				Util.INITIAL_STATUS_XINFENG_OPEN = 0;
				
			}else{
				Util.INITIAL_STATUS_XINFENG_OPEN = 1;
			}
			
			Util.d("Util.INITIAL_STATUS_SWITCHMODE " +Util.INITIAL_STATUS_SWITCHMODE);
			Util.INITIAL_STATUS_AUTOMODE = data[15] & 0xff;
			Util.INITIAL_STATUS_TIMING = data[16] & 0xff;
			
			if(data.length == 20){
			    if((data[17] & 0x80)>0){
			     Util.INITIAL_STATUS_FENG_STATE[15] = 1;
			    }
			    if((data[17] & 0x40)>0){
			     Util.INITIAL_STATUS_FENG_STATE[14] = 1;
			    }
			    if((data[17] & 0x20)>0){
			     Util.INITIAL_STATUS_FENG_STATE[13] = 1;
			    }
			    if((data[17] & 0x10)>0){
			     Util.INITIAL_STATUS_FENG_STATE[12] = 1;
			    }
			    if((data[17] & 0x08)>0){
			     Util.INITIAL_STATUS_FENG_STATE[11] = 1;
			    }
			    if((data[17] & 0x04)>0){
			     Util.INITIAL_STATUS_FENG_STATE[10] = 1;
			    }
			    if((data[17] & 0x02)>0){
			     Util.INITIAL_STATUS_FENG_STATE[9] = 1;
			    }
			    if((data[17] & 0x01)>0){
			     Util.INITIAL_STATUS_FENG_STATE[8] = 1;
			    }
			    if((data[18] & 0x80)>0){
			     Util.INITIAL_STATUS_FENG_STATE[7] = 1;
			    }
			    if((data[18] & 0x40)>0){
			     Util.INITIAL_STATUS_FENG_STATE[6] = 1;
			    }
			    if((data[18] & 0x20)>0){
			     Util.INITIAL_STATUS_FENG_STATE[5] = 1;
			    }
			    if((data[18] & 0x10)>0){
			     Util.INITIAL_STATUS_FENG_STATE[4] = 1;
			    }
			    if((data[18] & 0x08)>0){
			     Util.INITIAL_STATUS_FENG_STATE[3] = 1;
			    }
			    if((data[18] & 0x04)>0){
			     Util.INITIAL_STATUS_FENG_STATE[2] = 1;
			    }
			    if((data[18] & 0x02)>0){
			     Util.INITIAL_STATUS_FENG_STATE[1] = 1;
			    }
			    if((data[18] & 0x01)>0){
			     Util.INITIAL_STATUS_FENG_STATE[0] = 1;
			    }
			    
			    Util.INITIAL_STATUS_FENG_NUM = data[19] & 0xff;
			    if(Util.INITIAL_STATUS_FENG_NUM > 16){
			     Util.INITIAL_STATUS_FENG_NUM = 16;
			    }
			   }
			
			initData();
				
	
    	}
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
	Runnable runnablefeng=new Runnable() { 
		
		
	    @Override  
	    public void run() {  
	    	
	    	if(control_feng_num == 0){
	    		control_wind_im2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.control_feng0));		
	    	}else if(control_feng_num == 1){
	    		control_wind_im2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.control_feng1));	
	    	}else if(control_feng_num == 2){
	    		control_wind_im2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.control_feng2));	
	    	}else if(control_feng_num == 3){
	    		control_wind_im2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.control_feng3));	
	    	}else if(control_feng_num == 4){
	    		control_wind_im2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.control_feng4));	
	    	}

	        //要做的事情  
	    	control_feng_num = (control_feng_num+1)%5;
	        control_handler.postDelayed(this, 500);
	    }
	};
	
	Runnable runnablefengver =new Runnable() { 

		
	    @Override  
	    public void run() {  
	    	
	    	if(control_ver_feng_num == 0){
	    		control_wind_im.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.control_ver_feng0));	
	    		control_wind_im1.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.control_ver_feng0));
	    	}else if(control_ver_feng_num == 1){
	    		control_wind_im.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.control_ver_feng1));	
	    		control_wind_im1.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.control_ver_feng1));
	    	}

	        //要做的事情  
	    	control_ver_feng_num = (control_ver_feng_num+1)%2;
	        control_ver_handler.postDelayed(this, 500);  
	    	
	    }  
	};  
	
	
	Runnable runnablefengleft=new Runnable() { 

		
	    @Override  
	    public void run() {  
	    	
	    	if(control_feng_num == 0){
	    		control_wind_im2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.control_feng0));		
	    	}else if(control_feng_num == 1){
	    		control_wind_im2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.control_fengleft1));	
	    	}else if(control_feng_num == 2){
	    		control_wind_im2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.control_fengleft2));	
	    	}else if(control_feng_num == 3){
	    		control_wind_im2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.control_fengleft3));	
	    	}else if(control_feng_num == 4){
	    		control_wind_im2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.control_fengleft4));	
	    	}

	        //要做的事情  
	    	control_feng_num = (control_feng_num+1)%5;
	        control_handler.postDelayed(this, 500);  
	    	
	    }  
	};  
	 @Override
	    public void onDestroy(){
		 Util.d("control destroy");
			control_handler.removeCallbacks(runnablefeng);
			control_handler.removeCallbacks(runnablefengleft);
			control_ver_handler.removeCallbacks(runnablefengver);
		 	super.onDestroy();
		 //	System.exit(0);
	    }
}