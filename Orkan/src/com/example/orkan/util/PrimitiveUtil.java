package com.example.orkan.util;

/**
 * Created by liboustc on 2015/9/2.
 */
public class PrimitiveUtil {

    public static byte[] intToBytes(int value) {
        byte[] dest = new byte[4];
        dest[0] = (byte) ((value >> 24) & 0xff);
        dest[1] = (byte) ((value >> 16) & 0xff);
        dest[2] = (byte) ((value >> 8) & 0xff);
        dest[3] = (byte) (value & 0xff);

        return dest;
    }
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = ( ((src[offset] & 0xFF)<<24)
                |((src[offset+1] & 0xFF)<<16)
                |((src[offset+2] & 0xFF)<<8)
                |(src[offset+3] & 0xFF));
        return value;
    }
    
    public static byte charToASCIIByte(char c) {
        byte b = (byte) (c & 0xFF);
        return b;
    }
    
    public static char byteToChar(byte[] b) {
        char c = (char) (((b[0] & 0xFF) << 8) | (b[1] & 0xFF));
        return c;
    }
    
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }

        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }

    public static void main(String[] args) {
        int a = 0x12345678;
        System.out.println(bytesToHexString(intToBytes(a)));
    }

}
