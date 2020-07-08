package com.example.serialcommunicationtest.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.serialcommunicationtest.R;
import com.example.serialcommunicationtest.adapter.MsgListAdapter;
import com.example.serialcommunicationtest.bean.Command;
import com.example.serialcommunicationtest.bean.Device;
import com.example.serialcommunicationtest.comn.SerialPortManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android_serialport_api.SerialPort;

public class PortDetailActivity extends AppCompatActivity {

    private int backPressedTime = 0; //返回键触发次数
    private long mExitTime = System.currentTimeMillis();

    public static Handler handler = new Handler();
    public static Handler workHandler = new Handler();

    private String devicePath;
    private boolean isSerialPortOpened = false;

    private Device device;
    private List<String> recyclerContents = new ArrayList<>();

    private boolean isBpDeviceOpened = false;

    //按钮监听器
    class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.port_switch:
                    switchSerialPort();
                    break;
                case R.id.start_bp_switch:
                    startBloodPressureDevice();
                    break;
                case R.id.stop_bp_switch:
                    stopBloodPressureDevice();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_port_detail);

        Intent intent = getIntent();
        devicePath = intent.getStringExtra("devicePath");

        initView();
        FloatingActionButton port_switch = findViewById(R.id.port_switch);
        Button start_bp_switch = findViewById(R.id.start_bp_switch);
        Button stop_bp_switch = findViewById(R.id.stop_bp_switch);

        port_switch.setOnClickListener(new ButtonClickListener());
        start_bp_switch.setOnClickListener(new ButtonClickListener());
        stop_bp_switch.setOnClickListener(new ButtonClickListener());

    }

    /**
     * 界面初始化
     * */
    private void initView() {

        Resources res =getResources();
        Spinner spinner = findViewById(R.id.sp_bt);
        ArrayAdapter<String> adapter= new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                res.getStringArray(R.array.baudrates_value)
        );
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        TextView textView = findViewById(R.id.tv_port);
        String devicePathText = getResources().getString(R.string.device_path_text);
        String portStr = String.format(devicePathText, devicePath);
        textView.setText(portStr);
        updateViewState(isSerialPortOpened);
    }

    /**
     * 打开&关闭串口
     */
    private void switchSerialPort() {

        initDevice();

        if (isSerialPortOpened){
            SerialPortManager.instance().close();
            isSerialPortOpened = false;
            initMsgList("串口已关闭");
            Toast.makeText(this, "串口已关闭", Toast.LENGTH_LONG).show();
            //updateViewState(isOpened);
        } else {

            SerialPort serialPort = SerialPortManager.instance().open(device);
            //isOpened的值是{serialPort != null}的判断结果
            isSerialPortOpened = serialPort != null;
            if (isSerialPortOpened) {
                Toast.makeText(this, "成功打开串口", Toast.LENGTH_SHORT).show();
                recyclerContents = new ArrayList<>();
                initMsgList("成功打开串口");
                getMessage();

                //updateViewState(isOpened);
            } else {
                SerialPortManager.instance().close();
                Toast.makeText(this, "打开串口失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static final String[] CMD_NBP_PERIOD_MODE_LIST = {
            "00",   //设置为手动方式
            "01",   //设置自动测量周期为1分钟
            "02",   //设置自动测量周期为2分钟
            "03",   //设置自动测量周期为3分钟
            "04",   //设置自动测量周期为4分钟
            "05",   //设置自动测量周期为5分钟
            "06",   //设置自动测量周期为10分钟
            "07",   //设置自动测量周期为15分钟
            "08",   //设置自动测量周期为30分钟
            "09",   //设置自动测量周期为60分钟
            "0A",   //设置自动测量周期为90分钟
            "0B",   //设置自动测量周期为120分钟
            "0C",   //设置自动测量周期为180分钟
            "0D",   //设置自动测量周期为240分钟
            "0E"    //设置自动测量周期为480分钟
    };

    private static final Map<String, Command> CMD_NBP = new HashMap<String, Command>(){{
        put("CMD_NBP_START", new Command("55D5")); //启动命令包
        put("CMD_NBP_END", new Command("56D6")); //中止测量命令包
        put("CMD_NBP_PERIOD", new Command("57", CMD_NBP_PERIOD_MODE_LIST)); //测量周期设置命令包
        put("CMD_NBP_CAL", new Command("58")); //测量校准命令包
        put("CMD_NBP_RESET", new Command("59D9")); //测量复位命令包
        put("CMD_NBP_PNEUMATIC", new Command("5A")); //漏气检测命令包
        put("CMD_NBP_STATE", new Command("5B")); //状态查询命令包
        put("CMD_NBP_INF", new Command("5D")); //充气命令包
        put("CMD_NBP_GET_RESULT", new Command("5E")); //查询结果命令包
    }};


    private void startBloodPressureDevice() {
        if (isSerialPortOpened) {
            final boolean[] isAckPackNotBack = {true};
            //发送0x59复位命令，有应答
            SerialPortManager.instance().sendCommand(Objects.requireNonNull(CMD_NBP.get("CMD_NBP_RESET")).getCommand());

            HandlerThread bpStartThread = new HandlerThread("START_BP");
            bpStartThread.start();
            workHandler = new Handler(bpStartThread.getLooper() ) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    if (1 == msg.what) {
                        if ("设备应答：命令成功".equals(msg.obj.toString()))
                            if (isAckPackNotBack[0]) {
                                //发送0x55启动命令，有应答
                                SerialPortManager.instance().sendCommand(Objects.requireNonNull(CMD_NBP.get("CMD_NBP_START")).getCommand());
                                isAckPackNotBack[0] = false;
                            }

                    }
                }
            };

            isBpDeviceOpened = true;
        } else {
            Toast.makeText(this, "串口未打开", Toast.LENGTH_SHORT).show();
        }

    }

    private void stopBloodPressureDevice() {
        final boolean[] isAckPackNotBack = {true};
        if (isBpDeviceOpened) {
            //发送0x56测量中止命令，有应答
            SerialPortManager.instance().sendCommand(Objects.requireNonNull(CMD_NBP.get("CMD_NBP_END")).getCommand());
            HandlerThread bpStopThread = new HandlerThread("STOP_BP");
            bpStopThread.start();
            workHandler = new Handler(bpStopThread.getLooper() ) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    if (1 == msg.what) {
                        if ("设备应答：命令成功".equals(msg.obj.toString()))
                            if (isAckPackNotBack[0]) {
                                //发送0x59复位命令，有应答
                                SerialPortManager.instance().sendCommand(Objects.requireNonNull(CMD_NBP.get("CMD_NBP_RESET")).getCommand());
                                isAckPackNotBack[0] = false;
                            }

                    }
                }
            };

            isBpDeviceOpened = false;
        } else {
            Toast.makeText(this, "设备未开始工作", Toast.LENGTH_SHORT).show();
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
     * define
     */
    private void getMessage() {

        handler = new Handler(){
            // 通过复写handlerMessage()从而确定更新UI的操作
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (0 == msg.what) {
                    //已屏蔽重复初始值
                    String resStr = msg.obj.toString();
                    if ("非新生儿模式下检测到新生儿袖带".equals(resStr)){
                        //发送0x56测量终止命令
                        SerialPortManager.instance().sendCommand(Objects.requireNonNull(CMD_NBP.get("CMD_NBP_END")).getCommand());
                    }
                    if (!"2047 2047 ".equals(resStr) && !"脉率：0，血氧：0".equals(resStr)){
                        initMsgList(resStr);
                    }
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
     */
    private void initMsgList(String content) {
        recyclerContents.add(content);
        RecyclerView recyclerView = findViewById(R.id.msg_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); //设置布局管理器
        recyclerView.setAdapter(new MsgListAdapter(this,recyclerContents));    //设置Adapter
        //recyclerView.smoothScrollToPosition(recyclerContents.size()-1);

    }

    /**
     * 更新按钮
     *
     * @param isSerialPortOpened -串口开关状态
     */
    private void updateViewState(boolean isSerialPortOpened) {

        FloatingActionButton button = findViewById(R.id.port_switch);
        //TODO:状态改变动画
        button.setVisibility(isSerialPortOpened ? View.INVISIBLE : View.VISIBLE);

    }

    /**
     * activity结束时清理handler message
     * */
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
        if (isSerialPortOpened){
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
                SerialPortManager.instance().close();
                isSerialPortOpened = false;

                backPressedTime = 0;
                super.onBackPressed();
            }
        } else super.onBackPressed();

    }

}
