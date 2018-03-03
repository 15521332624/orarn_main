package com.example.orkan.activity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orkan.R;

import com.example.orkan.net.UDPSocketServer;
import com.example.orkan.net.UDPWatcher;
import com.example.orkan.third.kprogresshud.KProgressHUD;
import com.example.orkan.third.timewheel.NumericWheelAdapter;
import com.example.orkan.third.timewheel.OnWheelScrollListener;
import com.example.orkan.third.timewheel.WheelView;
import com.example.orkan.util.Util;
import com.example.orkan.view.slidedatetimepicker.SlideDateTimePicker;

public class TimingSetActivity extends Activity implements UDPWatcher{
	private String[] timingModeList = {"周一","周二","周三","周四","周五","周六","周日","周一到周五","每天"};
	private int[] timingModeList_int = { R.string.week_1, R.string.week_2, R.string.week_3, R.string.week_4, R.string.week_5, R.string.week_6, R.string.week_7, R.string.week_8, R.string.week_9 };
  	private List<Map<String, String>> modelist = new ArrayList<Map<String, String>>(); // 定义显示的内容包
  	private SimpleAdapter modeSimpleAdapter = null;
	private ListView modListView;
	private WheelView stHourView;
    private WheelView stMinView;
    private WheelView endHourView;
    private WheelView endMinView;
    KProgressHUD hud;
	protected UDPSocketServer mProbeSocketDiscover;
	String [] levelStrs = {"一级","二级","三级"};
	int[] levelStrs_int = { R.string.level_1, R.string.level_2, R.string.level_3};
    TextView timing_day_name_tx;
 //   private String id;
    private String startTime;
    private String endTime;
    private String mode;
    private int modeInt;
    private TextView timing_delete;
    private int[] TimeDuan = {6, 0, 18, 0};
    private TextView time_alert;
	TextView title_tx;
	RelativeLayout timing_set_li1;
	ImageView title_im;
	ImageView title_left;
	Button timing_fengsu_button;
	
	int chooseLevel = 0;
	
