package com.example.orkan.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;
import com.example.orkan.R;
//Ap list adapter
public class DeviceListAdapter extends SimpleAdapter {
    //回调代理
    private TimingListAdapterListener mTimingListAdapterListener;
    private List<Map<String,  Object>> data;
    private Context mcontext;

    public DeviceListAdapter(Context context, List<Map<String, Object>> data,
                              int resource, String[] from, int[] to) {
        super(context, data, resource,from,to);
        this.data = data;
        this.mcontext = context;
    }

    public void setTimingListAdapterListener(TimingListAdapterListener delegate){
        this.mTimingListAdapterListener = delegate;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder;
        if(convertView == null){
            convertView = super.getView(position,convertView,parent);
            mHolder = new ViewHolder();
            // mHolder.icon = (ImageView) convertView.findViewById(R.id.list_item_image);
            convertView.setTag(mHolder);
        }else{
            mHolder = (ViewHolder) convertView.getTag();
        }
        
        TextView device_online_txt = (TextView)convertView.findViewById(R.id.device_online_txt);
        if(device_online_txt.getText().toString().equals(mcontext.getString(R.string.on_line))) {
        		device_online_txt.setTextColor(mcontext.getResources().getColor(R.color.theme_color));	
        }else {
        		device_online_txt.setTextColor(mcontext.getResources().getColor(R.color.theme_tx_color));
        }
       // Map<String,String> map = data.get(position);
        
        return convertView;
    }
    //Viewholder缓存
    private class ViewHolder{
        //private ImageView icon;
    }
    public interface TimingListAdapterListener {
        public void onTimingListCallback();
    }
}