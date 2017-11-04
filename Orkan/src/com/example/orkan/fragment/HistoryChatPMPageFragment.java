package com.example.orkan.fragment;

import java.util.ArrayList;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.orkan.R;
import com.example.orkan.util.Util;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public class HistoryChatPMPageFragment extends Fragment implements
		View.OnClickListener {
	public View rootView;
	LineChart mChart;

	// KProgressHUD hud;
	String tipText = "ug/m3";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.d("history pm oncreate");
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_history_pm, container,
				false);

		initData();
		return rootView;
	}

	public void initData() {
		mChart = (LineChart) rootView
				.findViewById(R.id.bezier_curve_chart);
		initLineChart();
	}
	

	 
	public void refresh() {
		if (rootView == null) {
			return;
		}

	     LineData data = getLineData();  
         mChart.setData(data);  
		 mChart.invalidate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}



	private void initLineChart() {
	       mChart.setDrawGridBackground(false);  
	        mChart.setDrawBorders(false);  
	        // 设置右下角描述  
	        mChart.setDescription("");  
	        //设置透明度  
	        mChart.setAlpha(0.8f);  
	        //设置网格底下的那条线的颜色  
	    //    mChart.setBorderColor(Color.rgb(213, 216, 214));  
	        //设置高亮显示  
	    //    mChart.setHighlightEnabled(true);  
	        //设置是否可以触摸，如为false，则不能拖动，缩放等  
	        mChart.setTouchEnabled(true);  
	        //设置是否可以拖拽  
	        mChart.setDragEnabled(false);  
	        //设置是否可以缩放  
	        mChart.setScaleEnabled(false);  
	        //设置是否能扩大扩小  
	        mChart.setPinchZoom(false);  
	        mChart.getXAxis().setPosition(XAxisPosition.BOTTOM);
	        mChart.getXAxis().setAvoidFirstLastClipping(true);
	        mChart.getXAxis().setDrawGridLines(false);
	        //mChart.getXAxis().setLabelsToSkip(0);
	      // mChart.getXAxis().resetLabelsToSkip(); 
	       
	        YAxis leftAxis = mChart.getAxisLeft();
	        YAxis rightAxis = mChart.getAxisRight();
	        rightAxis.setEnabled(false);	
	        leftAxis.setDrawGridLines(true);
	        leftAxis.setStartAtZero(true);
	       // leftAxis.setAxisMaxValue(150);
	       // leftAxis.setLabelCount(15, true);
	       // leftAxis.setShowOnlyMinMax(true);
	        // 加载数据  
	        LineData data = getLineData();  
	        mChart.setData(data);  
	        /** 
	         * ====================3.x，y动画效果和刷新图表等=========================== 
	         */  
	        mChart.invalidate();
		
		
	}
	
    private LineData getLineData() {  
  
        ArrayList<String> xVals = new ArrayList<String>();  
        for (int i = 0; i < Util.CHAR_POINTS_INDOOR_PM.size(); i++) {  
            xVals.add(Util.CHAR_POINTS_INDOOR_PM.get(i).time);  
        }  
        
        ArrayList<Entry> yVals = new ArrayList<Entry>();  
        for (int i = 0; i < Util.CHAR_POINTS_INDOOR_PM.size(); i++) {  
            yVals.add(new Entry(Util.CHAR_POINTS_INDOOR_PM.get(i).value, i));  
        }  
  
        LineDataSet set1 = new LineDataSet(yVals, "X-Time Y-mg/m3");  
        set1.setDrawCubic(false);  //设置曲线为圆滑的线  
        set1.setCubicIntensity(0.2f);  
        set1.setDrawFilled(false);  //设置包括的范围区域填充颜色  
        set1.setDrawCircles(false);  //设置有圆点  
        set1.setLineWidth(3f);    //设置线的宽度  
        set1.setCircleSize(6f);   //设置小圆的大小  
        set1.setValueTextSize(0);
       // set1.setValueTextColor(Color.RED);
        set1.setCircleColor(Color.parseColor("#32C5BD"));
        set1.setColor(Color.parseColor("#32C5BD"));    //设置曲线的颜色  
  
        return new LineData(xVals, set1);  
    }  


}
