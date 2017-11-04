package com.example.orkan.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SaveSharedData {

	public static void SaveData(Context context, String key,String value){
	
		SharedPreferences sharedPreferences = context.getSharedPreferences(Util.PROJECT_NAME,
		   Activity.MODE_PRIVATE);

	    SharedPreferences.Editor editor = sharedPreferences.edit();
	    editor.putString(key, value);
	    editor.apply();
      
	}
	public static void SaveData(Context context, String key,boolean value){
		SharedPreferences sharedPreferences = context.getSharedPreferences(Util.PROJECT_NAME,
	        Activity.MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPreferences.edit();
	    editor.putBoolean(key, value);
        editor.apply();
	}
    public static void SaveData(Context context, String key,int value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(Util.PROJECT_NAME,
                Activity.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }
}
