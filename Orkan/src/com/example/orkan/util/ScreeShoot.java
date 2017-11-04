package com.example.orkan.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.View;

public class ScreeShoot {
	  
    // 获取指定Activity的截屏，保存到png文件  
    private static Bitmap takeScreenShot(Activity activity) {  
        // View是你需要截图的View  
        View view = activity.getWindow().getDecorView();  
        view.setDrawingCacheEnabled(true);  
        view.buildDrawingCache();  
        Bitmap b1 = view.getDrawingCache();  
        return b1;  
    }  
  
    // 保存到sdcard  
    private static void savePic(Bitmap b, String strFileName) {  
          
        FileOutputStream fos = null;  
        try {  
            fos = new FileOutputStream(strFileName); 
            if (null != fos) {  
                b.compress(Bitmap.CompressFormat.PNG, 10, fos); 
                fos.flush();  
                fos.close();  
            }  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
  
    // 程序入口  
    public static String shoot(Activity a) {  
        String strFileName = a.getApplicationContext().getFilesDir().getAbsolutePath() + String.valueOf(System.currentTimeMillis()) + ".png"; 
        String fileName = Environment.getExternalStorageDirectory() + "/" + "orkan/" +  String.valueOf(System.currentTimeMillis()) + ".png";
        String path = Environment.getExternalStorageDirectory() + "/" + "orkan/";
        File dirFirstFolder = new File(path);//方法二：通过变量文件来获取需要创建的文件夹名字  
        if(!dirFirstFolder.exists())  
        { //如果该文件夹不存在，则进行创建  
          dirFirstFolder.mkdirs();//创建文件夹  
        }  
        ScreeShoot.savePic(ScreeShoot.takeScreenShot(a), fileName);  
        return fileName;  
    }  
}
