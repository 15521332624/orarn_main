package com.example.orkan.net;

import android.os.Handler;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.orkan.controller.MQTTController.MQTTFinish;
import com.example.orkan.model.DiscoverRevMsg;
import com.example.orkan.util.PrimitiveUtil;
import com.example.orkan.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class SmartUDPSocketServer implements Runnable {
	private static final int SEND_PORT = 48899;
	private static final int REV_PORT = 48899;
	private FindDevice ie;

	private static final int BUFFER_LENGTH = 512;
	private Thread receiveUDPThread;
	private static SmartUDPSocketServer instance;

	private static DatagramSocket UDPSocket;
	private DatagramPacket receiveDatagramPacket;
	private byte[] receiveBuffer = new byte[BUFFER_LENGTH];

	private static String mLocalIp;

	public static String eapConIP = "";

	// 存放观察者
	private List<UDPWatcher> list = new ArrayList<UDPWatcher>();

	private boolean isThreadRunning;

	private SmartUDPSocketServer(String localIp) {
		mLocalIp = localIp;
	}

	public SmartUDPSocketServer() {

	}

	// 单例模式
	public static SmartUDPSocketServer getInstance() {
		if (instance == null) {
			instance = new SmartUDPSocketServer();

		}
		return instance;
	}

	public void setLocalIp(String localIp) {
		mLocalIp = localIp;
	}

	public void setFindDevie(FindDevice ie) {
		this.ie = ie;
	}

	@Override
	public void run() {
		Util.d("discover thread run");

		try {
			if (UDPSocket == null)
				UDPSocket = new DatagramSocket(REV_PORT);

		} catch (SocketException e) {
			e.printStackTrace();
		}
		while (isThreadRunning) {
			try {
				receiveDatagramPacket = new DatagramPacket(receiveBuffer, BUFFER_LENGTH);
				UDPSocket.receive(receiveDatagramPacket);
				if (receiveDatagramPacket.getLength() == 0) {
					Util.d("udp server: Received length 0");
					continue;
				}

				String revStr = new String(receiveBuffer);
				revStr = revStr.trim();

				if (revStr.contains("HF-A11ASSISTHREAD")) {
					continue;
				}
				Util.d("udp server: Received data : " + revStr);
				String[] data = revStr.split(",");
				if (data.length >= 3) {
					ie.FindDevice(data[2], data[0], data[1]);
				}
				// notifyWatchers(codeInt, revJson);
			} catch (IOException e) {
				Util.d("udp server: Received error");
				continue;
			}
		}

	}

	// 开启server
	public void startUDPSocketThread() {
		Util.d("discover thread start");

		if (receiveUDPThread == null) {
			receiveUDPThread = new Thread(this);
			receiveUDPThread.start();
		}
		isThreadRunning = true;
	}

	// 关闭server
	public void stopUDPSocketThread() {
		Util.d("discover thread stop");
		isThreadRunning = false;
		receiveDatagramPacket = null;
		if (UDPSocket != null && !UDPSocket.isClosed()) {
			UDPSocket.close();
			UDPSocket = null;
		}

		if (receiveUDPThread != null)
			receiveUDPThread.interrupt();
		receiveUDPThread = null;

	}

	// 广播搜索EAP Controller
	public void sendDiscover() {
		String BROADCASTIP = Util.getBroadcastAddress();

		Util.d("send discover udp");
		
		sendUDPdata(BROADCASTIP, "HF-A11ASSISTHREAD");
	}

	public synchronized void sendUDPdata(final String targetIP, final String str) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					InetAddress targetAddr;
					targetAddr = InetAddress.getByName(targetIP);
					// 接收缓存

					Util.d("send ip:" + targetIP);
					// UDP socket

					DatagramPacket sendDatagramPacket;

					int length = str.length();

					Util.d("send :" + str);
					// 发送UDP报文

					sendDatagramPacket = new DatagramPacket(str.getBytes(), length, targetAddr, SEND_PORT);

					UDPSocket.send(sendDatagramPacket);
					Util.d("send :" + str);
				} catch (Exception e) {
					Util.d("send socket exception");
					e.printStackTrace();

				}
			}
		}).start();

	}

	public boolean checkCode(int code) {
		if (code >= 0xa0 && code <= 0xff) {
			return true;
		} else {
			return false;
		}
	}

	public interface FindDevice {
		// 这只是一个普通的方法，可以接收参数、也可以返回值
		public void FindDevice(String name, String ip, String mac);
	}
}