	private ListView deviceListView;
	private List<Map<String, String>> devicelist = new ArrayList<Map<String, String>>(); // 定义显示的内容包
  	private SimpleAdapter deviceSimpleAdapter = null;
    private OnWheelScrollListener time_Listener = new OnWheelScrollListener() {

        @Override
        public void onScrollingStarted(WheelView wheel) {
            // TODO Auto-generated method stub
            //wheel.setCurrentItem(15, false);
        }

        @Override
        public void onScrollingFinished(WheelView wheel) {
            // TODO Auto-generated method stub
            TimeDuan[0] = stHourView.getCurrentItem();
            TimeDuan[1] = stMinView.getCurrentItem();
            TimeDuan[2] = endHourView.getCurrentItem();
            TimeDuan[3] = endMinView.getCurrentItem();
        }
    };
    
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timingset);
    	
		Intent intent = this.getIntent();

	//	id = intent.getStringExtra("Id");
		startTime = intent.getStringExtra("StartTime");
		endTime = intent.getStringExtra("EndTime");
		mode = intent.getStringExtra("Mode");
        modeInt = Integer.parseInt(mode);
        init();
  
    }
    
    private void init(){
    	hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
   	 
		//注册观察者
    	mProbeSocketDiscover = UDPSocketServer.getInstance();
    	mProbeSocketDiscover.addWatcher(this);
    	title_tx = (TextView)findViewById(R.id.title_tx);
    	title_tx.setText(R.string.title_timing);
    	timing_day_name_tx = (TextView)findViewById(R.id.timing_day_name_tx);
    	  // TODO Auto-generated method stub
        time_alert = (TextView) findViewById(R.id.timefor_alert);
        stHourView = (WheelView) findViewById(R.id.wheel_st_hour);
        NumericWheelAdapter stHourViewAdapter = new NumericWheelAdapter(
                this, 0, 23, "%02d");
        stHourViewAdapter.setLabel(getString(R.string.time_h));
        // 开始 时
        stHourView.setCyclic(true);
        stHourView.setViewAdapter(stHourViewAdapter);
        stHourView.setVisibleItems(7);
        stHourView.setCurrentItem(TimeDuan[0]);
        stHourView.addScrollingListener(time_Listener);

        stMinView = (WheelView) findViewById(R.id.wheel_st_min);
        NumericWheelAdapter stMinViewAdapter = new NumericWheelAdapter(
        		this, 0, 59, "%02d");
        stMinViewAdapter.setLabel(getString(R.string.time_m));
        // 开始 分
        stMinView.setCyclic(true);
        stMinView.setViewAdapter(stMinViewAdapter);
        stMinView.setVisibleItems(7);
        stMinView.setCurrentItem(TimeDuan[1]);
        stMinView.addScrollingListener(time_Listener);

        endHourView = (WheelView) findViewById(R.id.wheel_end_hour);
        NumericWheelAdapter endHourViewAdapter = new NumericWheelAdapter(
        		this, 0, 23, "%02d");
        endHourViewAdapter.setLabel(getString(R.string.time_h));
        // 结束 时
        endHourView.setCyclic(true);
        endHourView.setViewAdapter(endHourViewAdapter);
        endHourView.setVisibleItems(7);
        endHourView.setCurrentItem(TimeDuan[2]);
        endHourView.addScrollingListener(time_Listener);

        endMinView = (WheelView) findViewById(R.id.wheel_end_min);
        NumericWheelAdapter endMinViewAdapter = new NumericWheelAdapter(
        		this, 0, 59, "%02d");
        endMinViewAdapter.setLabel(getString(R.string.time_m));
        // 结束 分
        endMinView.setCyclic(true);
        endMinView.setViewAdapter(endMinViewAdapter);
        endMinView.setVisibleItems(7);
        endMinView.setCurrentItem(TimeDuan[3]);
        endMinView.addScrollingListener(time_Listener);
        timing_delete = (TextView)findViewById(R.id.timing_delete);
//        if(id.equals("0")){
//        	timing_delete.setVisibility(View.GONE);
//        }
        timing_delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	
            	
            	HashMap<String, String> map = Util.CONTROL_TIMING_LIST.get(modeInt);
            	map.put("StartTime", "00:00");
            	map.put("EndTime", "00:00");
            	TimingSetActivity.this.finish();
            }
        });
        
     	title_im = (ImageView)findViewById(R.id.title_im);
    	title_im.setImageResource(R.drawable.timing_selected);
    	title_im.setVisibility(View.VISIBLE);
    	title_im.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	
               if (judgeTime()) {
                   time_alert.setVisibility(View.GONE);
                   HashMap<String, String> map = Util.CONTROL_TIMING_LIST.get(modeInt);
                   map.put("StartTime", getTimeStr(TimeDuan).substring(0, 5));
                   map.put("EndTime", getTimeStr(TimeDuan).substring(6, 11));
                   map.put("level", chooseLevel+"");
                   Util.d("set"+modeInt+" " + getTimeStr(TimeDuan).substring(0, 5) + "  "+  getTimeStr(TimeDuan).substring(6, 11));
                   
                   TimingSetActivity.this.finish();
               } else {
                  
                   time_alert.setVisibility(View.VISIBLE);
               }
            }
        });
        
    	title_left= (ImageView)findViewById(R.id.title_left);
    	title_left.setImageResource(R.drawable.back);
    	title_left.setVisibility(View.VISIBLE);
    	title_left.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	TimingSetActivity.this.finish();
            }
        });
    	timing_fengsu_button = (Button)findViewById(R.id.timing_fengsu_button);
    	HashMap<String, String> map = Util.CONTROL_TIMING_LIST.get(modeInt);
    	int level = Integer.parseInt(map.get("level"));
    	if (level < 3){
    		timing_fengsu_button.setText(getString(levelStrs_int[level]));
    		chooseLevel = level;
    	}
          
    	timing_fengsu_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	final AlertDialog alertDialog = new AlertDialog.Builder(TimingSetActivity.this).create();  
            	alertDialog.show();  
            	Window window = alertDialog.getWindow();  
            	window.setContentView(R.layout.dialog_list);
            	deviceListView = (ListView)window.findViewById(R.id.dialog_list);
            	TextView dialog_title = (TextView)window.findViewById(R.id.dialog_title);
            	dialog_title.setText(R.string.select_wind_speed);
            	devicelist.clear();
                
            		Map<String, String> map = new HashMap<String, String>(); // 定义Map集合，保存每一行数据  
            		map.put("device", getString(R.string.level_1));
            		devicelist.add(map); // 保存了所有的数据行  
            		 map = new HashMap<String, String>(); // 定义Map集合，保存每一行数据  
             		map.put("device", getString(R.string.level_2));
             		devicelist.add(map); // 保存了所有的数据行  
             		 map = new HashMap<String, String>(); // 定义Map集合，保存每一行数据  
             		map.put("device", getString(R.string.level_3));
             		devicelist.add(map); // 保存了所有的数据行  
            
            	deviceSimpleAdapter = new SimpleAdapter(TimingSetActivity.this, devicelist,  
            		R.layout.list_dialog, new String[] { "device" } // Map中的key的名称  
            		, new int[] { R.id.list_tx }); // 是data_list.xml中定义的组件的资源ID  
            	deviceListView.setAdapter(deviceSimpleAdapter);  
            	deviceListView.setOnItemClickListener(new OnItemClickListener(){
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position,  
							long id) {  
						// TODO Auto-generated method stub
						chooseLevel = position;
						timing_fengsu_button.setText(getString(levelStrs_int[chooseLevel]));
                    	 alertDialog.cancel();
					}
                     
                });

            }
        });
    	
    	
        timing_set_li1 = (RelativeLayout)findViewById(R.id.timing_set_li1);
        timing_set_li1.setVisibility(View.GONE);
        timing_set_li1.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v)
            {
            	
            	final AlertDialog alertDialog = new AlertDialog.Builder(TimingSetActivity.this).create();  
            	alertDialog.show();  
            	Window window = alertDialog.getWindow();  
            	window.setContentView(R.layout.dialog_list);
            	modListView = (ListView)window.findViewById(R.id.dialog_list);
            	TextView dialog_title = (TextView)window.findViewById(R.id.dialog_title);
            	dialog_title.setText("选择模式");
            	modelist.clear();
            	for (int x = 0; x < timingModeList.length; x++) {  
            		Map<String, String> map = new HashMap<String, String>(); // 定义Map集合，保存每一行数据  
            		map.put("mode", getString(timingModeList_int[x])); // 与data_list.xml中的TextView组加匹配
            		modelist.add(map); // 保存了所有的数据行  
            	}  
            	modeSimpleAdapter = new SimpleAdapter(TimingSetActivity.this, modelist,  
            		R.layout.list_dialog, new String[] { "mode" } // Map中的key的名称  
            		, new int[] { R.id.list_tx }); // 是data_list.xml中定义的组件的资源ID  
            	modListView.setAdapter(modeSimpleAdapter);  
            	modListView.setOnItemClickListener(new OnItemClickListener(){
            	
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position,  
							long id) {  
						// TODO Auto-generated method stub
						 timing_day_name_tx.setText(getString(timingModeList_int[position]));
                   // 	 mode = position+"";
                    	 alertDialog.cancel();
					}
                     
                });
            	
