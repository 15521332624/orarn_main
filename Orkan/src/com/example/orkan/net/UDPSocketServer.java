package com.example.orkan.net;

import android.os.Handler;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

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

public class UDPSocketServer implements Runnable, UDPWatched {
	private static final int SEND_PORT = Util.EAP_DISCOVER_UDP_SEND_PORT;
	private static final int REV_PORT = Util.EAP_DISCOVER_TCP_RECIEVE_PORT;
	private static final int SEND_LEN = Util.EAP_DISCOVER_UDP_SEND_LEN;
	private static final int REV_LEN = Util.EAP_DISCOVER_UDP_RECIEVE_LEN;

	private static final int POINT_LEN = 1;
	private static final int INSTANCE_CODE_LEN = 4;
	private static final int CHECK_CODE_LEN = 1;
	private static final byte KEY_CHECK = (byte) 0xab;

	private ServerSocket serverSocket;

	private static final int BUFFER_LENGTH = 512;
	private Thread receiveUDPThread;
	private static UDPSocketServer instance;

	private static DatagramSocket UDPSocket;
	private DatagramPacket receiveDatagramPacket;
	private byte[] receiveBuffer = new byte[BUFFER_LENGTH];

	private static String mLocalIp;

	public static String eapConIP = "";

	// 存放观察者
	private List<UDPWatcher> list = new ArrayList<UDPWatcher>();

	private boolean isThreadRunning;

	private UDPSocketServer(String localIp) {
		mLocalIp = localIp;
	}

	private UDPSocketServer() {

	}

	// 单例模式
	public static UDPSocketServer getInstance() {
		if (instance == null) {
			instance = new UDPSocketServer();

		}
		return instance;
	}

	public void setLocalIp(String localIp) {
		mLocalIp = localIp;
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

				// for(int i=0; i< receiveBuffer.length;i++){
				// receiveBuffer[i] ^= KEY_CHECK;
				// }

				// 获取指令码
				byte[] codeBytes = new byte[INSTANCE_CODE_LEN];
				;
				System.arraycopy(receiveBuffer, 0, codeBytes, 0, INSTANCE_CODE_LEN);
				int codeInt = PrimitiveUtil.bytesToInt(codeBytes, 0);
				Util.d("udp server: code " + codeInt);
				// 非法性检测
				if (!checkCode(codeInt)) {
					continue;
				}

				// 获取长度
				byte revLenByte[] = new byte[REV_LEN];
				System.arraycopy(receiveBuffer, INSTANCE_CODE_LEN, revLenByte, 0, REV_LEN);
				// byte转int
				int revLen = PrimitiveUtil.bytesToInt(revLenByte, 0);
				if (revLen <= 0) {
					continue;
				}
				Util.d("udp server: revLen " + revLen);
				// 解析数据
				byte revByte[] = new byte[revLen];
				System.arraycopy(receiveBuffer, INSTANCE_CODE_LEN + REV_LEN, revByte, 0, revLen);
				Util.d("udp server: Received data : " + new String(revByte));
				JSONObject revJson = new JSONObject(new String(revByte));
				// notifyWatchers(codeInt, revJson);
			} catch (IOException e) {
				Util.d("udp server: Received error");
				continue;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
		}

	}

	// 开启server
	public void startUDPSocketThread(String localIp) {
		Util.d("discover thread start");
		setLocalIp(localIp);
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
		// 发送内容
		JSONObject jsonObj = new JSONObject();

		try {
			jsonObj.put("Ip", mLocalIp);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/**
		 * 封装长度和内容 报文包括四字节的长度+JSON数据部分,前4位为数据长度，后面为数据内容。 { “request_type”: 1,
		 * “ip”:本地IP地址,String }
		 * 
		 */
		byte InstructionCode = Util.INSTANCE_CODE_DISCOVER_SEND;
		Util.d("send discover udp");
		sendUDPdata(BROADCASTIP, jsonObj, InstructionCode);
	}

	public synchronized void sendUDPdata(final String targetIP, final JSONObject jsonObj, final int instructionCode) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					InetAddress targetAddr;
					targetAddr = InetAddress.getByName(targetIP);
					// 接收缓存
					byte[] sendBuffer = new byte[BUFFER_LENGTH];
					Util.d("send ip:" + targetIP);
					// UDP socket
					DatagramSocket UDPSocket;
					DatagramPacket sendDatagramPacket;

					int length = jsonObj.toString().length();
					byte[] codeBytes = PrimitiveUtil.intToBytes(instructionCode);
					byte[] lenBytes = PrimitiveUtil.intToBytes(length);
					byte[] jsonBytes = jsonObj.toString().getBytes("UTF-8");
					byte[] checkcode = new byte[1];
					for (int i = 0; i < jsonBytes.length; i++) {
						checkcode[0] ^= jsonBytes[i];
					}

					int toalLength = INSTANCE_CODE_LEN + SEND_LEN + jsonBytes.length + CHECK_CODE_LEN;

					System.arraycopy(codeBytes, 0, sendBuffer, 0, INSTANCE_CODE_LEN);
					System.arraycopy(lenBytes, 0, sendBuffer, INSTANCE_CODE_LEN, SEND_LEN);
					System.arraycopy(jsonBytes, 0, sendBuffer, INSTANCE_CODE_LEN + SEND_LEN, jsonBytes.length);
					System.arraycopy(checkcode, 0, sendBuffer, INSTANCE_CODE_LEN + SEND_LEN + jsonBytes.length,
							CHECK_CODE_LEN);

					// for (int i =0; i < toalLength; i++) {
					// sendBuffer[i] ^= KEY_CHECK;
					// }

					Util.d("send :" + PrimitiveUtil.bytesToHexString(sendBuffer));
					// 发送UDP报文
					UDPSocket = new DatagramSocket(SEND_PORT);
					sendDatagramPacket = new DatagramPacket(sendBuffer, toalLength, targetAddr, SEND_PORT);

					UDPSocket.send(sendDatagramPacket);
					UDPSocket.close();
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
		for (UDPWatcher watcher : list) {
			Util.d("notify : " + watcher);
			watcher.getUDPMessage(code, data, len);
		}
	}
}
