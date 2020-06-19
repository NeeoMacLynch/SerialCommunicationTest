package com.example.ecgtest.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecgtest.R;
import com.example.ecgtest.adapter.PortListAdapter;
import com.example.ecgtest.bean.Device;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android_serialport_api.SerialPortFinder;

public class MainActivity extends AppCompatActivity {

    private List<String> devicesPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();
    }

    /**
     * 初始化RecyclerView中的数据
     * */
    private void initData() {
        SerialPortFinder serialPortFinder = new SerialPortFinder();

        // 设备
        devicesPath = Arrays.asList(serialPortFinder.getAllDevicesPath());
        if (devicesPath.size() == 0) {
            devicesPath.add(getString(R.string.no_serial_device));
        }
    }

    /**
     * 初始化RecyclerView
     * */
    private void initView() {
        RecyclerView recyclerView = findViewById(R.id.port_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); //设置布局管理器
        recyclerView.setAdapter(new PortListAdapter(this,devicesPath));    //设置Adapter
    }

}
