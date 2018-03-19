package com.example.orkan;
import com.example.orkan.util.CrashHandler;

import android.app.Application;
import android.util.Log;

public class MYAPP extends Application {
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.v("snake", "APP APPLIACTION");
		CrashHandler crashHandler = CrashHandler.getInstance();  
		crashHandler.init(this);  
	}

}
