package com.example.orkan.net;

import org.json.JSONObject;

public interface UDPWatcher {
	 //public void getUDPMessage(int code, JSONObject jsonData);
	 
	 public void getUDPMessage(int code, byte[]data,int len);
}
