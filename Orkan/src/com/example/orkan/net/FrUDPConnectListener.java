package com.example.orkan.net;



public interface FrUDPConnectListener {
	
	public void onSuccessConnect();
	
	public void onDistoryConnect();
	
	public void reportReceivedPacket(String receivedString);
	
	public void reportSendError();
	
}
