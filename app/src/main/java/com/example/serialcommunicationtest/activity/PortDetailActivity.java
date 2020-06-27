package com.example.serialcommunicationtest.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.serialcommunicationtest.R;
import com.example.serialcommunicationtest.adapter.MsgListAdapter;
import com.example.serialcommunicationtest.bean.Device;
import com.example.serialcommunicationtest.comn.SerialPortUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import android_serialport_api.SerialPort;

public class PortDetailActivity extends AppCompatActivity {

    private int backPressedTime = 0; //返回键触发次数
    private long mExitTime = System.currentTimeMillis();

    public static Handler handler = new Handler();

    private String devicePath;
    private boolean isOpened = false;

    private Device device;
    private List<String> recyclerContents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_port_detail);

        Intent intent = getIntent();
        devicePath = intent.getStringExtra("devicePath");

        initView();
        FloatingActionButton buttonStar = findViewById(R.id.bt_port_switch);
        buttonStar.setOnClickListener(v -> switchSerialPort());

    }

    /**
     * 界面初始化
     * */
    private void initView() {

        Resources res =getResources();
        Spinner spinner = findViewById(R.id.sp_bt);
        ArrayAdapter<String> adapter= new ArrayAdapter<>(this, R.layout.spinner_item, res.getStringArray(R.array.baudrates_value));
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        TextView textView = findViewById(R.id.tv_port);
        String devicePathText = getResources().getString(R.string.device_path_text);
        String portStr = String.format(devicePathText, devicePath);
        textView.setText(portStr);
        updateViewState(isOpened);
    }

    /**
     * 打开&关闭串口
     */
    private void switchSerialPort() {

        initDevice();

        if (isOpened){
            SerialPortUtils.instance().close();
            isOpened = false;
            initMsgList("串口已关闭");
            Toast.makeText(this, "串口已关闭", Toast.LENGTH_LONG).show();
            //updateViewState(isOpened);
        } else {

            SerialPort serialPort = SerialPortUtils.instance().open(device);
            //isOpened的值是{serialPort != null}的判断结果
            isOpened = serialPort != null;
            if (isOpened) {
                Toast.makeText(this, "成功打开串口", Toast.LENGTH_SHORT).show();
                recyclerContents = new ArrayList<>();
                initMsgList("成功打开串口");
                getMessage();

                //updateViewState(isOpened);
            } else {
                SerialPortUtils.instance().close();
                Toast.makeText(this, "打开串口失败", Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     * 初始化设备
     */
    private void initDevice() {

        Spinner spinner = findViewById(R.id.sp_bt);
        String baudrate = spinner.getSelectedItem().toString();

        device = new Device(devicePath, baudrate);
    }

    /**
     * 收取信息
     *
     */
    private void getMessage() {

        handler = new Handler(){
            // 通过复写handlerMessage()从而确定更新UI的操作
            @Override
            public void handleMessage(@NonNull Message msg) {
                //已屏蔽2047 2047
                String resStr = msg.obj.toString();
                if (!"2047 2047 ".equals(resStr)){
                    initMsgList(resStr);
                }
            }
        };

    }
    // TODO: 2020/6/19 解决内存泄露
    /*public static class SaveHandler extends Handler {
        WeakReference<Activity> mActivityReference;
        SaveHandler(Activity activity) {
            mActivityReference= new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            PortDetailActivity portDetailActivity = new PortDetailActivity();
            final Activity activity = mActivityReference.get();
            if (activity != null) {
                portDetailActivity.initMsgList(msg.obj.toString());
            }
        }
    }*/

    /**
     * 显示信息
     *
     * @param content -获得的内容
     * todo 注意修改函数
     */
    private void initMsgList(String content) {
        recyclerContents.add(content);
        RecyclerView recyclerView = findViewById(R.id.msg_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); //设置布局管理器
        recyclerView.setAdapter(new MsgListAdapter(this,recyclerContents));    //设置Adapter
        recyclerView.smoothScrollToPosition(recyclerContents.size()-1);
    }

    /**
     * 更新按钮
     *
     * @param isSerialPortOpened -串口开关状态
     */
    private void updateViewState(boolean isSerialPortOpened) {

        FloatingActionButton button = findViewById(R.id.bt_port_switch);
        //TODO:状态改变动画
        button.setVisibility(isSerialPortOpened ? View.INVISIBLE : View.VISIBLE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * 双击确认退出activity并关闭串口
     * */
    @Override
    public void onBackPressed() {
        //判断串口是否开启
        if (isOpened){
            if (System.currentTimeMillis() - mExitTime > 2000 || backPressedTime == 0) {
                //第一次点击
                RecyclerView dataList = findViewById(R.id.msg_list);
                Snackbar snackbar = Snackbar.make(dataList, "再按一次返回，退出当前页面并关闭串口", Snackbar.LENGTH_LONG);
                snackbar.setActionTextColor(Color.WHITE);
                snackbar.show();

                mExitTime = System.currentTimeMillis();
                backPressedTime++;
            } else {
                //第二次点击
                SerialPortUtils.instance().close();
                isOpened = false;

                backPressedTime = 0;
                super.onBackPressed();
            }
        } else super.onBackPressed();

    }

}
