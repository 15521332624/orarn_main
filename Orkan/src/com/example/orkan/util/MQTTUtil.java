package com.example.orkan.util;



public class MQTTUtil {

	public static byte[] Head = { (byte) 0xaa, (byte) 0x55, (byte) 0xaa,
			(byte) 0x55 };

	public static short Version = 0;

	public static short COMMA0 = 1;
	public static short COMMA1 = 2;
	public static short COMMA2 = 3;
	public static short COMMA3 = 4;
	public static short COMMA4 = 5;
	public static short COMMA5 = 6;
	public static short COMMAD = 9;
	public static short COMMAB = 13;
	public static short COMMAc = 15;
	public static short COMMAF = 11;
	public static short COMMAa = 12;

	public static int A1DATATEMPERATURE = 0;
	public static int A1DATAHUMIDITY = 0;
	public static int A1DATAVOC = 0;
	public static int A1DATACO2 = 0;
	public static int A1DTATPM25 = 0;
	public static int A1DATAHCH0 = 0;
	public static int A1DATALIGHT = 0;
	public static int A1DATAERROR = 0xd8;

	public static int A3DATAID = 0;
	public static int A3DATAOC = 0;
	public static int A3DATAFLY = 0;
	public static int A4DATAFLY2 = 0;

	public static boolean analyseData(byte[] rev) {
		Util.d("rev = " + byte2hex(rev));
		int lenth = rev.length;
		Util.d("revlenth= " + lenth);
		int dataLenth = 0;
		short comm = 0;
		if (lenth < 10)
			return false;
		// Head Analyse
		for (int i = 0; i < 4; i++) {
			if (rev[i] != Head[i])
				return false;
		}
		dataLenth = rev[5];
		comm = rev[7];
		Util.d("dataLenth= " + dataLenth);
		//byte[] crcCheckData = subBytes(rev, 12, dataLenth);

		if (comm == COMMA1) {
			int dataBg = 12;
			for (int j = 0; j < dataLenth; j++) {
				switch (j) {
				case 0:
					// temp
					A1DATATEMPERATURE = rev[12];
					Util.d("A1DATATEMPERATURE = " + A1DATATEMPERATURE);
					break;
				case 1:
					A1DATAHUMIDITY = rev[13];
					break;
				case 2:
					A1DATAVOC = rev[14];
					break;
				case 3:
					break;
				case 4:
					// A1DATAVOC = rev[14];
					A1DATACO2 = (short) (((rev[15] & 0x00FF) << 8) | (0x00FF & rev[16]));
					break;
				case 5:
					break;
				case 6:
					A1DTATPM25 = (short) (((rev[dataBg + j + 1] & 0x00FF) << 8) | (0x00FF & rev[dataBg
							+ j]));
					break;
				case 8:
					break;
				case 9:
				case 10:
					break;

				default:
					break;
				}

			}

		} else if (comm == COMMA3) {

		}

		return true;
	}

	public static byte[] packetData(byte comm, byte[] commData) {
		byte[] data = new byte[12];
		// HEAD
		data[0] = Head[0];
		data[1] = Head[1];
		data[2] = Head[2];
		data[3] = Head[3];
		// Length
		data[4] = 0;
		data[5] = (byte) commData.length; // Lenth
		// Comm
		data[6] = 0;
		data[7] =  comm;
		// CRC
		int crc = CRC16.calcCrc16(commData);
		data[8] = (byte) ((crc & 0xff00) >> 8);
		data[9] = (byte) (crc & 0xff); // CRC
		
		// Version
		data[10] = 0;
		data[11] = 0;
		// Merger Data
		Util.d("send data  = " + byte2hex(byteMerger(data, commData)));
		Util.d("len = " + commData.length);
		return byteMerger(data, commData);
	}
//
//	public static byte[] packetA0Data() {
//		byte[] data = new byte[12];
//		data[0] = Head[0];
//		data[1] = Head[1];
//		data[2] = Head[2];
//		data[3] = Head[3];
//		data[4] = 0;
//		data[5] = 0; // Lenth
//		data[6] = 0;
//		data[7] = 12;
//		byte[] dataTemp = {};
//		int crc = getCrc(dataTemp);
//
//		data[8] = (byte) ((crc & 0xff00) >> 8);
//		; // CRC
//		data[9] = (byte) (crc & 0xff); // CRC
//		Util.d("data = " + byte2hex(data));
//
//		return data;
//	}

	
	public static String byte2hex(byte[] buffer) {
		String h = "";

		for (int i = 0; i < buffer.length; i++) {
			String temp = Integer.toHexString(buffer[i] & 0xFF);
			if (temp.length() == 1) {
				temp = "0" + temp;
			}
			h = h + " " + temp;
		}

		return h;

	}
	public static String byte2hexNospace(byte[] buffer) {
		String h = "";

		for (int i = 0; i < buffer.length; i++) {
			String temp = Integer.toHexString(buffer[i] & 0xFF);
			if (temp.length() == 1) {
				temp = "0" + temp;
			}
			h = h + "" + temp;
		}

		return h;

	}

	public static byte[] subBytes(byte[] src, int begin, int count) {
		byte[] bs = new byte[count];
		System.arraycopy(src, begin, bs, 0, count);
		return bs;
	}

	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}

	public static byte[] addBytes(byte[] data1, byte[] data2) {  
	    byte[] data3 = new byte[data1.length + data2.length];  
	    System.arraycopy(data1, 0, data3, 0, data1.length);  
	    System.arraycopy(data2, 0, data3, data1.length, data2.length);  
	    return data3;  
	  
	} 
	
	public static byte[] addBytes(byte[] data1, byte[] data2, byte[] data3) {  
	    byte[] data4 = new byte[data1.length + data2.length + data3.length];  
	    System.arraycopy(data1, 0, data4, 0, data1.length);  
	    System.arraycopy(data2, 0, data4, data1.length, data2.length); 
	    System.arraycopy(data3, 0, data4, data1.length+data2.length, data3.length);  
	    return data4;  
	  
	} 
	
}
