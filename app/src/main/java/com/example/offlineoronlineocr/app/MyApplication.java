package com.example.offlineoronlineocr.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.example.offlineoronlineocr.utils.FileUtils;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

/**
 * MyApplication class
 *
 * @author yuepengfei
 * @date 2019/5/25
 */
public class MyApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context sContext;
    public static TessBaseAPI sTessBaseAPI;
    public static String token;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();

    }

    public static void setInit(Context context) {
        OCR.getInstance(context).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                // 调用成功，返回AccessToken对象
                token = result.getAccessToken();
                Log.d("MyApplication", "onResult: " + "调用百度文字识别API成功" + token);
            }

            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError子类SDKError对象
                Log.d("MyApplication", "onResult: " + "调用百度文字识别API失败");
            }
        }, context);

        sTessBaseAPI = new TessBaseAPI();
        // 字体库可通过 "+" 进行合并
        String lang = "chi_sim+ypf+eng";
        // tesseract 指定设别的路径
        String dataPath = context.getFilesDir() + "/tesseract";
        // 字体库父文件的路径
        String filePath = dataPath + "/tessdata";

        if (FileUtils.checkFile(new File(filePath), dataPath, lang, context)) {
            sTessBaseAPI.init(dataPath, lang);
        }
    }
}
