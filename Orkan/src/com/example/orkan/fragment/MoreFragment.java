package com.example.orkan.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.orkan.R;
import com.example.orkan.activity.APConfigTypeActivity;
import com.example.orkan.activity.AboutUsActivity;
import com.example.orkan.activity.AddDeviceActivity;
import com.example.orkan.activity.DeviceListActivity;
import com.example.orkan.activity.LoginActivity;
import com.example.orkan.dialog.MessageDialog;
import com.example.orkan.net.UDPSocketServer;
import com.example.orkan.third.kprogresshud.KProgressHUD;
import com.example.orkan.util.Util;
import com.example.orkan.view.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

public class MoreFragment extends BaseTabFragment implements View.OnClickListener {
	TextView title_tx;
	View fragmentView;
	Context context;
	ImageView title_more;
	LinearLayout more_unbund_li;
	LinearLayout more_about_li;
	LinearLayout more_help_li;
	LinearLayout more_device_li;
	LinearLayout more_devicelist_li;
	LinearLayout more_quit_li;
	TextView device_id;
	KProgressHUD hud;
	CircleImageView userIcon;
	private ProbeHandler probeHandler = new ProbeHandler();
	protected UDPSocketServer mProbeSocketDiscover;
	private List<Map<String, String>> devicelist = new ArrayList<Map<String, String>>(); // 定义显示的内容包
	private SimpleAdapter deviceSimpleAdapter = null;
	private ListView deviceListView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// //注册观察者
		// mProbeSocketDiscover = UDPSocketServer.getInstance();
		// mProbeSocketDiscover.addWatcher(this);
		context = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentView = inflater.inflate(R.layout.fragment_more, null);
		init(fragmentView);
		initData();
		// getUserData();
		return fragmentView;
	}

	protected void init(View view) {
		super.init(view);
		title_tx = (TextView) view.findViewById(R.id.title_tx);
		title_tx.setText(R.string.title_more);
		title_more = (ImageView) view.findViewById(R.id.title_image);
		title_more.setOnClickListener(this);
		more_unbund_li = (LinearLayout) view.findViewById(R.id.more_unbund_li);
		more_unbund_li.setOnClickListener(this);
		more_about_li = (LinearLayout) view.findViewById(R.id.more_about_li);
		more_about_li.setOnClickListener(this);
		more_help_li = (LinearLayout) view.findViewById(R.id.more_help_li);
		more_help_li.setOnClickListener(this);
		more_device_li = (LinearLayout) view.findViewById(R.id.more_device_li);
		more_device_li.setOnClickListener(this);
		more_devicelist_li = (LinearLayout) view.findViewById(R.id.more_devicelist_li);
		more_devicelist_li.setOnClickListener(this);
		more_quit_li = (LinearLayout) view.findViewById(R.id.more_quit_li);
		more_quit_li.setOnClickListener(this);
		device_id = (TextView) view.findViewById(R.id.device_id);
		device_id.setOnClickListener(this);
		userIcon = (CircleImageView) view.findViewById(R.id.user_icon);
	}

	protected void initData() {
		if("null".endsWith(Util.MQTT_DEVICE_ID))
			device_id.setText(R.string.plzlogin);
		else 
			device_id.setText(Util.MQTT_DEVICE_NAME);

	}

	// 按钮点击事件
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_image:
			// 分享
			Intent shareIntent = new Intent();
			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_TEXT, "新风相伴，自然为邻\nhttp://www.orkan.com.cn/");
			shareIntent.setType("text/plain");

			// 设置分享列表的标题，并且每次都显示分享列表
			startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)));
			break;
		case R.id.more_unbund_li:
			if("null".endsWith(Util.MQTT_DEVICE_ID))
				break;
			final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
			alertDialog.show();
			Window window = alertDialog.getWindow();
			window.setContentView(R.layout.dialog_quit);
			TextView alert_btn_title = (TextView) window.findViewById(R.id.alert_btn_title);
			alert_btn_title.setText(R.string.unbind_device);
			Button reboot_cancel_alert_btn = (Button) window.findViewById(R.id.reboot_cancel_alert_btn);
			reboot_cancel_alert_btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					alertDialog.cancel();
					unbund();
				}
			});
			Button reboot_confirm_alert_btn = (Button) window.findViewById(R.id.reboot_confirm_alert_btn);
			reboot_confirm_alert_btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					alertDialog.cancel();
				}
			});
			break;
		case R.id.more_device_li:
			if("null".endsWith(Util.MQTT_DEVICE_ID))
				break;
			bund();
			// 设备
			break;
		case R.id.more_help_li:
			// 帮助

			break;
		case R.id.more_about_li:
			// 关于我们
			getActivity().startActivity(new Intent(getActivity(), AboutUsActivity.class));
			break;
		case R.id.more_devicelist_li:
			// 设备列表
			if("null".endsWith(Util.MQTT_DEVICE_ID))
				break;
			FragmentActivity c = this.getActivity();
			startActivity(new Intent(c, DeviceListActivity.class));
			c.finish();
			break;
		case R.id.more_quit_li:
			// 退出登录
			startActivity(new Intent(this.getActivity(), LoginActivity.class));
			getActivity().finish();
			break;
		case R.id.device_id:
			// change name
			if("null".endsWith(Util.MQTT_DEVICE_ID))
				gotoLogin();
			else
				showInputDialog();
			break;
		}
	}
	
	private void gotoLogin() {
		startActivity(new Intent(this.getActivity(), LoginActivity.class));
		getActivity().finish();
	}

	EditText dialog_edit;
	private void showInputDialog() {
		final AlertDialog alertDialog = new AlertDialog.Builder(
				getActivity()).create();

		
		View window = View.inflate(getActivity(), R.layout.dialog_edittext, null);
		dialog_edit = (EditText) window
				.findViewById(R.id.message);
		dialog_edit.setFocusable(true);
		TextView dialog_title = (TextView) window
				.findViewById(R.id.title);
		
		Button positiveButton = (Button)window.findViewById(R.id.positiveButton);
		positiveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				changeName(dialog_edit.getText().toString().trim());
				alertDialog.dismiss();
			}
		});
		
		Button negativeButton = (Button)window.findViewById(R.id.negativeButton);
		negativeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				alertDialog.dismiss();
			}
		});
		dialog_title.setText(R.string.input_device_name);
		
		alertDialog.setView(window);
		alertDialog.setOnShowListener(new OnShowListener() {  
            public void onShow(DialogInterface dialog) {  
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);  
                imm.showSoftInput(dialog_edit, InputMethodManager.SHOW_IMPLICIT);  
            }  
        });  
		alertDialog.show();
	}

	// 处理用户对话框
	protected class ProbeHandler extends Handler {
		public ProbeHandler() {

		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Util.HANDLER_GET_DEVICE_SUCCESS:
				// final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
				// alertDialog.show();
				// Window window = alertDialog.getWindow();
				// window.setContentView(R.layout.dialog_list);
				// deviceListView = (ListView)window.findViewById(R.id.dialog_list);
				// TextView dialog_title = (TextView)window.findViewById(R.id.dialog_title);
				// dialog_title.setText("选择设备");
				// devicelist.clear();
				//
				// for (int x = 0; x < Util.INITIAL_STATUS_DEVICE_ID.size(); x++) {
				// Map<String, String> map = new HashMap<String, String>(); // 定义Map集合，保存每一行数据
				// map.put("device", Util.INITIAL_STATUS_DEVICE_ID.get(x)); //
				// 与data_list.xml中的TextView组加匹配
				// devicelist.add(map); // 保存了所有的数据行
				// }
				// deviceSimpleAdapter = new SimpleAdapter(context, devicelist,
				// R.layout.list_dialog, new String[] { "device" } // Map中的key的名称
				// , new int[] { R.id.list_tx }); // 是data_list.xml中定义的组件的资源ID
				// deviceListView.setAdapter(deviceSimpleAdapter);
				// deviceListView.setOnItemClickListener(new OnItemClickListener(){
				// @Override
				// public void onItemClick(AdapterView<?> parent, View view, int position,
				// long id) {
				// // TODO Auto-generated method stub
				// Toast.makeText(getActivity(), "选择的设备为：" +
				// Util.INITIAL_STATUS_DEVICE_ID.get(position), Toast.LENGTH_SHORT).show();
				// alertDialog.cancel();
				// }
				//
				// });

				//
				// AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				// builder.setTitle("选择一个设备");
				// // 指定下拉列表的显示数据
				//
				// // 设置一个下拉的列表选择项
				// builder.setItems(cities, new DialogInterface.OnClickListener()
				// {
				// @Override
				// public void onClick(DialogInterface dialog, int which)
				// {
				//
				// }
				// });
				// builder.show();
				break;
			case Util.HANDLER_GET_DEVICE_TIMEOUT:
				if (hud != null && hud.isShowing()) {
					hud.dismiss();
					Toast.makeText(getActivity(), getString(R.string.connect_fail), Toast.LENGTH_SHORT).show();
					clearRes();
				}
				break;
			default:
				break;
			}
		}
	}

	// private void searchDevice(){
	// Handler handler = new Handler();
	// handler.postDelayed(new Runnable() {
	// @Override
	// public void run() {
	// hud.dismiss();
	// Message msg = new Message();
	// msg.what = Util.HANDLER_GET_DEVICE_SUCCESS;
	// Util.INITIAL_STATUS_DEVICE_ID.clear();
	// Util.INITIAL_STATUS_DEVICE_ID.add("空气净化器1");
	// Util.INITIAL_STATUS_DEVICE_ID.add("空气净化器2");
	// Util.INITIAL_STATUS_DEVICE_ID.add("设备3");
	//
	// probeHandler.sendMessage(msg);
	// Util.d("send device success");
	// }
	// }, 2000);
	// }
	//
	private void scheduleDismiss() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				hud.dismiss();
			}
		}, 2000);
	}

	private void clearRes() {
		// if(mProbeSocketDiscover != null) {
		// mProbeSocketDiscover.stopUDPSocketThread();
		// }
		// if(timer != null) {
		// timer.cancel();
		// }
	}

	// @Override
	// public void getUDPMessage(int code, JSONObject jsonData) {
	// // TODO Auto-generated method stub
	// switch(code){
	// case Util.INSTANCE_CODE_GETSTATUS_REV:
	// if (hud.isShowing()){
	// hud.dismiss();
	// }
	// break;
	// }
	//
	// }
	//
	private void bund() {
		// if (Util.MQTT_USER_MAC !="" && Util.MQTT_USER_MAC != Util.MQTT_TEST_MAC){
		// MessageDialog msgDialog = new MessageDialog(getActivity(),
		// "只能绑定一个设备\n请先解绑");
		// msgDialog.show();
		// return;
		// }

		startActivity(new Intent(getActivity(), APConfigTypeActivity.class));

		getActivity().finish();
	}
	
	private void changeName(final String name) {
		// 解绑
		Util.d("Chane Name  = " + name + " Util.USER_ID = " + Util.USER_ID + " Util.USER_TOCKEN = " + Util.USER_TOCKEN + " Device_Id = " + Util.MQTT_DEVICE_ID);
		FinalHttp fh = new FinalHttp();
		AjaxParams params = new AjaxParams();

		params.put("id", Util.USER_ID);
		params.put("token", Util.USER_TOCKEN);
		params.put("deviceId", Util.MQTT_DEVICE_ID);
		params.put("nick", name);

		fh.post(Util.URL + "Device/edit", params, new AjaxCallBack<String>() {
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				try {
					Util.d(t);
					JSONObject jsonData = new JSONObject(t);
					int code = jsonData.getInt("code");
					Util.d("code  " + code);

					if (code == 1) {
						Util.MQTT_DEVICE_NAME = name;
						device_id.setText(Util.MQTT_DEVICE_NAME);
						MessageDialog msgDialog = new MessageDialog(getActivity(), getString(R.string.change_success));
						msgDialog.show();

					} else {
						MessageDialog msgDialog = new MessageDialog(getActivity(), getString(R.string.change_name_fail));
						msgDialog.show();
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				MessageDialog msgDialog = new MessageDialog(getActivity(), getString(R.string.change_name_fail));
				msgDialog.show();

			}

		});

	}

	private void unbund() {
		hud = KProgressHUD.create(getActivity()).setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);

		hud.show();

		// 解绑
		FinalHttp fh = new FinalHttp();
		AjaxParams params = new AjaxParams();

		params.put("id", Util.USER_ID);
		params.put("token", Util.USER_TOCKEN);

		fh.post(Util.URL + "Device/unBind/" + Util.MQTT_USER_MAC, params, new AjaxCallBack<String>() {
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				try {
					Util.d(t);
					JSONObject jsonData = new JSONObject(t);
					int code = jsonData.getInt("code");
					Util.d("code  " + code);

					if (code == 1) {
						Util.MQTT_USER_MAC = "";
						MessageDialog msgDialog = new MessageDialog(getActivity(), getString(R.string.unbind_success));
						msgDialog.show(new OnClickListener() {
							@Override
							public void onClick(View v) {
								startActivity(new Intent(getActivity(), DeviceListActivity.class));
								getActivity().finish();
							}
						});

					} else {
						MessageDialog msgDialog = new MessageDialog(getActivity(), getString(R.string.unbind_fail));
						msgDialog.show();
					}

					hud.dismiss();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				hud.dismiss();
			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				MessageDialog msgDialog = new MessageDialog(getActivity(), getString(R.string.unbind_fail));
				msgDialog.show();
				hud.dismiss();

			}

		});

	}

	private void getUserData() {

		FinalHttp fh = new FinalHttp();
		AjaxParams params = new AjaxParams();
		params.put("id", Util.USER_ID);
		params.put("token", Util.USER_TOCKEN);

		fh.post(Util.URL + "User/getInfo", params, new AjaxCallBack<String>() {
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				try {
					Util.d(t);
					JSONObject jsonData = new JSONObject(t);
					int code = jsonData.getInt("code");
					if (code == 1) {
						JSONObject data = jsonData.getJSONObject("data");
						Util.USER_PHONE = data.getString("phone");
						Util.USER_NAME = data.getString("name");

						Util.USER_IM = data.getString("avatar");
						device_id.setText(Util.USER_NAME);
						Util.d(Util.USER_IM);
						if (Util.USER_IM != null && !Util.USER_IM.equals("null")) {
							ImageLoader.getInstance().loadImage(Util.USER_IM, new ImageLoadingListener() {

								@Override
								public void onLoadingCancelled(String arg0, View arg1) {
									// TODO Auto-generated method stub

								}

								@Override
								public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
									// TODO Auto-generated method stub
									userIcon.setImageBitmap(arg2);

								}

								@Override
								public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
									// TODO Auto-generated method stub

								}

								@Override
								public void onLoadingStarted(String arg0, View arg1) {
									// TODO Auto-generated method stub

								}

							});
						}

					} else {
						MessageDialog msgDialog = new MessageDialog(context, jsonData.getString("msg"));
						msgDialog.show();
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				MessageDialog msgDialog = new MessageDialog(context, getString(R.string.get_user_fail));
				msgDialog.show();
			}
		});

	}

}