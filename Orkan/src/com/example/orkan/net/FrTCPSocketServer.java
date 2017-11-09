package com.example.orkan.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import com.example.orkan.util.Util;

public class FrTCPSocketServer {
	
	private static int PORT = 20001;
	private static String mLocalIp = "10.10.100.254";
	private Thread receiveTPCThread = null;
	private Thread sendTCPThread = null;
	private Socket tcpClient;
	private DataOutputStream outputStream = null;
	private DataInputStream inputStream = null;
	private int READ_BUFFER_SIZE = 512;
	private int WRITE_BUFFER_SIZE = 512;
	

	private static final int BUFFER_LENGTH = 512;
	private boolean isThreadRunning;
	private static DatagramSocket UDPSocket = null;
	private DatagramPacket receiveDatagramPacket;
	private byte[] receiveBuffer = new byte[BUFFER_LENGTH];
	private FrUDPConnectListener listener = null;
	private String sendData = null;

	public FrTCPSocketServer(String Ip,int SendPort) {
		mLocalIp = Ip;
		PORT = SendPort;
	}
	
	public void addUDPListener(FrUDPConnectListener listener){
		this.listener = listener;
	}
	
	public void sendSocketData(String sendData){
		this.sendData = sendData;
	}
	
	// 开启server
	public void startTCPSocketThread() {
		new Thread(socketRun).start();
	}
	
	
	// 关闭server
	public void stopTCPSocketThread() {
		
		isThreadRunning = false;
		if (tcpClient != null && !tcpClient.isClosed()) {
			try {
				tcpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			UDPSocket = null;
		}

		receiveTPCThread = null;
		sendTCPThread = null;

	}
	
	private Runnable socketRun = new Runnable() {
		
		@Override
		public void run() {
			try {
				isThreadRunning = true;
				if(tcpClient != null){
					tcpClient.close();
				}
				tcpClient = new Socket(InetAddress.getByName(mLocalIp), PORT);
				outputStream = new DataOutputStream(tcpClient.getOutputStream());
				inputStream = new DataInputStream(tcpClient.getInputStream());
				receiveTPCThread = new Thread(receiver);
				sendTCPThread = new Thread(send);
			} catch (Exception e) {
				stopTCPSocketThread();
				e.printStackTrace();
			}
		}
	};
	
	
	private Runnable receiver = new Runnable() {
		
		@Override
		public void run() {
			Util.d("UDPSocketReceiver thread run");
			final byte[] readBuffer = new byte[READ_BUFFER_SIZE];
			while (isThreadRunning) {
				
				try {
					inputStream.read(readBuffer);
					String revStr = new String(readBuffer,"utf-8");
					listener.reportReceivedPacket(revStr);
				} catch (Exception e) {
					stopTCPSocketThread();
					e.printStackTrace();
				}
	
				
			}
		}
	};
	
	private Runnable send = new Runnable() {
		
		@Override
		public void run() {
			
			while(sendData == null){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			
			try {
				if(sendData != null){
					outputStream.writeChars(sendData);
					outputStream.flush();
					sendData = null;
				}
				
			} catch (Exception e) {
				stopTCPSocketThread();
				sendData = null;
				e.printStackTrace();

			}
			
			
			
		}
	};

}
