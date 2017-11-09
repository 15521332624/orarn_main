package com.example.orkan.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.example.orkan.util.Util;

public class FrUDPSocketServer {
	
	private static int SEND_PORT = 20001;
	private static int REV_PORT = 20000;
	private static String mLocalIp = "10.10.100.254";
	
	private Thread receiveUDPThread = null;
	private Thread sendUDPThread = null;
	private static final int BUFFER_LENGTH = 512;
	private boolean isThreadRunning;
	private static DatagramSocket UDPSocket = null;
	private DatagramPacket receiveDatagramPacket;
	private byte[] receiveBuffer = new byte[BUFFER_LENGTH];
	private FrUDPConnectListener listener = null;
	private String sendData = null;

	public FrUDPSocketServer(String Ip,int SendPort,int RevPort) {
		mLocalIp = Ip;
		SEND_PORT = SendPort;
		REV_PORT = RevPort;
	}
	
	public void addUDPListener(FrUDPConnectListener listener){
		this.listener = listener;
	}
	
	public void sendSocketData(String sendData){
		this.sendData = sendData;
	}
	
	// 开启server
	public void startUDPSocketThread() {
		isThreadRunning = true;
		if (receiveUDPThread == null) {
			receiveUDPThread = new Thread(receiver);
			receiveUDPThread.start();
		}
		
		if(sendUDPThread == null) {
			sendUDPThread = new Thread(send);
			sendUDPThread.start();
		}
		
	}
	
	// 关闭server
	public void stopUDPSocketThread() {
		Util.d("fr discover thread stop");
		isThreadRunning = false;
		receiveDatagramPacket = null;
		if (UDPSocket != null && !UDPSocket.isClosed()) {
			UDPSocket.close();
			UDPSocket = null;
		}

		receiveUDPThread = null;
		sendUDPThread = null;

	}
	
	private Runnable receiver = new Runnable() {
		
		@Override
		public void run() {
			Util.d("UDPSocketReceiver thread run");
			
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
						continue;
					}

					String revStr = new String(receiveBuffer,"utf-8");
					revStr = revStr.trim();

					listener.reportReceivedPacket(revStr);
					
				} catch (IOException e) {
					Util.d("udp server: Received error");
					
					continue;
				}
			}
		}
	};
	
	private Runnable send = new Runnable() {
		
		@Override
		public void run() {
			Util.d("UDPSocketsend thread run");
			try {
				if (UDPSocket == null)
					UDPSocket = new DatagramSocket(REV_PORT);

			} catch (SocketException e) {
				e.printStackTrace();
			}
			DatagramPacket sendDatagramPacket;
			
			while(sendData == null){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			
				try {
					if(sendData != null){
						int length = sendData.length();
						// 发送UDP报文
						sendDatagramPacket = new DatagramPacket(sendData.getBytes(), length, InetAddress.getByName(mLocalIp), SEND_PORT);
						UDPSocket.send(sendDatagramPacket);
						sendData = null;
					}
					
				} catch (Exception e) {
					Util.d("send socket exception");
					listener.reportSendError();
					sendData = null;
					e.printStackTrace();
	
				}
			
			
			
		}
	};

}
