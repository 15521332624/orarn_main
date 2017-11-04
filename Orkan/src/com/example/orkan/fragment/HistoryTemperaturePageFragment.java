package com.example.orkan.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.orkan.R;
import com.example.orkan.fragment.HistoryPMPageFragment.ProbeHandler;
import com.example.orkan.net.UDPSocketServer;
import com.example.orkan.net.UDPWatcher;
import com.example.orkan.third.kprogresshud.KProgressHUD;
import com.example.orkan.util.Util;
import com.example.orkan.view.BezierCurveChart;
import com.example.orkan.view.BezierCurveChart.Point;

public class HistoryTemperaturePageFragment extends Fragment implements View.OnClickListener{
    public View rootView;
    BezierCurveChart bezierCurveChart;
 	
 	 KProgressHUD hud;
 	 String tipText = "°C";
	 private ProbeHandler probeHandler = new ProbeHandler();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	 rootView = inflater.inflate(R.layout.fragment_history_temperature, container, false);
    	 initView();
         initData();
         return rootView;
    }
    private void initView(){
   	 hud = KProgressHUD.create(getActivity())
             .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
 		
     }
    private void initData(){
    	bezierCurveChart = 
 				(BezierCurveChart) rootView.findViewById(R.id.bezier_curve_chart);
 		float max = 0;
	
		for(int i=0;i<Util.POINTS_INDOOR_TEMPERATURE.size();i++) {
			float tpm = Util.POINTS_INDOOR_TEMPERATURE.get(i).y;
			if(max < tpm){
				max = tpm;
			}
		}
		float maxInt = (((int)max)/10+1)*10;
		if (Util.POINTS_INDOOR_TEMPERATURE.size()>0){
			bezierCurveChart.init(Util.POINTS_INDOOR_TEMPERATURE ,null, 
				new String[] { Util.INITIAL_STATUS_HISTORY_STARTTIME, Util.INITIAL_STATUS_HISTORY_ENDTIME },new String[] { "0", maxInt+""}, tipText,maxInt);
		}
    } 
    public void refresh(){
    	 if (rootView == null){
    		 return;
    	 }
    	 initData();
    }
     
     //handler更新UI
    protected class ProbeHandler extends Handler {
        public ProbeHandler() {
         
        }

	 @Override
	 public void handleMessage(Message msg) {
	 	JSONObject jsonData= (JSONObject)msg.obj;
	 	// TODO Auto-generated method stub
		switch(msg.what){
			case Util.INSTANCE_CODE_HISTORY_REV:
				try {
					Util.INITIAL_STATUS_HISTORY_STARTTIME = jsonData.getString("StartTime");
					Util.INITIAL_STATUS_HISTORY_ENDTIME = jsonData.getString("EndTime");
					//JSONArray pmArray = jsonData.getJSONArray("PmData");
					//JSONArray humidityArray = jsonData.getJSONArray("HumidityData");
					JSONArray tempArray = jsonData.getJSONArray("TempData");
					float max = 0;
					Util.POINTS_INDOOR_TEMPERATURE = new ArrayList<BezierCurveChart.Point>();
					for(int i=0;i<tempArray.length();i++) {
						float tpm = Float.parseFloat(tempArray.getString(i));
						Util.POINTS_INDOOR_TEMPERATURE.add(new Point(i, tpm));
						if(max < tpm){
							max = tpm;
						}
					}
					float maxInt = (((int)max)/10+1)*10;
					if (Util.POINTS_INDOOR_TEMPERATURE.size()>0){
						bezierCurveChart.init(Util.POINTS_INDOOR_TEMPERATURE ,null, 
							new String[] { Util.INITIAL_STATUS_HISTORY_STARTTIME, Util.INITIAL_STATUS_HISTORY_ENDTIME },new String[] { "0", maxInt+""}, tipText,maxInt);
					}
				} catch (JSONException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				break;
			}
	 	}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
