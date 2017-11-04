package com.example.orkan.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.orkan.R;


public class MessageDialog {
	AlertDialog alertDialog;
	String title;
	Context context;
	public MessageDialog(Context context,String title){
		this.title = title;
		this.context = context;
		alertDialog = new AlertDialog.Builder(context).create();  
		
	}
	
	
	public void show(final OnClickListener click){
		alertDialog.show(); 
		Window window = alertDialog.getWindow();  
		window.setContentView(R.layout.dialog_message);  
		TextView titleTx = (TextView)window.findViewById(R.id.alert_btn_title);
		titleTx.setText(title);
		Button alert_btn_ok = (Button)window.findViewById(R.id.alert_btn_ok);
		alert_btn_ok.setOnClickListener(new OnClickListener(){
	        @Override
	        public void onClick(View v)
	        {
	        	alertDialog.cancel();
	        	click.onClick(v);
	        		
	        }
	    });
	}
	public boolean isShowing(){
		return alertDialog.isShowing();
	}
	
	public void show(){
		
		alertDialog.show(); 
		Window window = alertDialog.getWindow();  
		window.setContentView(R.layout.dialog_message);  
		TextView titleTx = (TextView)window.findViewById(R.id.alert_btn_title);
		titleTx.setText(title);
		Button alert_btn_ok = (Button)window.findViewById(R.id.alert_btn_ok);
		alert_btn_ok.setOnClickListener(new OnClickListener(){
	        @Override
	        public void onClick(View v)
	        {
	        	alertDialog.cancel();	
	        }
	    });
	}
	
}
