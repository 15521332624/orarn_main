package com.example.orkan.net;

import org.json.JSONObject;

public interface UDPWatched {
	public void addWatcher(UDPWatcher watcher);

	public void removeWatcher(UDPWatcher watcher);

	public void notifyWatchers(int code, byte[]data,int len);
}
