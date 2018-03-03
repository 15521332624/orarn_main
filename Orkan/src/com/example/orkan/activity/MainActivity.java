package com.example.orkan.activity;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.example.orkan.R.drawable;
import com.example.orkan.R.layout;
import com.example.orkan.controller.MQTTController;
import com.example.orkan.controller.MQTTController.MQTTFinish;
import com.example.orkan.dialog.MessageDialog;
import com.example.orkan.fragment.*;
import com.example.orkan.net.UDPSocketServer;
import com.example.orkan.net.UDPWatched;
import com.example.orkan.net.UDPWatcher;
import com.example.orkan.third.kprogresshud.KProgressHUD;
import com.example.orkan.util.MQTTUtil;
import com.example.orkan.util.Util;
import com.example.orkan.view.BezierCurveChart;
import com.example.orkan.view.BezierCurveChart.Point;
import com.example.orkan.R;

/**
 * Main Activity with slidemenu for EAP controller
 *
 * @author libo
 */
public class MainActivity extends BaseActivity implements UDPWatcher, MQTTFinish{
    //定义FragmentTabHost对象
    private FragmentTabHost mTabHost;
    //定义一个布局
    private LayoutInflater layoutInflater;
	KProgressHUD hud;
	Handler handler;
	Handler handler2;
	boolean first = true;
	Runnable r1;
	Runnable r2;
	MessageDialog offlineMsgDialog;
	AlertDialog loginMsgDialog;
	AlertDialog fengjiAlertDialog;
    private MQTTController mqttController;
    //定义数组来存放Fragment界面
    private Class fragmentArray[] = { StateFragment.class,ControlFragment.class,HistoryFragment.class,MoreFragment.class};
    //Tab选项卡的文字
    private int mTextviewArray[] = {R.string.tab_state,
    		R.string.tab_control,
    		R.string.tab_history,
    		R.string.tab_more};
    //Tab选项卡的图片
    private int mImageViewArray[] = {R.drawable.tab_state_index,
                                    R.drawable.tab_control_index,
                                    R.drawable.tab_history_index,
                                    R.drawable.tab_mine_index};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.d("MainActivity oncreate");
 		setContentView(R.layout.activity_main);
 		
 		offlineMsgDialog = new MessageDialog(MainActivity.this,
				getString(R.string.device_out));
 		fengjiAlertDialog = new AlertDialog.Builder(this).create(); 
 		
 		loginMsgDialog = new AlertDialog.Builder(this).create(); 
 		
 		initData();
        initView();
        
