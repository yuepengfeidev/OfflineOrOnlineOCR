package com.example.offlineoronlineocr.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.offlineoronlineocr.R;
import com.example.offlineoronlineocr.app.MyApplication;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.offlineoronlineocr.utils.PermissionsUtils.PERMISSION_REQUEST_CODE;
import static com.example.offlineoronlineocr.utils.PermissionsUtils.hasPermissions;
import static com.example.offlineoronlineocr.utils.PermissionsUtils.permissions;
import static com.example.offlineoronlineocr.utils.PermissionsUtils.requestNecessaryPermissions;


/**
 * MainActivity class
 *
 * @author yuepengfei
 * @date 2019/5/25
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 没有权限则获取权限
        if (!hasPermissions(permissions)) {
            requestNecessaryPermissions(this, permissions);
        } else {
            mHandler.sendEmptyMessage(0);
        }

        ButterKnife.bind(this);
        onViewClicked(getWindow().getDecorView());


    }

    @OnClick({R.id.bt_offline_recognition, R.id.bt_online_recognition})
    public void onViewClicked(View view) {
        Intent intent = new Intent(this, CameraViewActivity.class);
        switch (view.getId()) {
            case R.id.bt_offline_recognition:
                intent.putExtra("Mode", "offline");
                startActivity(intent);
                break;
            case R.id.bt_online_recognition:
                intent.putExtra("Mode", "online");
                startActivity(intent);
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && !hasPermissions(permissions)) {
            Toast.makeText(getApplicationContext(), "请确定打开所有权限", Toast.LENGTH_LONG).show();
            finish();
        } else {
            mHandler.sendEmptyMessage(0);
        }
    }

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            MyApplication.setInit(MainActivity.this);
            return false;
        }
    });
}
