package com.example.orkan.activity;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.example.orkan.util.Util;

public class RegActivity extends BaseActivity{
	ListView timinglistView;
	TimingListAdapter adapter;
	TextView title_tx;
	ImageView title_im;
	ImageView title_left;
	ImageView content_im;
	private TimeCount time;
	EditText accountEdit;
	EditText pwdEdit;
	EditText checknumEdit;
	Button checknumButton;
	Button regutton;
	
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        init();
    }
    
    private void init(){
    	title_tx = (TextView)findViewById(R.id.title_tx);
    	title_tx.setText(R.string.title_reg);
    	//content_im = (ImageView)findViewById(R.id.content_im);
    
    	accountEdit = (EditText)findViewById(R.id.account_edit);
    	pwdEdit = (EditText)findViewById(R.id.pwd_edit);
    	checknumEdit = (EditText)findViewById(R.id.checknum_edit);
    	
    	checknumButton = (Button)findViewById(R.id.checknum_button);
    	
    	checknumButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	time.start();//开始计时
            
            	getCheckNum();
            }
        });
    	
    	regutton = (Button)findViewById(R.id.reg_button);
    	regutton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            
            	reg();
            }
        });
    	
    	time = new TimeCount(60000, 1000);//构造CountDownTimer对象
    	title_left= (ImageView)findViewById(R.id.title_left);
    	title_left.setImageResource(R.drawable.back);
    	title_left.setVisibility(View.VISIBLE);
    	title_left.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	RegActivity.this.finish();
            }
        });
    	
    }
    private void getCheckNum(){
    	if(accountEdit.getText().toString().trim().equals("")){
    		MessageDialog msgDialog = new MessageDialog(RegActivity.this,
					"手机号为空");
			msgDialog.show();
			return;
    	}
    	beginAll();
    	 FinalHttp fh = new FinalHttp();
 		 fh.get(Util.URL+"Login/register/sendCode/" + accountEdit.getText().toString().trim(), new AjaxCallBack<String>() {
 			@Override
 			public void onSuccess(String t){
 				super.onSuccess(t);
 				try {
 					Util.d(t);
					JSONObject jsonData = new JSONObject(t);
					int code = jsonData.getInt("code");
					if (code == 1){
						MessageDialog msgDialog = new MessageDialog(RegActivity.this,
								"发送成功");
						msgDialog.show();
					}else{
						MessageDialog msgDialog = new MessageDialog(RegActivity.this,
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
	 				MessageDialog msgDialog = new MessageDialog(RegActivity.this,
	 						"发送失败\n请检查网络连接");
					msgDialog.show();
					cancelAll();
 			}
 		});
    }
    private void reg(){
    	if(accountEdit.getText().toString().trim().equals("") || 
    			pwdEdit.getText().toString().trim().equals("") ||
    			checknumEdit.getText().toString().trim().equals("")){
    		MessageDialog msgDialog = new MessageDialog(RegActivity.this,
					"输入为空");
			msgDialog.show();
			return;
    	}
    	 beginAll();
    	 FinalHttp fh = new FinalHttp();
         AjaxParams params = new AjaxParams();
         params.put("account", accountEdit.getText().toString().trim());
         params.put("name", "Orkan用户");
         params.put("password", pwdEdit.getText().toString().trim());
         params.put("captcha", checknumEdit.getText().toString().trim());
         
 		 fh.post(Util.URL+"Login/register", params, new AjaxCallBack<String>() {
 			@Override
 			public void onSuccess(String t){
 				super.onSuccess(t);
 				try {
 					Util.d(t);
					JSONObject jsonData = new JSONObject(t);
					int code = jsonData.getInt("code");
					if (code == 1){
						MessageDialog msgDialog = new MessageDialog(RegActivity.this,
								"注册成功");
						msgDialog.show(new OnClickListener(){
					        @Override
					        public void onClick(View v)
					        {
					        	RegActivity.this.finish();	
					        }
					    });
					}else{
						MessageDialog msgDialog = new MessageDialog(RegActivity.this,
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
	 				MessageDialog msgDialog = new MessageDialog(RegActivity.this,
	 						"注册失败\n请检查网络连接");
					msgDialog.show();
					cancelAll();
 			}
 		});
    }
    
    /* 定义一个倒计时的内部类 */
    class TimeCount extends CountDownTimer {
	    public TimeCount(long millisInFuture, long countDownInterval) {
	    super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
	    }
	    @Override
	    public void onFinish() {//计时完毕时触发
	    	checknumButton.setText(R.string.get_checknum);
	    	checknumButton.setClickable(true);
	    	checknumButton.setTextColor(RegActivity.this.getResources().getColor(R.color.theme_color));
	    }
	    @Override
	    public void onTick(long millisUntilFinished){//计时过程显示
	    	checknumButton.setClickable(false);
	    	checknumButton.setTextColor(RegActivity.this.getResources().getColor(R.color.lb_tx));
	    	checknumButton.setText(millisUntilFinished /1000+"秒");
    	}
    }
}
