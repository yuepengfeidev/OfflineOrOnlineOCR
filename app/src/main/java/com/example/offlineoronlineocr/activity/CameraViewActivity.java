package com.example.offlineoronlineocr.activity;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.example.offlineoronlineocr.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * CameraViewActivity class
 *
 * @author yuepengfei
 * @date 2019/5/25
 */

public class CameraViewActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener {

    @BindView(R.id.jcv_camera)
    JavaCameraView mOpenCVCamera;
    @BindView(R.id.sf_surface1)
    SurfaceView sfSurface1;
    @BindView(R.id.sf_surface2)
    SurfaceView sfSurface2;

    /**
     * 识别模式：offline \ online
     */
    String mode;

    final String TAG = "CameraViewActivity";

    Rect mRect;
    /**
     * 蓝色选取框的矩阵
     */
    Mat mMat;
    /**
     * 获取图片的矩阵
     */
    Mat imgInput;
    /**
     * 蓝色选区框的 宽高
     */
    int mHeight;
    int mWidth;
    /**
     * 蓝色选区框的位置
     */
    int mY;
    int mX;
    /**
     * 蓝色选区框 宽高 对 获得照片的占比
     */
    double mWScale = (double) 1 / 4;
    double mHScale = (double) 3 / 4;

    RelativeLayout.LayoutParams mLayoutParams;


    private BaseLoaderCallback mBaseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    mOpenCVCamera.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View view = getWindow().getDecorView();
        // 在照相界面下，保持屏幕长亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 设置沉静式全屏，隐藏任务栏和导航栏
        int uiOptions =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        view.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_camera_view);
        super.onCreate(savedInstanceState);

        mode = getIntent().getStringExtra("Mode");

        ButterKnife.bind(this);
        onViewClicked(view);

        initView();
    }

    /**
     * 初始化 Camera
     */
    private void initView() {
        mOpenCVCamera.setVisibility(SurfaceView.VISIBLE);
        // front-camera(1),  back-camera(0)
        mOpenCVCamera.setCameraIndex(0);
        mOpenCVCamera.setCvCameraViewListener(this);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        // 蓝色选区框的宽高
        mHeight = (int) (width * mWScale);
        mWidth = (int) (height * mHScale);

        // 蓝色选取框 位于 获取照片 的中心位置
        mY = (width - mHeight) / 2;
        mX = (height - mWidth) / 2;

        mLayoutParams = new RelativeLayout.LayoutParams(mWidth + 5, mHeight + 5);
        mLayoutParams.setMargins(mX, mY, 0, 0);
        sfSurface1.setLayoutParams(mLayoutParams);

        mLayoutParams = new RelativeLayout.LayoutParams(mWidth - 5, mHeight - 5);
        mLayoutParams.setMargins(mX + 5, mY + 5, 0, 0);
        sfSurface2.setLayoutParams(mLayoutParams);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        imgInput = inputFrame;
        return imgInput;
    }

    @OnClick({R.id.iv_exit, R.id.bt_take_photo})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_exit:
                mOpenCVCamera.disableView();
                finish();
                overridePendingTransition(0, R.anim.out);
                break;
            case R.id.bt_take_photo:
                processImage();
                // 创建识别图像大小 的Bitmap
                Bitmap recognitionBitmap = Bitmap.createBitmap(mMat.cols(), mMat.rows(), Bitmap.Config.ARGB_8888);
                // 将识别图像的矩阵Mat 转化为 其bitmap
                Utils.matToBitmap(mMat, recognitionBitmap);
                // 将识别图像bitmap转换为Uri，存入手机
                Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), recognitionBitmap, null, null));
                Intent intent = new Intent(this, RecognitionActivity.class);
                // 通过uri传递图片
                intent.putExtra("Uri", uri.toString());
                intent.putExtra("Mode", mode);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * 对获取的图像进行处理,使得容易识别
     */
    private void processImage() {
        mRect = new Rect(mX, mY, mWidth, mHeight);
        // 从整张图中截取选m区部分
        mMat = imgInput.submat(mRect);

        // 灰度化处理
        Imgproc.cvtColor(mMat, mMat, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.cvtColor(mMat, mMat, Imgproc.COLOR_GRAY2RGBA);

       /* // 二值化处理
        Imgproc.adaptiveThreshold(mMat, mMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY_INV, 3, 10);*/

        // 通过腐蚀和膨胀操作去除噪声
        //腐蚀膨胀计算的核心
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        // 腐蚀化处理
        Imgproc.erode(mMat, mMat, kernel);
        // 膨胀化处理
        Imgproc.dilate(mMat, mMat, kernel);
        mMat.copyTo(imgInput.submat(mRect));

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mBaseLoaderCallback);
        } else {
            Log.d(TAG, "onResume :: OpenCV library found inside package. Using it!");
            mBaseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        View view = getWindow().getDecorView();
        // 在照相界面下，保持屏幕长亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 设置沉静式全屏，隐藏任务栏和导航栏
        int uiOptions =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        view.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCVCamera != null && mOpenCVCamera.isEnabled()) {
            mOpenCVCamera.disableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCVCamera != null && mOpenCVCamera.isEnabled()) {
            mOpenCVCamera.disableView();
        }
    }
}
