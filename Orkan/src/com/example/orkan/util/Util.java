package com.example.orkan.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.orkan.view.BezierCurveChart;

public class Util
{
    //project name
    public static final String PROJECT_NAME = "EAP CONTROLLER";
    //屏幕尺寸
    public static int DEVICE_SCREEN_WIDTH;
    public static int DEVICE_SCREEN_HEIGHT;
    
    //HTTP
	public static final String NetWork = "http://weatherapi.market.xiaomi.com/wtr-v2/weather";
	
	public static final String WEATHER_URL = "http://zhwnlapi.etouch.cn/Ecalender/api/v2/weather";
	
	public static final String URL = "http://orkan.t-tt.cn/";
	public static final String URL_H = "http://t.t-tt.cn/com.luftmon.orkan/";
	public static String USER_ID = "";
	public static String USER_TOCKEN = "";
	public static String USER_PHONE = "";
	public static String USER_NAME = "";
	public static String USER_IM = "";
	
	//MQTT
	public static final String MQTT_HOST = "tcp://iotplc.cn:1883";
	public static final String MQTT_TOPIC_PRE = "/fw/orkan/";
	public static final String MQTT_TOPIC_SUF_DEVICE_TO_APP = "/v1.0/up";
	public static final String MQTT_TOPIC_SUF_APP_TO_DEVICE = "/v1.0/down";
	public static byte[] MQTT_USER_FENG = {}; 
	public static final String MQTT_USER_FENG_TEST = "48ff70067577525009352581";
	
	public static final String MQTT_TEST_MAC = "f0fe6b45feef";
	public static String MQTT_USER_MAC = MQTT_TEST_MAC;
	public static String MQTT_DEVICE_NAME = "Orkan";
	public static String MQTT_USER_IP = "";
	public static String MQTT_DEVICE_ID = "";
	public static int START_FROM_LOGIN = 0;
	
	public static int AP_MODULE_CONFIG = 0;
	public static int MODULE_DASK = 1;
	public static int MODULE_INSERT = 2;
	
	//sharedpreference
	public static final String SAVED_ACCOUNT = "account";
	public static final String SAVED_PWD = "password";
	
    //设备检测模块
    public static final int HANDLER_GET_DEVICE_SUCCESS = 1;
    public static final int HANDLER_GET_DEVICE_TIMEOUT = 2;

    public static final int EAP_DISCOVER_UDP_SEND_PORT = 27001;
    public static final int EAP_DISCOVER_TCP_RECIEVE_PORT = 27002;
    public static final int EAP_DISCOVER_UDP_SEND_LEN = 4;
    public static final int EAP_DISCOVER_UDP_RECIEVE_LEN = 4;
    public static final int EAP_DISCOVER_TIMEOUT_MS = 5000;
    //指令码
    public static final int INSTANCE_CODE_DISCOVER_SEND  = 0x50;
    public static final int INSTANCE_CODE_GETSTATUS_SEND  = 0x51;
    public static final int INSTANCE_CODE_AUTOMODE_SEND  = 0x61;
    public static final int INSTANCE_CODE_NEWWINDE_SEND  = 0x62;
    public static final int INSTANCE_CODE_WIND_SEND  = 0x63;
    public static final int INSTANCE_CODE_TIMING_SEND  = 0x64;
    public static final int INSTANCE_CODE_TIMINGLIST_SEND  = 0x65;
    public static final int INSTANCE_CODE_TIMINGDEL_SEND  = 0x66;
    public static final int INSTANCE_CODE_HISTORY_SEND  = 0x71;
    
    public static final int INSTANCE_CODE_STATE_CITY  = -1;
    
