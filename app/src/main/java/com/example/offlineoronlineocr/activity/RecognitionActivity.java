package com.example.offlineoronlineocr.activity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.offlineoronlineocr.R;
import com.example.offlineoronlineocr.app.MyApplication;
import com.example.offlineoronlineocr.retrofit.NetRequestInterface;
import com.example.offlineoronlineocr.retrofit.RecognitionBean;
import com.example.offlineoronlineocr.utils.FileUtils;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RecognitionActivity
 *
 * @author yuepengfei
 * @date 2019/5/25
 */

public class RecognitionActivity extends AppCompatActivity {
    TessBaseAPI mTessBaseAPI;
    Bitmap recognitionBitmap;
    final String TAG = "RecognitionActivity";
    @BindView(R.id.iv_recognition_img)
    ImageView ivRecognitionImg;
    @BindView(R.id.tv_recognition_result)
    TextView tvRecognitionResult;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    /**
     * 容纳 RaJava dispose 的容器，用于一次性销毁处理
     */
    CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);
        ButterKnife.bind(this);

        mTessBaseAPI = MyApplication.sTessBaseAPI;
        String uri = getIntent().getStringExtra("Uri");
        getRecognitionBitMap(uri);
        String mode = getIntent().getStringExtra("Mode");
        String offline = "offline";
        String online = "online";
        if (offline.equals(mode)) {
            recognizeInOffline(recognitionBitmap);
        } else if (online.equals(mode)) {
            recognizeInOnline(recognitionBitmap);
        }
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * 联网识别:百度API
     *
     * @param recognitionBitmap 识别图像Bitmap
     */
    private void recognizeInOnline(Bitmap recognitionBitmap) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://aip.baidubce.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        Observable<RecognitionBean> observable = retrofit.create(NetRequestInterface.class)
                .getRecognitionResult(MyApplication.token, bitMapToBase64(recognitionBitmap));

        Disposable disposable = observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RecognitionBean>() {
                    @Override
                    public void accept(RecognitionBean recognitionBean) {
                        progressBar.setVisibility(View.GONE);
                        StringBuilder content = new StringBuilder();
                        List<RecognitionBean.WordsResultBean> resultBeans = recognitionBean.getWords_result();
                        for (RecognitionBean.WordsResultBean resultBean : resultBeans) {
                            content.append(resultBean.getWords());
                        }
                        tvRecognitionResult.setText(content);
                    }
                });
        compositeDisposable = new CompositeDisposable(disposable);

    }

    /**
     * 离线识别:tess-two
     *
     * @param recognitionBitmap 识别图像Bitmap
     */
    private void recognizeInOffline(final Bitmap recognitionBitmap) {

        // RxJava 异步识别，随后显示结果
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                mTessBaseAPI.setImage(recognitionBitmap);
                if (mTessBaseAPI.getUTF8Text() != null) {
                    emitter.onNext(mTessBaseAPI.getUTF8Text());
                } else {
                    emitter.onNext("无法识别结果");
                }
            }
        });
        Disposable disposable = observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        progressBar.setVisibility(View.GONE);
                        tvRecognitionResult.setText(s);
                    }
                });
        // 将RxJava的dispose添加进CompositeDisposable，一次性处理
        compositeDisposable = new CompositeDisposable(disposable);

    }

    /**
     * 获取到存入手机的识别图片并显示，随后删除
     *
     * @param uriString 识别图片的Uri
     */
    private void getRecognitionBitMap(String uriString) {
        Uri uri = Uri.parse(uriString);
        try {
            recognitionBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            // 显示识别图像
            ivRecognitionImg.setImageBitmap(recognitionBitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: " + "无法获取到识别图像!");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getContentResolver().delete(uri, null, null);
        } else {
            FileUtils.deleteFile(this, uri);
        }
    }

    private String bitMapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 一次性处理 容器中的 所有dispose
        if (compositeDisposable != null && compositeDisposable.size() > 0) {
            compositeDisposable.dispose();
            compositeDisposable.clear();
        }
    }
}
