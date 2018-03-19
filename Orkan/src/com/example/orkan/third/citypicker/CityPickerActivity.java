package com.example.orkan.third.citypicker;

import java.util.List;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.orkan.R;
import com.example.orkan.activity.BaseActivity;
import com.example.orkan.controller.MainController;

/**
 * author zaaach on 2016/1/26.
 */
public class CityPickerActivity extends BaseActivity implements View.OnClickListener {
    public static final int REQUEST_CODE_PICK_CITY = 2333;
    public static final String KEY_PICKED_CITY = "picked_city";

    private ListView mListView;
    private ListView mResultListView;
    private SideLetterBar mLetterBar;
    private EditText searchBox;
    private ImageView clearBtn;
    private ImageView backBtn;
    private ViewGroup emptyView;

    private CityListAdapter mCityAdapter;
    private ResultListAdapter mResultAdapter;
    private List<City> mAllCities;
    private DBManager dbManager;
    private LocationClient mLocClient;  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        initData();
        initView();
        initLocation();
    }

    private void initLocation() {
    	
    	mLocClient = new LocationClient(getApplicationContext());  
        mLocClient.registerLocationListener(new BDLocationListenerImpl());//注册定位监听接口  
          
        /** 
         * LocationClientOption 该类用来设置定位SDK的定位方式。 
         */  
        LocationClientOption option = new LocationClientOption();  
        option.setOpenGps(true); //打开GPRS  
        option.setAddrType("all");//返回的定位结果包含地址信息  
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02  
        option.setPriority(LocationClientOption.GpsFirst); // 设置GPS优先  
        option.setScanSpan(5000); //设置发起定位请求的间隔时间为5000ms  
        option.disableCache(false);//禁止启用缓存定位  
//      option.setPoiNumber(5);    //最多返回POI个数     
//      option.setPoiDistance(1000); //poi查询距离          
//      option.setPoiExtraInfo(true);  //是否需要POI的电话和地址等详细信息          
        mLocClient.setLocOption(option);  //设置定位参数  
          
          
        mLocClient.start();  // 调用此方法开始定位  
    	
    	
//        mLocationClient = new AMapLocationClient(this);
//        AMapLocationClientOption option = new AMapLocationClientOption();
//        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//        option.setOnceLocation(true);
//        mLocationClient.setLocationOption(option);
//        mLocationClient.setLocationListener(new AMapLocationListener() {
//            @Override
//            public void onLocationChanged(AMapLocation aMapLocation) {
//                if (aMapLocation != null) {
//                    if (aMapLocation.getErrorCode() == 0) {
//                        String city = aMapLocation.getCity();
//                        String district = aMapLocation.getDistrict();
//                        Log.e("onLocationChanged", "city: " + city);
//                        Log.e("onLocationChanged", "district: " + district);
//                        String location = StringUtils.extractLocation(city, district);
//                        mCityAdapter.updateLocateState(LocateState.SUCCESS, location);
//                    } else {
//                        //定位失败
//                        mCityAdapter.updateLocateState(LocateState.FAILED, null);
//                    }
//                }
//            }
//        });
//        mLocationClient.startLocation();
    }

    private void initData() {

        dbManager = new DBManager(this);
        dbManager.copyDBFile();
        mAllCities = dbManager.getAllCities();
        mCityAdapter = new CityListAdapter(this, mAllCities);
        mCityAdapter.setOnCityClickListener(new CityListAdapter.OnCityClickListener() {
            @Override
            public void onCityClick(String name,String pinyin) {
                back(name,pinyin);
            }

            @Override
            public void onLocateClick() {
                Log.e("onLocateClick", "重新定位...");
                mCityAdapter.updateLocateState(LocateState.LOCATING, null);
                mLocClient.start();
            }
        });

        mResultAdapter = new ResultListAdapter(this, null);
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.listview_all_city);
        mListView.setAdapter(mCityAdapter);

        TextView overlay = (TextView) findViewById(R.id.tv_letter_overlay);
        mLetterBar = (SideLetterBar) findViewById(R.id.side_letter_bar);
        mLetterBar.setOverlay(overlay);
        mLetterBar.setOnLetterChangedListener(new SideLetterBar.OnLetterChangedListener() {
            @Override
            public void onLetterChanged(String letter) {
                int position = mCityAdapter.getLetterPosition(letter);
                mListView.setSelection(position);
            }
        });

        searchBox = (EditText) findViewById(R.id.et_search);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString();
                if (TextUtils.isEmpty(keyword)) {
                    clearBtn.setVisibility(View.GONE);
                    emptyView.setVisibility(View.GONE);
                    mResultListView.setVisibility(View.GONE);
                } else {
                    clearBtn.setVisibility(View.VISIBLE);
                    mResultListView.setVisibility(View.VISIBLE);
                    List<City> result = dbManager.searchCity(keyword);
                    if (result == null || result.size() == 0) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        mResultAdapter.changeData(result);
                    }
                }
            }
        });

        emptyView = (ViewGroup) findViewById(R.id.empty_view);
        mResultListView = (ListView) findViewById(R.id.listview_search_result);
        mResultListView.setAdapter(mResultAdapter);
        mResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                back(mResultAdapter.getItem(position).getEnName(),mResultAdapter.getItem(position).getPinyin());
            }
        });

        clearBtn = (ImageView) findViewById(R.id.iv_search_clear);
        backBtn = (ImageView) findViewById(R.id.back);

        clearBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
    }

    private void back(String city,String city_pinyin){
       // ToastUtils.showToast(this, "点击的城市：" + city);
        MainController mainController = MainController.getInstance(this);
        mainController.citychoosed(city,city_pinyin);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_search_clear:
                searchBox.setText("");
                clearBtn.setVisibility(View.GONE);
                emptyView.setVisibility(View.GONE);
                mResultListView.setVisibility(View.GONE);
                break;
            case R.id.back:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocClient.stop();
    }
    
    
    public class BDLocationListenerImpl implements BDLocationListener {  
    	  
        /** 
         * 接收异步返回的定位结果，参数是BDLocation类型参数 
         */  
        @Override  
        public void onReceiveLocation(BDLocation location) {  
        	 Log.e("onLocationChanged", "cannot get location");
            if (location == null) {
            	 mCityAdapter.updateLocateState(LocateState.FAILED, null);
                return;  
            }  
            String city = location.getCity();
            String district = location.getDistrict();
            Log.e("onLocationChanged", "city: " + city);
            Log.e("onLocationChanged", "district: " + district);
            String tlocation = StringUtils.extractLocation(city, district);
            mCityAdapter.updateLocateState(LocateState.SUCCESS, tlocation);
         
           
        }  
  
     
  
    }  
      
  
}
