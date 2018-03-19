package com.example.orkan.adapter;

import com.example.orkan.R;
import com.example.orkan.fragment.HistoryChatHumidityPageFragment;
import com.example.orkan.fragment.HistoryChatPMPageFragment;
import com.example.orkan.fragment.HistoryChatTemperaturePageFragment;
import com.example.orkan.util.Util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class HistoryTabAdapter extends FragmentPagerAdapter {
	// 内容标题
	public static final String[] DONG_HUA_TITLE = new String[] {"PM2.5","湿度",
			"温度"};
	public static final int[] DONG_HUA_TITEL_INT = new int[] {R.string.history_pm,R.string.history_temperature_h,R.string.history_humidity};
	public HistoryChatPMPageFragment pmPage;
	public HistoryChatHumidityPageFragment humidityPage;
	public HistoryChatTemperaturePageFragment temperaturePage;
	public FragmentManager fm_t = null;
	
	public HistoryTabAdapter(FragmentManager fm) {
		super(fm);
		fm_t = fm;
		fm.getFragments().clear();
		pmPage = new HistoryChatPMPageFragment();
		humidityPage = new HistoryChatHumidityPageFragment();
		temperaturePage = new HistoryChatTemperaturePageFragment();
	
		// TODO Auto-generated constructor stub
	}
	
	// 获取项
	@Override
	public Fragment getItem(int position) {
		Util.d("Fragment position:" + position);
		switch (position) {
		case 0:
			return pmPage;
		case 1:
			return temperaturePage;
		case 2:
			return humidityPage;
		default:
			return pmPage;
		}
	}

	public void notifypm(){
		pmPage.refresh();
	}
	public void notifytemp(){
		temperaturePage.refresh();
	}
	public void notifyhumi(){
		humidityPage.refresh();
	}
	
	public void notifyall(){
		pmPage.refresh();
		humidityPage.refresh();
		temperaturePage.refresh();
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		// 返回页面标题
		return Util.mcontext.getString(DONG_HUA_TITEL_INT[position % DONG_HUA_TITLE.length]);
		//return fm_t.findFragmentById(0).getActivity().getString(DONG_HUA_TITEL_INT[position % DONG_HUA_TITLE.length]);
		
		//return DONG_HUA_TITLE[position % DONG_HUA_TITLE.length];
	}

	@Override
	public int getCount() {
		// 页面个数
		return DONG_HUA_TITLE.length;
	}

}
