package com.example.orkan.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

public class ApConfDevTypeAdapter extends SimpleAdapter {

	public ApConfDevTypeAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to) {
		super(context, data, resource, from, to);
	}
	
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder;
        if(convertView == null){
            convertView = super.getView(position,convertView,parent);
            mHolder = new ViewHolder();
            convertView.setTag(mHolder);
        }else{
            mHolder = (ViewHolder) convertView.getTag();
        }
        
        
        return convertView;
    }
    
    //Viewholder缓存
    private class ViewHolder{
    }
	
	

}
