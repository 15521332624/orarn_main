package com.example.orkan.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class GetSharedData {

	public static String GetData(Context context, String key,String value){
		
		 SharedPreferences sharedPreferences = context.getSharedPreferences(Util.PROJECT_NAME,
				   Activity.MODE_PRIVATE);
	
		 return sharedPreferences.getString(key, value);
	}
	public static boolean GetData(Context context, String key,boolean value){
		 SharedPreferences sharedPreferences = context.getSharedPreferences(Util.PROJECT_NAME,
				   Activity.MODE_PRIVATE);
		 return sharedPreferences.getBoolean(key, value);
	}
	public static int GetData(Context context, String key,int value){
		 SharedPreferences sharedPreferences = context.getSharedPreferences(Util.PROJECT_NAME,
				   Activity.MODE_PRIVATE);
		 return sharedPreferences.getInt(key, value);
	}
}
