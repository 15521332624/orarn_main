package com.example.orkan.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.orkan.R;
import com.example.orkan.controller.MQTTController;
import com.example.orkan.net.UDPWatcher;
import com.example.orkan.third.kprogresshud.KProgressHUD;
import com.example.orkan.util.Util;

/**
 * Base Activity for EAP controller
 *
 * @author libo
 */

public class BaseActivity extends FragmentActivity {
	
	 
	protected KProgressHUD hud;
	
		
	 Handler timeouthandler = new Handler();
	 Runnable timeoutrunnable;
     protected void timeOutControl(){
     	timeouthandler.postDelayed(timeoutrunnable, Util.TIME_OUT_LEN);
     }
     protected void timeOutCancel(){
     	timeouthandler.removeCallbacks(timeoutrunnable);
     }
     protected void beginAll(){
     	hud.show();
     	timeOutControl();
     }
     protected void cancelAll(){
     	hud.dismiss();
     	timeOutCancel();
     }
     
    protected void init(View view){
    	
    }
	
    @SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
    	timeoutrunnable = new Runnable() {
            @Override
            public void run() {
            	if (hud.isShowing()){
            		hud.dismiss();
            		Toast.makeText(BaseActivity.this,
   		                    getString(R.string.time_out), Toast.LENGTH_SHORT).show();
            	}
            }
        };
        
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//	        Window window = this.getWindow();  
//	      //设置透明状态栏,这样才能让 ContentView 向上  
//	      window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);   
//	        
//	      //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色  
//	      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);   
//	      //设置状态栏颜色  
//	      window.setStatusBarColor(getResources().getColor(R.color.theme_color));  
//	        
//	      ViewGroup mContentView = (ViewGroup) this.findViewById(Window.ID_ANDROID_CONTENT);  
//	      View mChildView = mContentView.getChildAt(0);  
//	      if (mChildView != null) {  
//	          //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 使其不为系统 View 预留空间.  
//	          ViewCompat.setFitsSystemWindows(mChildView, false);  
//	      }  
//        }
    }
    
    
   
   
    public void finish(View view){
    	
        finish();
    }

  
}
