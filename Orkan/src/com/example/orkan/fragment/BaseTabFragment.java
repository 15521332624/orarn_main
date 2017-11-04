package com.example.orkan.fragment;

import com.example.orkan.third.kprogresshud.KProgressHUD;
import com.example.orkan.util.Util;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

public class BaseTabFragment extends Fragment{
	 KProgressHUD  hud;
	
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
    	hud = KProgressHUD.create(getActivity())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
    	timeoutrunnable = new Runnable() {
            @Override
            public void run() {
            	if (hud.isShowing()){
            		hud.dismiss();
            		Toast.makeText(getActivity(),
   		                    "请求超时", Toast.LENGTH_SHORT).show();
            	}
            }
        };
    }
    protected void initData(){
   
    }
   
}