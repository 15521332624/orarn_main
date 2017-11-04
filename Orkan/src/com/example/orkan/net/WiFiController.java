package com.example.orkan.net;

import com.example.orkan.util.Util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WiFiController {
	private static WiFiController instance;
	private static Context mContext;
	private WifiManager wifiManager;
	private WifiInfo wifiInfo;
	private String localIpAddress;
	
	private WiFiController() {
       

    }
	
	public void init(){
		 //获取wifi服务
        wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);

        wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        localIpAddress = Util.intToIp(ip);
        Util.d("device ip:" +localIpAddress);
	}

	 //单例模式
    public static WiFiController getInstance(Context context) {
        if (instance == null) {
            mContext = context;
            instance = new WiFiController();
        }
        return instance;
    }
    
    public String getLocalIpAddress(){
    	return localIpAddress;
    }
    
    public boolean isWifiEnabled(){
    	return wifiManager.isWifiEnabled();
    }
    
}
