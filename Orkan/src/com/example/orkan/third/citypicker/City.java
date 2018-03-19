package com.example.orkan.third.citypicker;

import com.example.orkan.util.Util;

/**
 * author zaaach on 2016/1/26.
 */
public class City {
    private String name;
    private String pinyin;

    public City() {}

    public City(String name, String pinyin) {
        this.name = name;
        this.pinyin = pinyin;
    }

    public String getName() {
    	 if(Util.language == 1) {
        return name;
    	 }else
    	 {
    		 return pinyin;
    	 }
    }
    
    public String getEnName() {
    		return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }
}
