package com.example.orkan.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.orkan.R;
import com.example.orkan.adapter.ApConfDevTypeAdapter;
import com.example.orkan.util.Util;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class APConfigTypeActivity extends BaseActivity{
	
	TextView title_tx;
	ImageView title_left;
	ListView device_type_list;
	ApConfDevTypeAdapter adapter;
	List<Map<String, ?>>  deviceList = new ArrayList<Map<String, ?>>();
	int[] device_type_pic = {R.drawable.orkan,R.drawable.xiangfan};
	int[] device_name = {R.string.device_type_fenwen,R.string.device_type_xiangfan};
	int[] device_introduce = {R.string.device_fenwen_introduce,R.string.device_xiangfan_introduce};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_apconfigtype);
		
		title_tx = (TextView) findViewById(R.id.title_tx);
		title_tx.setText(R.string.title_type);
		title_left = (ImageView) findViewById(R.id.title_left);
		title_left.setImageResource(R.drawable.back);
		title_left.setVisibility(View.VISIBLE);
		
		title_left.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				APConfigTypeActivity.this.finish();
			}
		});
		initListView();
	}	
	
	
	private void initListView() {
		
		device_type_list = (ListView)findViewById(R.id.device_type_list);
		
	  	String[] from = { "devicename","deviceintroduce","devicepic"};
        int[] to = { R.id.device_name_txt,R.id.device_introdue_txt,R.id.device_pic};
        
		for(int i=0;i<2;i++) {
			HashMap<String, Object> dmap = new HashMap<String, Object>();
			dmap.put("devicename", this.getString(device_name[i]));
			dmap.put("deviceintroduce", this.getString(device_introduce[i]));
			dmap.put("devicepic",device_type_pic[i]);
			deviceList.add(dmap);
		}
		
		adapter = new ApConfDevTypeAdapter(this, deviceList, R.layout.list_type_device, from, to);
		device_type_list.setAdapter(adapter);
		
		device_type_list.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if(arg2 == 0) {
					Util.AP_MODULE_CONFIG = Util.MODULE_DASK;
				}else {
					Util.AP_MODULE_CONFIG = Util.MODULE_INSERT;
				}
				startActivity(new Intent(APConfigTypeActivity.this,AddDeviceActivity.class));
				//APConfigTypeActivity.this.finish();
			}
		});
	}
	
	
	
	

}