    public static final int INSTANCE_CODE_DISCOVER_REV  = 0xa0;
    public static final int INSTANCE_CODE_GETSTATUS_REV  = 0xa1;
    public static final int INSTANCE_CODE_AUTOMODE_REV  = 0xb1;
    public static final int INSTANCE_CODE_NEWWINDE_REV  = 0xb2;
    public static final int INSTANCE_CODE_WIND_REV  = 0xb3;
    public static final int INSTANCE_CODE_TIMING_REV  = 0xb4;
    public static final int INSTANCE_CODE_TIMINGLIST_REV  = 0xb5;
    public static final int INSTANCE_CODE_TIMINGDEL_REV  = 0xb6;
    public static final int INSTANCE_CODE_HISTORY_REV  = 0xc1;
    
   
    //控制
    public static final long TIME_OUT_LEN = 10000;
    public static final int CONTROL_FILTER_MIN_1 = 3*6*24*60;
    public static final int CONTROL_FILTER_MIDDLE_1 = 3*18*24*60;
    public static final int CONTROL_FILTER_MIN_2 = 2*365*24*12;
    public static final int CONTROL_FILTER_MIDDLE_2 = 2*365*24*36;
    public static final int CONTROL_FILTER_MIN_3 = 6*30*24*12;
    public static final int CONTROL_FILTER_MIDDLE_3 = 6*30*24*36;
    public static List<HashMap<String, String>> CONTROL_TIMING_LIST = new ArrayList<HashMap<String, String>>();
    
    
    //历史模块
    public static final int HISTORY_POINTS_NUM = 20;
 
    public static List<BezierCurveChart.Point> POINTS_INDOOR_PM = new ArrayList<BezierCurveChart.Point>();
    public static List<BezierCurveChart.Point> POINTS_OUTDOOR_PM = new ArrayList<BezierCurveChart.Point>();
    public static List<BezierCurveChart.Point> POINTS_INDOOR_HUMIDITY = new ArrayList<BezierCurveChart.Point>();
    public static List<BezierCurveChart.Point> POINTS_OUTDOOR_HUMIDITY = new ArrayList<BezierCurveChart.Point>();
    public static List<BezierCurveChart.Point> POINTS_INDOOR_TEMPERATURE = new ArrayList<BezierCurveChart.Point>();
    public static List<BezierCurveChart.Point> POINTS_OUTDOOR_TEMPERATURE = new ArrayList<BezierCurveChart.Point>();
    
    public static List<CharData> CHAR_POINTS_INDOOR_PM = new ArrayList<CharData>();
    public static List<CharData> CHAR_POINTS_OUTDOOR_PM = new ArrayList<CharData>();
    public static List<CharData> CHAR_POINTS_INDOOR_HUMIDITY = new ArrayList<CharData>();
    public static List<CharData> CHAR_OUTDOOR_HUMIDITY = new ArrayList<CharData>();
    public static List<CharData> CHAR_INDOOR_TEMPERATURE = new ArrayList<CharData>();
    public static List<CharData> CHAR_OUTDOOR_TEMPERATURE = new ArrayList<CharData>();
    
    public static int HistoryData = 0;
    public static int HistoryHumidityData = 0;
    public static int HistoryTemData = 0;
    
    //初始化状态数据
    public static List<String> INITIAL_STATUS_DEVICE_ID = new ArrayList<String>();
    public static String INITIAL_STATUS_PM_VALUE = "0";
    public static String INITIAL_STATUS_HUMIDITY_VALUE = "0";
    public static String INITIAL_STATUS_TEMP_VALUE = "0";
    public static String INITIAL_STATUS_OUTDOOR_PM_VALUE = "--";
    public static String INITIAL_STATUS_OUTDOOR_HUMIDITY_VALUE = "--";
    public static String INITIAL_STATUS_OUTDOOR_TEMP_VALUE = "--";
    public static String INITIAL_STATUS_CITY = "null";
    
    public static int INITIAL_STATUS_OPEN = 0;
    public static int INITIAL_STATUS_AUTOMODE = 0;
    public static int INITIAL_STATUS_XINFENG_OPEN = 0;
    
  //  public static int INITIAL_STATUS_NEWWINDMODE = 0;
    public static int INITIAL_STATUS_SWITCHMODE = 0;
    
    public static int INITIAL_STATUS_TIMING = 0;
    public static int INITIAL_STATUS_LIGHTMODE = 0;
    public static int INITIAL_STATUS_INSIDEFILTER_VALUE = 0;
    public static int INITIAL_STATUS_MIDDLEFILTER_VALUE = 0;
    public static int INITIAL_STATUS_OUTSIDEFILTER_VALUE = 0;
    
