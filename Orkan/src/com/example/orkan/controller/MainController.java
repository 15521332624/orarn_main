package com.example.orkan.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.example.orkan.net.WiFiController;
import com.example.orkan.util.GetSharedData;
import com.example.orkan.util.Util;
import com.example.orkan.view.BezierCurveChart;
import com.example.orkan.view.BezierCurveChart.Point;

public class MainController {
	private static MainController instance;
	private static Context mContext;
    private CityInterface cityInterface;
	
	private MainController() {
        //获取wifi服务
		
    }
	
	public void init(){
		Util.INITIAL_STATUS_DEVICE_ID.clear();
		Util.POINTS_INDOOR_PM =new ArrayList<BezierCurveChart.Point>();
		Util.POINTS_OUTDOOR_PM =new ArrayList<BezierCurveChart.Point>();
		Util.POINTS_INDOOR_HUMIDITY =new ArrayList<BezierCurveChart.Point>();
		Util.POINTS_OUTDOOR_HUMIDITY =new ArrayList<BezierCurveChart.Point>();
		Util.POINTS_INDOOR_TEMPERATURE=new ArrayList<BezierCurveChart.Point>();
		Util.POINTS_OUTDOOR_TEMPERATURE =new ArrayList<BezierCurveChart.Point>();
		//暂时数据
		for(int i=0;i<10;i++) {
			Util.POINTS_INDOOR_PM .add(new Point(i, (float) (0)));
  		}
//		
		for(int i=0;i<10;i++) {
			Util.POINTS_INDOOR_HUMIDITY .add(new Point(i, (float) (0)));
  		}
		
		for(int i=0;i<10;i++) {
			Util.POINTS_INDOOR_TEMPERATURE .add(new Point(i, (float) (0)));
  		}
	
		Util.INITIAL_STATUS_HISTORY_DATAMODE = Util.DATA_MODE_HOUR;
		
		//定时配置获取
		Util.CONTROL_TIMING_LIST.clear();
		for(int i=0;i<7;i++) {
			HashMap<String, String> map = new HashMap<String, String>();
		
		  map.put("StartTime", GetSharedData.GetData(mContext,"StartTime"+i,"00:00"));
		  map.put("EndTime", GetSharedData.GetData(mContext,"EndTime"+i,"00:00"));
		  map.put("level", GetSharedData.GetData(mContext,"level"+i,"0"));
		  Util.CONTROL_TIMING_LIST.add(map);
		}
		
	}

	 //单例模式
    public static MainController getInstance(Context context) {
        if (instance == null) {
            mContext = context;
            instance = new MainController();
        }
        return instance;
    }
    //为了解决Activity传值的bug而直接用回调解决
    public interface CityInterface {
        void setcity(String city,String city_pinyin);
    }
    public void setCityInterface(CityInterface cityInterface){  //注册
        this.cityInterface = cityInterface;
    }
     
    public void citychoosed(String city,String city_pinyin){
    	cityInterface.setcity(city,city_pinyin);
    }
    
}