//            	 AlertDialog.Builder builder = new AlertDialog.Builder(TimingSetActivity.this);
//                 builder.setTitle("重复选项");
//                 //    指定下拉列表的显示数据
//                
//                 //    设置一个下拉的列表选择项
//                 builder.setItems(timingModeList, new DialogInterface.OnClickListener()
//                 {
//                     @Override
//                     public void onClick(DialogInterface dialog, int which)
//                     {
//                         //Toast.makeText(TimingSetActivity.this, "选择的为：" + timingModeList[which], Toast.LENGTH_SHORT).show();
//                    	 timing_day_name_tx.setText(timingModeList[which]);
//                    	 mode = which+"";
//                     }
//                 });
//                 builder.show();
            }
        });
        

    	 stHourView.setCurrentItem(Integer.parseInt(startTime.substring(0, 2)));
    	 stMinView.setCurrentItem(Integer.parseInt(startTime.substring(3, 5)));
    	 endHourView.setCurrentItem(Integer.parseInt(endTime.substring(0, 2)));
    	 endMinView.setCurrentItem(Integer.parseInt(endTime.substring(3, 5)));
 
    }
  
	@Override
	public void getUDPMessage(int code, byte[] data, int len) {

	}
    
	private void timeOutControl(){
	   	 Handler handler = new Handler();
	        handler.postDelayed(new Runnable() {
	            @Override
	            public void run() {
	            	if (hud.isShowing()){
	            		hud.dismiss();
	            		Toast.makeText(TimingSetActivity.this,
			                    getString(R.string.time_out), Toast.LENGTH_SHORT).show();
	            	}
	            }
	        }, Util.TIME_OUT_LEN);
	   }

    
    private Boolean judgeTime() {
        if (TimeDuan[0] > TimeDuan[2]) {
            return false;
        } else if ((TimeDuan[0] == TimeDuan[2]) && (TimeDuan[1] >= TimeDuan[3])) {
            return false;
        } else {
            return true;
        }
    }
    private String getTimeStr(int[] st) {
        return new StringBuilder()
                .append((st[0] < 10) ? ("0" + st[0]) : st[0]).append(":")
                .append((st[1] < 10) ? ("0" + st[1]) : st[1])
                .append("~")
                .append((st[2] < 10) ? ("0" + st[2]) : st[2]).append(":")
                .append((st[3] < 10) ? ("0" + st[3]) : st[3])
                .toString();
    }

}