        Util.mcontext = MainActivity.this;
    }
    protected void onNewIntent(Intent intent) {

    	   super.onNewIntent(intent);

    	   setIntent(intent);//must store the new intent unless getIntent() will return the old one
    	   Util.d("on new intent");
    }
    protected void initData(){
    	mqttController = MQTTController.getInstance();
    	mqttController.stop();
    	mqttController.addWatcher(this);
    	mqttController.setFinishCallBack(this);
    	beginAll();	
    	mqttController.start();
    	r1 = new Runnable() {
            @Override
            public void run() {
            	//等待连接
//            	byte[] b = {(byte)0x32,(byte)0xff,(byte)0x70,(byte)0x06,
//                		(byte)0x34,(byte)0x47,(byte)0x34,(byte)0x33,(byte)0x16,
//                		(byte)0x41,(byte)0x16,(byte)0x57,(byte)0x01};
            	byte[] b = {};
            	byte code = (byte)0x12;
                mqttController.sendMsg(code, b);
//                mqttController.sendMsg(code, b);
//                mqttController.sendMsg(code, b);
            }
        };
        r2 = new Runnable() {
            @Override
            public void run() {
            	getState();
            	handler2.postDelayed(this,60*1000);
            }
        };
    }
    private void initView() {
        //实例化布局对象
        layoutInflater = LayoutInflater.from(this);
        //实例化TabHost对象，得到TabHost
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
      
        mTabHost.setup(this, getSupportFragmentManager(), R.id.main_tab_content);

        //得到fragment的个数
        int count = fragmentArray.length;

        for (int i = 0; i < count; i++) {
            //为每一个Tab按钮设置图标、文字和内容
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(getResources().getString(mTextviewArray[i])).setIndicator(getTabItemView(i));
            //将Tab按钮添加进Tab选项卡中
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
        }
    }

    private void getState(){
    	FinalHttp fh = new FinalHttp();
		AjaxParams params = new AjaxParams();
		params.put("id", Util.USER_ID);
		params.put("token", Util.USER_TOCKEN);
		params.put("mac", Util.MQTT_USER_MAC);
		Util.d("params  " +params);
		fh.post(Util.URL+"Device/line", params, new AjaxCallBack<String>() {
		@Override
		public void onSuccess(String t){
			super.onSuccess(t);
			try {
				Util.d("getstate  " +t);
				JSONObject jsonData = new JSONObject(t);
				int code = jsonData.getInt("code");
				if (code == 1){
					JSONArray data = jsonData.getJSONArray("data");
					if (data.length()<1){
						if(!offlineMsgDialog.isShowing())
						offlineMsgDialog.show();
						return;
					}
					JSONObject obj = data.getJSONObject(0);
					String mac = obj.getString("mac");
					boolean state = obj.getBoolean("state");
					if (mac.equals(Util.MQTT_USER_MAC) && state == true){
						return;
					}else{
						if(!offlineMsgDialog.isShowing())
						offlineMsgDialog.show();
						return;
					}
					
				}else{
					if(!loginMsgDialog.isShowing()){

						loginMsgDialog.show();  
		            	Window window = loginMsgDialog.getWindow();  
		            	window.setContentView(R.layout.dialog_samelogin);  
		            	TextView alert_btn_title = (TextView)window.findViewById(R.id.alert_btn_title);
		            	alert_btn_title.setText(R.string.login_other_device);
		            	Button reboot_cancel_alert_btn = (Button)window.findViewById(R.id.reboot_cancel_alert_btn);
		            	reboot_cancel_alert_btn.setOnClickListener(new OnClickListener(){
		                    @Override
		                    public void onClick(View v)
		                    {
		                    	loginMsgDialog.cancel();  
		                    	startActivity(new Intent(MainActivity.this,LoginActivity.class));	    
		                    	MainActivity.this.finish();
		                    }
		                });
		            	Button reboot_confirm_alert_btn = (Button)window.findViewById(R.id.reboot_confirm_alert_btn);
		            	reboot_confirm_alert_btn.setOnClickListener(new OnClickListener(){
		                    @Override
		                    public void onClick(View v)
		                    {
		                    	loginMsgDialog.cancel();  	
		                    }
		                });
					}
					//startActivity(new Intent(MainActivity.this,LoginActivity.class));
					//MainActivity.this.finish();
					return;
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg){
				Util.d("faild-->"+strMsg);
				super.onFailure(t, errorNo, strMsg);
				
				
			}
		});
    }
    
    /**
     * 给Tab按钮设置图标和文字
     */
    private View getTabItemView(int index){
        View view = layoutInflater.inflate(R.layout.layout_tab_bottom, null);

        ImageView imageView = (ImageView) view.findViewById(R.id.tab_bottom_im);
        imageView.setImageResource(mImageViewArray[index]);
        TextView textView = (TextView) view.findViewById(R.id.tab_bottom_tx);
        textView.setText(mTextviewArray[index]);

        return view;
    }
    
    public void onBackPressed() { 
    	
    	final AlertDialog alertDialog = new AlertDialog.Builder(this).create();  
    	alertDialog.show();  
    	Window window = alertDialog.getWindow();  
    	window.setContentView(R.layout.dialog_quit);  
    	Button reboot_cancel_alert_btn = (Button)window.findViewById(R.id.reboot_cancel_alert_btn);
    	reboot_cancel_alert_btn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v)
            {
            	if (mqttController!=null){
            		mqttController.removeWatcher(MainActivity.this);
            		mqttController.stop();
            		mqttController = null;
    		 	}
            	alertDialog.dismiss();
                MainActivity.this.finish(); 
                System.exit(0);
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

	@Override
	public void getUDPMessage(int code, byte[] data, int len) {
		// TODO Auto-generated method stub
		Util.d("main get message "+code);
		if(code == 0x0d){
			if(fengjiAlertDialog.isShowing()){
				return;
			}
			if(len>=12){
				if(data[0]== (byte)0xff && data[1] == (byte)0xff &&
						data[2]== (byte)0xff && data[3] == (byte)0xff){
					
					fengjiAlertDialog.show();  
	            	Window window = fengjiAlertDialog.getWindow();  
	            	window.setContentView(R.layout.dialog_quit);  
	            	TextView alert_btn_title = (TextView)window.findViewById(R.id.alert_btn_title);
	            	alert_btn_title.setText(R.string.not_connect_fan_device);
	            	Button reboot_cancel_alert_btn = (Button)window.findViewById(R.id.reboot_cancel_alert_btn);
	            	reboot_cancel_alert_btn.setOnClickListener(new OnClickListener(){
	                    @Override
	                    public void onClick(View v)
	                    {
	                    	fengjiAlertDialog.cancel();  
	                    	byte[] b = {};
	                    	byte code = (byte)0x13;
	                        mqttController.sendMsg(code, b); 	
	                        beginAll();
	                    }
	                });
	            	Button reboot_confirm_alert_btn = (Button)window.findViewById(R.id.reboot_confirm_alert_btn);
	            	reboot_confirm_alert_btn.setOnClickListener(new OnClickListener(){
	                    @Override
	                    public void onClick(View v)
	                    {
	                    	fengjiAlertDialog.cancel();  	
	                    }
	                });
				}else{
					Util.d("get MQTT_USER_FENG ");
					Util.MQTT_USER_FENG = MQTTUtil.subBytes(data, 0, 12);
				 	byte[] b1 = Util.MQTT_USER_FENG;
					//获取新风机状态
					mqttController.sendMsg((byte)0x03, b1);
				}
			}
			
		}else if(code == 0x0d){
			//新风机配对
			cancelAll();
			MessageDialog msgDialog = new MessageDialog(this,
					getString(R.string.pair_success));
			msgDialog.show();
		}
		
	}
	 @Override
	    public void onDestroy(){
		 	super.onDestroy();
		 //	System.exit(0);
	    }

    
	 
	 
	/*
	 *  timebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimeWheel(MainActivity.this, new TimeListener() {
                    @Override
                    public void getTime(int[] time) {
                        //time[0]  start hour
                        //time[1]  start minute
                        //time[2]  endhour
                        //time[3]  end minute
                        timetv.setText(getTimeStr(time));
                    }
                });
            }
        });
    }

    private String getTimeStr(int[] st) {
        return new StringBuilder()
                .append((st[0] < 10) ? ("0" + st[0]) : st[0]).append(":")
                .append((st[1] < 10) ? ("0" + st[1]) : st[1])
                .append(" ～ ")
                .append((st[2] < 10) ? ("0" + st[2]) : st[2]).append(":")
                .append((st[3] < 10) ? ("0" + st[3]) : st[3])
                .toString();
    }

	 */

	 public void finish(){
		 if (mqttController!=null){
		 		mqttController.stop();
		 		mqttController = null;
		 	}
		 if(handler !=null){
			 handler.removeCallbacks(r1);
		 }
		 if(handler2 !=null){
			 handler2.removeCallbacks(r2);
		 }
	    	super.finish();
	    }
	
	@Override
	public void MQTTFinish() {
		// TODO Auto-generated method stub
		cancelAll();
		Util.d("MQTT FINISH");
		if (first){
			handler = new Handler();
	        handler.postDelayed(r1, 10);
	        if (Util.START_FROM_LOGIN == 1){
		        getState();
		        handler2 = new Handler();
		        handler2.postDelayed(r2, 60*1000);
	        }
		}
		first = false;
		
	}
}
