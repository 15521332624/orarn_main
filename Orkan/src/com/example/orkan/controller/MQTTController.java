package com.example.orkan.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.orkan.model.MQTTRevMsg;
import com.example.orkan.net.UDPWatched;
import com.example.orkan.net.UDPWatcher;
import com.example.orkan.util.MQTTUtil;
import com.example.orkan.util.Util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class MQTTController implements UDPWatched{
	private static MQTTController instance;

	private int qos = 0;
	private MqttConnectOptions options;
	private MqttClient client;
	private ScheduledExecutorService scheduler;
	private ProbeHandler probeHandler = new ProbeHandler();
	boolean isReconnect = true;
	private MQTTFinish ie;
	
	//存放观察者
    private List<UDPWatcher> list = new ArrayList<UDPWatcher>();
	private MQTTController() {
      
    }
	public void setFinishCallBack(MQTTFinish ie){
		this.ie = ie;
	}

	 //handler更新UI
    protected class ProbeHandler extends Handler {
       public ProbeHandler() {
       
       }
       
       @Override
       public void handleMessage(Message msg) {
       		if(msg.what == 1){
       			MQTTRevMsg mmsg = (MQTTRevMsg)msg.obj;
       			notifyWatchers(mmsg.getCode(),mmsg.getData(),mmsg.getLen());
       		}else if(msg.what == 2){
       		 ie.MQTTFinish();
       		}
       }
   }
	
	 //单例模式
    public static MQTTController getInstance() {
        if (instance == null) {
       
            instance = new MQTTController();
        }
        return instance;
    }
    
    public void start(){
    	 init();
         startReconnect();
    }
    
    public void init(){
    	try {
    		Util.d("mqtt  mac "+Util.MQTT_USER_MAC);
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(Util.MQTT_HOST, "app-"+Util.MQTT_USER_MAC,
                    new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            //设置连接的用户名
        //    options.setUserName(userName);
            //设置连接的密码
       //     options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(5);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
           // options.setKeepAliveInterval(20);
            //设置回调
            client.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    Util.d("connectionLost----------");
                    Util.d(cause.toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                	Util.d("deliveryComplete---------"
                            + token.isComplete());
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message)
                        throws Exception {
                    //subscribe后得到的消息会执行到这里面
             //   	Util.d("messageArrived----------");
              //  	Util.d("qos  "+message.getQos());
                	try{
	                	byte[] rev = message.getPayload();
	//                	for (int i = 0;i<rev.length;i++){
	//                		int revInt = rev[i] & 0xff;
	//                		Util.d(revInt+"");
	//                	}
	                	Util.d("rev = " + MQTTUtil.byte2hex(rev));
	                	int lenth = rev.length;
	            		
	            		int dataLenth = 0;
	            		short comm = 0;
	            		if (lenth < 10)
	            			return ;
	            		// Head Analyse
	            		for (int i = 0; i < 4; i++) {
	            			if (rev[i] != MQTTUtil.Head[i])
	            				return ;
	            		}
	            		dataLenth = rev[5];
	            		comm = rev[7];
	            		byte[] crcCheckData = MQTTUtil.subBytes(rev, 12, dataLenth);
	            		
	            		MQTTRevMsg mmsg = new MQTTRevMsg();
	            		mmsg.setCode(comm);
	            		mmsg.setData(crcCheckData);
	            		mmsg.setLen(dataLenth);
	            		
	               	  Message msg = new Message();
	                  msg.what = 1;
	                  msg.obj = mmsg;
	                  probeHandler.sendMessage(msg);
                	} catch (Exception e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		}
            		
            	//	notifyWatchers(comm,crcCheckData,dataLenth);
                	
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void startReconnect() {
    	isReconnect = true;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                if (!client.isConnected() && isReconnect) {
                    connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }
    
    public void sendMsg(byte code, byte[]b){
        
    //	Util.d("send  "+ (int)code + MQTTUtil.byte2hex(b));
    	
        MqttMessage message = new MqttMessage(MQTTUtil.packetData((byte)code,b));                       
        //发布消息到服务器
        try {
			client.publish(Util.MQTT_TOPIC_PRE+Util.MQTT_USER_MAC + Util.MQTT_TOPIC_SUF_APP_TO_DEVICE, message);
       
        } catch (MqttPersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    private void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.connect(options);
                    Util.d("MQTT连接成功");
                    try {
                        client.subscribe(Util.MQTT_TOPIC_PRE+Util.MQTT_USER_MAC + Util.MQTT_TOPIC_SUF_DEVICE_TO_APP, qos);
                     //   client.subscribe(Util.MQTT_TOPIC_PRE+Util.MQTT_USER_MAC + Util.MQTT_TOPIC_SUF_APP_TO_DEVICE, qos);
                    
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                	  Message msg = new Message();
                      msg.what = 2;
                   
                      probeHandler.sendMessage(msg);
                		
                } catch (Exception e) {
                    e.printStackTrace();
                   
                }
            }
        }).start();
    }

    public void stop(){
    	isReconnect = false;
    	list.clear();
    	if(scheduler!=null)
    		scheduler.shutdown();
    	try {
    		if(client!=null)
    			client.disconnect();
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    
    }
    
	@Override
	public void addWatcher(UDPWatcher watcher) {
		// TODO Auto-generated method stub
		list.add(watcher);
	}

	@Override
	public void removeWatcher(UDPWatcher watcher) {
		// TODO Auto-generated method stub
		list.remove(watcher);
	}

	@Override
	public void notifyWatchers(int code, byte[] data, int len) {
		// TODO Auto-generated method stub
		 for (UDPWatcher watcher : list)
	        {
			 	Util.d("notify : "+ watcher);
	            watcher.getUDPMessage(code,data,len);
	        }
	}
	public interface MQTTFinish {
	    // 这只是一个普通的方法，可以接收参数、也可以返回值
	    public void MQTTFinish();
	}
    
}
