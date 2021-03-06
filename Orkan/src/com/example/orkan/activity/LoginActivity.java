package com.example.orkan.activity;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.orkan.R;
import com.example.orkan.adapter.TimingListAdapter;
import com.example.orkan.dialog.MessageDialog;
import com.example.orkan.third.kprogresshud.KProgressHUD;
import com.example.orkan.util.GetSharedData;
import com.example.orkan.util.SaveSharedData;
import com.example.orkan.util.Util;

public class LoginActivity extends BaseActivity{
	ListView timinglistView;
	TimingListAdapter adapter;
	TextView title_tx;
	ImageView title_im;
	ImageView title_left;
	ImageView content_im;

	EditText accountEdit;
	EditText pwdEdit;
	Button loginButton;
	Button forgetButton;
	Button regButton;
	Button outlogin_button;
	ImageView code_eye;
	boolean isHigh = true;
	String autoMac;
	
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    	autoMac = GetSharedData.GetData(this, Util.SAVED_LOGIN_DEVICEMAC, "null");
	  	String savedAccount = GetSharedData.GetData(this, Util.SAVED_ACCOUNT, "");
    	String savedPWD = GetSharedData.GetData(this, Util.SAVED_PWD, "");
		if(!"null".contains(autoMac) && !"".contains(savedPWD) && !"".contains(savedAccount))
			handler.sendEmptyMessageDelayed(0, 200);
        
    }
    
    private void init(){
    	Util.MQTT_DEVICE_ID = "null";
    	title_tx = (TextView)findViewById(R.id.title_tx);
    	title_tx.setText(R.string.title_login);
    	//content_im = (ImageView)findViewById(R.id.content_im);
    	
    	title_left= (ImageView)findViewById(R.id.title_left);
    	code_eye = (ImageView)findViewById(R.id.code_eye);
    	title_left.setVisibility(View.VISIBLE);
    	
    	accountEdit = (EditText)findViewById(R.id.account_edit);
    	pwdEdit = (EditText)findViewById(R.id.pwd_edit);
    	
    	forgetButton = (Button)findViewById(R.id.forget_button);
    	outlogin_button = (Button)findViewById(R.id.outlogin_button);
    	outlogin_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(LoginActivity.this, MainActivity.class));
				LoginActivity.this.finish();
			}
		});
    	forgetButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	startActivity(new Intent(LoginActivity.this,GetPwdActivity.class));
           
            }
        });
    	
    	code_eye.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO 自动生成的方法存根
				isHigh = !isHigh;
				if(isHigh){
					pwdEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					code_eye.setImageResource(R.drawable.code_hide);
				}else{
					pwdEdit.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					code_eye.setImageResource(R.drawable.code_show);
				}
			}
		});
    	
    	regButton = (Button)findViewById(R.id.reg_button);
    	regButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	startActivity(new Intent(LoginActivity.this,RegActivity.class));
           
            }
        });
    	loginButton = (Button)findViewById(R.id.login_button);
    	loginButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	hud = KProgressHUD.create(LoginActivity.this)
                        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);              	
            	beginAll();
            	login();
            	
            }
        });
    	
    	String savedAccount = GetSharedData.GetData(this, Util.SAVED_ACCOUNT, "");
    	accountEdit.setText(savedAccount);
    	String savedPWD = GetSharedData.GetData(this, Util.SAVED_PWD, "");
    	pwdEdit.setText(savedPWD);
    }
    
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            beginAll();
            autoLogin();
        }
    };


    
	private boolean autoLogin(){
		Log.v("snake", "login");
		autoMac = GetSharedData.GetData(this, Util.SAVED_LOGIN_DEVICEMAC, "null");
	  	String savedAccount = GetSharedData.GetData(this, Util.SAVED_ACCOUNT, "");
    	String savedPWD = GetSharedData.GetData(this, Util.SAVED_PWD, "");
		if("null".contains(autoMac) || "".contains(savedPWD)|| "".contains(savedAccount))
			return false;
		autoLogin_mac();
		return true;
	}
	

	
	private void autoLogin_mac(){

    	SaveSharedData.SaveData(this, Util.SAVED_ACCOUNT, accountEdit.getText().toString().trim());
    	SaveSharedData.SaveData(this, Util.SAVED_PWD, pwdEdit.getText().toString().trim());
    	
		FinalHttp fh = new FinalHttp();
		AjaxParams params = new AjaxParams();
		params.put("account", accountEdit.getText().toString().trim());
		params.put("password", pwdEdit.getText().toString().trim());
		 
		fh.post(Util.URL+"Login/login", params, new AjaxCallBack<String>() {
		@Override
		public void onSuccess(String t){
			super.onSuccess(t);
			try {
				Util.d(t);
				JSONObject jsonData = new JSONObject(t);
				int code = jsonData.getInt("code");
				if (code == 1){
					JSONObject data = jsonData.getJSONObject("data");
					Util.USER_ID = data.getString("id");
					Util.USER_TOCKEN = data.getString("token");
					
					FinalHttp fh = new FinalHttp();
					AjaxParams params = new AjaxParams();
					params.put("id", Util.USER_ID);
					params.put("token", Util.USER_TOCKEN);
					 
					fh.post(Util.URL+"Device/getList", params, new AjaxCallBack<String>() {
					@Override
					public void onSuccess(String t){
						super.onSuccess(t);
						try {
							Util.d(t);
							JSONObject jsonData = new JSONObject(t);
							int code = jsonData.getInt("code");
							if (code == 1){
								JSONArray data = jsonData.getJSONArray("data");
								int length = data.length();  
								Util.d(length+"");
								//此版本仅有一个设备
								if (length < 1){
									cancelAll();
								}else{
									//TODO
									Util.START_FROM_LOGIN = 1;
									for (int i = 0; i < length; i++) {
										JSONObject deviceObj = data.getJSONObject(i);
										String mac = deviceObj.getString("mac");
										if(!mac.contains(autoMac))
											continue;
		
										Util.MQTT_DEVICE_NAME = deviceObj.getString("deviceName");
										Util.MQTT_USER_MAC = mac;
										Util.MQTT_DEVICE_ID = deviceObj.getString("id");
										SaveSharedData.SaveData(getApplicationContext(),Util.SAVED_LOGIN_DEVICEMAC, Util.MQTT_USER_MAC);
										startActivity(new Intent(LoginActivity.this, MainActivity.class));
										LoginActivity.this.finish();
								  
									}
									
									cancelAll();
							    	
							    	
								}
								
							}else{
								MessageDialog msgDialog = new MessageDialog(LoginActivity.this,
										jsonData.getString("msg"));
								msgDialog.show();
							}
							
						} catch (JSONException e) {
								e.printStackTrace();
							}
						cancelAll();
						}
					
						@Override
						public void onFailure(Throwable t, int errorNo, String strMsg){
							super.onFailure(t, errorNo, strMsg);
								cancelAll();
						}
					});
						
				}else{
					MessageDialog msgDialog = new MessageDialog(LoginActivity.this,
							jsonData.getString("msg"));
					msgDialog.show();
					cancelAll();
				}
		
			} catch (JSONException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}
			cancelAll();
			}
		
			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg){
				super.onFailure(t, errorNo, strMsg);
					MessageDialog msgDialog = new MessageDialog(LoginActivity.this,
							getString(R.string.login_fail));
					msgDialog.show();
					cancelAll();
			}
		});
    	
    
    
	}
	

    private void login(){
    	if(accountEdit.getText().toString().trim().equals("") || 
    			pwdEdit.getText().toString().trim().equals("")){
    		MessageDialog msgDialog = new MessageDialog(LoginActivity.this,
					getString(R.string.input_null));
			msgDialog.show();
			return;
    	}
    	
    	SaveSharedData.SaveData(this, Util.SAVED_ACCOUNT, accountEdit.getText().toString().trim());
    	SaveSharedData.SaveData(this, Util.SAVED_PWD, pwdEdit.getText().toString().trim());
    	
		FinalHttp fh = new FinalHttp();
		AjaxParams params = new AjaxParams();
		params.put("account", accountEdit.getText().toString().trim());
		params.put("password", pwdEdit.getText().toString().trim());
		 
		fh.post(Util.URL+"Login/login", params, new AjaxCallBack<String>() {
		@Override
		public void onSuccess(String t){
			super.onSuccess(t);
			try {
				Util.d(t);
				JSONObject jsonData = new JSONObject(t);
				int code = jsonData.getInt("code");
				if (code == 1){
					JSONObject data = jsonData.getJSONObject("data");
					Util.USER_ID = data.getString("id");
					Util.USER_TOCKEN = data.getString("token");
					
					FinalHttp fh = new FinalHttp();
					AjaxParams params = new AjaxParams();
					params.put("id", Util.USER_ID);
					params.put("token", Util.USER_TOCKEN);
					 
					fh.post(Util.URL+"Device/getList", params, new AjaxCallBack<String>() {
					@Override
					public void onSuccess(String t){
						super.onSuccess(t);
						try {
							Util.d(t);
							JSONObject jsonData = new JSONObject(t);
							int code = jsonData.getInt("code");
							if (code == 1){
								JSONArray data = jsonData.getJSONArray("data");
								int length = data.length();  
								Util.d(length+"");
								//此版本仅有一个设备
								if (length < 1){
							//	if(true){	
									startActivity(new Intent(LoginActivity.this,APConfigTypeActivity.class));	    
							    	LoginActivity.this.finish();

								}else{
									Util.START_FROM_LOGIN = 1;
									startActivity(new Intent(LoginActivity.this,DeviceListActivity.class));
								    
							    	LoginActivity.this.finish();
								}
								
							}else{
								MessageDialog msgDialog = new MessageDialog(LoginActivity.this,
										jsonData.getString("msg"));
								msgDialog.show();
							}
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
								e.printStackTrace();
							}
						cancelAll();
						}
					
						@Override
						public void onFailure(Throwable t, int errorNo, String strMsg){
							super.onFailure(t, errorNo, strMsg);
//								MessageDialog msgDialog = new MessageDialog(LoginActivity.this,
//										"登录失败");
//								msgDialog.show();
								cancelAll();
						}
					});
						
				}else{
					MessageDialog msgDialog = new MessageDialog(LoginActivity.this,
							jsonData.getString("msg"));
					msgDialog.show();
					cancelAll();
				}
		
			} catch (JSONException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}
			cancelAll();
			}
		
			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg){
				super.onFailure(t, errorNo, strMsg);
					MessageDialog msgDialog = new MessageDialog(LoginActivity.this,
							getString(R.string.login_fail));
					msgDialog.show();
					cancelAll();
			}
		});
    	
    
    }
}
