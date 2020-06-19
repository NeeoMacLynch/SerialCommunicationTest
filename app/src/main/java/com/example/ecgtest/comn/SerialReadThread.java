package com.example.ecgtest.comn;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.example.ecgtest.activity.PortDetailActivity;
import com.example.ecgtest.util.ByteUtils;
import com.example.ecgtest.util.DataUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 读串口线程
 */
public class SerialReadThread extends Thread {

    private static final String TAG = "SerialReadThread";

    private ArrayList<String> dataPack = new ArrayList<>();
    private static final int PACK_SIZE = 8;

    public static Handler handler = new Handler();
    private BufferedInputStream inputStream;

    public SerialReadThread(InputStream is) {
        inputStream = new BufferedInputStream(is);
    }

    @Override
    public void run() {
        byte[] received = new byte[1];
        int size;
        String hexStr;

        Log.e(TAG,"开始读线程");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                int available = inputStream.available();

                if (available > 0) {
                    size = inputStream.read(received);
                    if (size > 0) {

                        hexStr = onDataReceive(received, size);

                        String finalHexStr = hexStr;

                        //新线程内进行数据解析
                        Runnable runnable = () -> {
                            packData(finalHexStr);
                            if (dataPack.size() == PACK_SIZE) {
                                if (DataUtils.checkDataPack(dataPack)){
                                    //每次发送message必须重新构造message对象
                                    Message msg = Message.obtain();
                                    msg.obj = DataUtils.unPackData(dataPack);
                                    //调用PortDetailActivity中的静态handler
                                    PortDetailActivity.handler.sendMessage(msg);
                                }
                                dataPack.clear();
                            }
                        };
                        runnable.run();

                    }
                } else {
                    // 暂停时间，防止循环导致CPU占用率过高
                    SystemClock.sleep(1);
                }
            } catch (IOException e) {
                Log.e(TAG,"读取数据失败", e);

                Message msg = Message.obtain();
                msg.obj = "读取数据失败" + e; // 消息内存存放
                //调用PortDetailActivity中的静态handler
                PortDetailActivity.handler.sendMessage(msg);
            }

        }

        Log.d(TAG,"结束读进程");
    }

    /**
     * 停止读线程
     */
    public void close() {

        try {
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG,"异常:", e);
        } finally {
            super.interrupt();
        }
    }

    /**
     * 处理获取到的数据
     *
     * @param received -被处理数据
     * @param size -数组长度
     */
    private String onDataReceive(byte[] received, int size) {

        String hexStr = ByteUtils.bytes2HexStr(received, 0 , size);
        String msgStr = "接收数据：" + hexStr;

        Log.d(TAG, msgStr);
        return hexStr;
    }

    /**
     * 打包数据包
     *
     * @param hexStr -可能的数据包元素
     * */
    private void packData(String hexStr) {
        if (hexStr.equals("05") && dataPack.size() == 0) {
            dataPack.add(hexStr);
        } else if (dataPack.size() >= 1 && dataPack.size() <= PACK_SIZE) {
            dataPack.add(hexStr);
        }
    }

}