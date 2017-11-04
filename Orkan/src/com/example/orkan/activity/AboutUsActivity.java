package com.example.orkan.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.orkan.R;
import com.example.orkan.adapter.TimingListAdapter;
import com.example.orkan.net.UDPSocketServer;
import com.example.orkan.net.UDPWatcher;
import com.example.orkan.net.WiFiController;
import com.example.orkan.third.citypicker.SystemUtils;
import com.example.orkan.util.Util;

public class AboutUsActivity extends BaseActivity{
	ListView timinglistView;
	TimingListAdapter adapter;
	TextView title_tx;
	ImageView title_im;
	ImageView title_left;
	ImageView content_im;
	
	//TEST
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutus);
        init();
  
    }
    
    private void init(){
    	title_tx = (TextView)findViewById(R.id.title_tx);
    	title_tx.setText(R.string.more_about);
    	content_im = (ImageView)findViewById(R.id.content_im);
    	
    	title_left= (ImageView)findViewById(R.id.title_left);
    	title_left.setImageResource(R.drawable.back);
    	title_left.setVisibility(View.VISIBLE);
    	title_left.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	AboutUsActivity.this.finish();
            }
        });
    	
    	
    }
}