    public static int INITIAL_STATUS_OUTSIDEFILTER_VALUE_MAX = 3*30*24*60;
    public static int INITIAL_STATUS_MIDDLEFILTER_VALUE_MAX = 2*365*24*60;
    public static int INITIAL_STATUS_INSIDEFILTER_VALUE_MAX = 6*30*24*60;
    public static int[] INITIAL_STATUS_FENG_STATE = new int[16];
    public static int INITIAL_STATUS_FENG_NUM = 0;
   
    
    public static final int DATA_MODE_HOUR = 1;
    public static final int DATA_MODE_DAY = 2;
    public static final int DATA_MODE_WEEK = 3;
    public static String INITIAL_STATUS_HISTORY_STARTTIME = "";
    public static String INITIAL_STATUS_HISTORY_ENDTIME = "";
    public static int INITIAL_STATUS_HISTORY_DATAMODE = DATA_MODE_HOUR;
    
    public static final SimpleDateFormat ORKAN_SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault());
    public static final SimpleDateFormat ORKAN_STATUS_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
    
    //本地缓存信息
    public static String IP_ADDRESS = "0.0.0.0";

    //log tag
    public static final String TAG = "orkan";
    //debug log 封装
    public static void d(String s)
    {
        Log.d(TAG, s);
    }
    //error log 封装
    public static void e(String s)
    {
        Log.e(TAG, s);
    }
    /** 转换IP地址
     */
     public static String intToIp(int i)
     {
         return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                 + "." + ((i >> 24) & 0xFF);
     }

     public static String getStatusCurrentTime() {
     		Date date = new Date();
     	 return ORKAN_STATUS_DATE_FORMAT.format(date);
     }

    public static String getCurrentTime() {
    		Date date = new Date();
    	 return ORKAN_SIMPLE_DATE_FORMAT.format(date);
    }

    public static String getOneHourBeforeTime(String str) {
        Date dt = new Date();
		try {
			dt = ORKAN_SIMPLE_DATE_FORMAT.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(dt);
        rightNow.add(Calendar.HOUR,-1);//
     
        Date dt1=rightNow.getTime();
        String reStr = ORKAN_SIMPLE_DATE_FORMAT.format(dt1);
        return reStr;
    }
    public static String getOneHourLaterTime(String str) {
        Date dt = new Date();
		try {
			dt = ORKAN_SIMPLE_DATE_FORMAT.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(dt);
        rightNow.add(Calendar.HOUR,1);//
     
        Date dt1=rightNow.getTime();
        String reStr = ORKAN_SIMPLE_DATE_FORMAT.format(dt1);
        return reStr;
    }
    
    public static String getOneDayLaterTime(String str) {
        Date dt = new Date();
		try {
			dt = ORKAN_SIMPLE_DATE_FORMAT.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(dt);
        rightNow.add(Calendar.DAY_OF_YEAR,1);//
     
        Date dt1=rightNow.getTime();
        String reStr = ORKAN_SIMPLE_DATE_FORMAT.format(dt1);
        return reStr;
    }
    
    public static String getOneWeekLaterTime(String str) {
        Date dt = new Date();
		try {
			dt = ORKAN_SIMPLE_DATE_FORMAT.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(dt);
        rightNow.add(Calendar.DAY_OF_YEAR,7);//
     
        Date dt1=rightNow.getTime();
        String reStr = ORKAN_SIMPLE_DATE_FORMAT.format(dt1);
        return reStr;
    }
    
   
    public static String getBroadcastAddress() {

        return "255.255.255.255";
    }

    public static String getLocalBroadcastAddress() {
        System.setProperty("java.net.preferIPv4Stack", "true");
        try {
            for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum
                    .hasMoreElements();) {
                NetworkInterface ni = niEnum.nextElement();
                if (!ni.isLoopback()) {
                    for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
                        if (interfaceAddress.getBroadcast() != null) {
                            return interfaceAddress.getBroadcast().toString().substring(1);
                        }
                    }
                }
            }
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    //合法IP判断
    public static boolean isValidIP(String ipAddress)
    {
        String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();

    }

}