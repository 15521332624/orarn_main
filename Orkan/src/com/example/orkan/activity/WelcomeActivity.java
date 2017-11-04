package com.example.orkan.activity;

/**
 * Created by admin on 2015/9/29.
 */
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.orkan.R;
import com.example.orkan.controller.MainController;
import com.example.orkan.net.UDPSocketServer;
import com.example.orkan.net.UDPWatcher;
import com.example.orkan.net.WiFiController;
import com.example.orkan.third.citypicker.SystemUtils;
import com.example.orkan.util.GetSharedData;
import com.example.orkan.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class WelcomeActivity extends BaseActivity implements UDPWatcher{

    private int TIME = 1500;  
    //udp检测socket
    protected UDPSocketServer mProbeSocketDiscover;
    WiFiController wiFiController;
    MainController mainController;
    
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
  
        //初始化wifi控制器
     
        wiFiController = WiFiController.getInstance(this);
        wiFiController.init();
        SystemUtils.copyDB(this);
  
        //if (wiFiController.isWifiEnabled()){
//        if(true){
//        	//初始化历史数据控制器
        	mainController = MainController.getInstance(this);
            mainController.init();
//
//            mProbeSocketDiscover = UDPSocketServer.getInstance();
//            mProbeSocketDiscover.startUDPSocketThread(wiFiController.getLocalIpAddress());
//            mProbeSocketDiscover.addWatcher(this);
//            mProbeSocketDiscover.sendDiscover();
            handler.postDelayed(runnable, TIME);
//        }else{
//        	new AlertDialog.Builder(WelcomeActivity.this).setTitle("提示")
//        	 .setMessage("请先连接WiFi")
//        	 .setIcon(android.R.drawable.ic_dialog_info) 
//        	 .setPositiveButton("确定", new DialogInterface.OnClickListener() { 
//                @Override 
//                public void onClick(DialogInterface dialog, int which) { 
//                // 点击“确认”后的操作 
//
//                	WelcomeActivity.this.finish(); 
//                } 
//            }).show(); 
//        }
        initData();
    }
    private void initData(){
    	Util.INITIAL_STATUS_CITY = GetSharedData.GetData(this, "city", Util.INITIAL_STATUS_CITY);
    	//创建默认的ImageLoader配置参数  
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration  
                .createDefault(this);  
          
        //Initialize ImageLoader with configuration.  
        ImageLoader.getInstance().init(configuration);  
    }
    
    Handler handler = new Handler();  
    Runnable runnable = new Runnable() {  
        @Override  
        public void run() {  
            // handler自带方法实现定时器  
            try {
            	startActivity(new Intent(WelcomeActivity.this,LoginActivity.class));
                handler.removeCallbacks(runnable);
                finish();
            } catch (Exception e) {
                // TODO Auto-generated catch block  
                e.printStackTrace();
            }
        }
    };

	@Override
	public void getUDPMessage(int code, byte[] data, int len) {
		// TODO Auto-generated method stub
		switch(code){
		case Util.INSTANCE_CODE_DISCOVER_REV:
			
		case Util.INSTANCE_CODE_GETSTATUS_REV:
			break;
		default:
			break;
	}
	}  


}
