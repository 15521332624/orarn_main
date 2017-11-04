package com.example.orkan.model;

/**
 * received message in discover socket
 * @author libo
 */
public class DiscoverRevMsg {
    private int mType;
    private boolean mResult;
    private int mPortHttp;
    private int mPortHttps;

    public void setmType(int type){
        mType = type;
    }
    public int getmType(){
        return mType;
    }
    public void setmResult(boolean result){
        mResult = result;
    }
    public boolean getmResult(){
        return mResult;
    }
    public void setmPortHttp(int port){
        mPortHttp = port;
    }
    public int getmPortHttp(){
        return mPortHttp;
    }
    public void setmPortHttps(int port){
        mPortHttps = port;
    }
    public int getmPortHttps(){
        return mPortHttps;
    }
}
