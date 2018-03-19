package com.example.orkan.third.citypicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.orkan.R;
import com.example.orkan.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * author zaaach on 2016/1/26.
 */
public class HotCityGridAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mCities;
    private List<String> mCities_en;
    private List<String> mCities_pinyin;

    public HotCityGridAdapter(Context context) {
        this.mContext = context;
        mCities = new ArrayList<String>();
        mCities_en = new ArrayList<String>();
        mCities_pinyin = new ArrayList<String>();
        if(Util.language == 1) {
	        mCities.add("北京");
	        mCities.add("上海");
	        mCities.add("广州");
	        mCities.add("深圳");
	        mCities.add("杭州");
	        mCities.add("南京");
	        mCities.add("天津");
	        mCities.add("武汉");
	        mCities.add("重庆");
        }else {
            mCities.add("beijing");
	        mCities.add("shanghei");
	        mCities.add("guangzhou");
	        mCities.add("shenzhen");
	        mCities.add("hangzhou");
	        mCities.add("nanjing");
	        mCities.add("tianjin");
	        mCities.add("wuhan");
	        mCities.add("chongqing");
        }
        
        mCities_en.add("北京");
        mCities_en.add("上海");
        mCities_en.add("广州");
        mCities_en.add("深圳");
        mCities_en.add("杭州");
        mCities_en.add("南京");
        mCities_en.add("天津");
        mCities_en.add("武汉");
        mCities_en.add("重庆");
        mCities_pinyin.add("beijing");
        mCities_pinyin.add("shanghei");
        mCities_pinyin.add("guangzhou");
        mCities_pinyin.add("shenzhen");
        mCities_pinyin.add("hangzhou");
        mCities_pinyin.add("nanjing");
        mCities_pinyin.add("tianjin");
        mCities_pinyin.add("wuhan");
        mCities_pinyin.add("chongqing");
    }

    @Override
    public int getCount() {
        return mCities == null ? 0 : mCities.size();
    }

    @Override
    public String getItem(int position) {
        return mCities == null ? null : mCities.get(position);
    }
    
    public String getItemEn(int position) {
    		return mCities_en == null ? null : mCities_en.get(position);
    }
    
    public String getItemPinyin(int position) {
		return mCities_pinyin == null ? null : mCities_pinyin.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        HotCityViewHolder holder;
        if (view == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_hot_city_gridview, parent, false);
            holder = new HotCityViewHolder();
            holder.name = (TextView) view.findViewById(R.id.tv_hot_city_name);
            view.setTag(holder);
        }else{
            holder = (HotCityViewHolder) view.getTag();
        }
        holder.name.setText(mCities.get(position));
        return view;
    }

    public static class HotCityViewHolder{
        TextView name;
    }
}
