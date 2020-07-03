package com.example.serialcommunicationtest.comn;

import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.example.serialcommunicationtest.activity.PortDetailActivity;
import com.example.serialcommunicationtest.bean.BloodOxygenData;
import com.example.serialcommunicationtest.util.DataProcessingUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * 读串口线程
 */
public class SerialReadThread extends Thread {

    private static final String TAG = "SerialReadThread";

    private ArrayList<String> ecgDataPack = new ArrayList<>();
    private static final int ECG_PACK_SIZE = 8;

    private ArrayList<String> boDataPack = new ArrayList<>();
    private static final int BO_PACK_SIZE = 7;

    private ArrayList<String> theDataPack = new ArrayList<>();

    private BufferedInputStream inputStream;

    private byte[] received = new byte[1];
    private int size;

    public SerialReadThread(InputStream is) {
        inputStream = new BufferedInputStream(is);
    }

    /**
     * ECG数据解析进程
     * */
    private Runnable ecgRunnable = () -> {
        String flag = "EcgRunnable";
        packEcgData(DataProcessingUtils.onDataReceive(received, size, flag));
        if (ecgDataPack.size() == ECG_PACK_SIZE) {
            if (DataProcessingUtils.checkDataPack(ecgDataPack)){
                //每次发送message必须重新构造message对象
                Message msg = Message.obtain();
                msg.obj = DataProcessingUtils.unPackEcgData(ecgDataPack);
                //调用PortDetailActivity中的静态handler
                PortDetailActivity.handler.sendMessage(msg);
            }
            ecgDataPack.clear();
        }
    };

    /**
     * BO数据解析进程
     * */
    private Runnable bloodOxygenRunnable = () -> {
        String flag = "BloodOxygenRunnable";
        packBoData(DataProcessingUtils.onDataReceive(received, size, flag));
        if (boDataPack.size() == BO_PACK_SIZE) {
            BloodOxygenData bloodOxygenData;
            bloodOxygenData = DataProcessingUtils.unPackBoData(boDataPack);
            //可以对bo对象的属性判断并打印错误或正确信息
            Message msg = Message.obtain();
            msg.obj = "脉率：" + bloodOxygenData.getPulseRate() + "，血氧：" + bloodOxygenData.getBloodOxygen();
            PortDetailActivity.handler.sendMessage(msg);
        }
    };

    /**
     * BP数据解析进程
     * */
    private Runnable bloodPressureRunnable = () -> {

    };

    /**
     * 体温枪数据解析进程
     * */
    private Runnable thermometerRunnable = () -> {
        String flag = "ThermometerRunnable";
        String hexStr = DataProcessingUtils.onDataReceive(received, size, flag);
        //判断打包是否结束
        if(packTheData(hexStr)){
            //每次发送message必须重新构造message对象
            Message msg = Message.obtain();
            msg.obj = DataProcessingUtils.unPackTheData(theDataPack);
            //调用PortDetailActivity中的静态handler
            PortDetailActivity.handler.sendMessage(msg);
            theDataPack.clear();
        }
    };

    Thread ecgThread = new Thread(ecgRunnable);
    Thread theThread = new Thread(thermometerRunnable);
    Thread boThread = new Thread(bloodOxygenRunnable);
    Thread bpThread = new Thread(bloodPressureRunnable);

    @Override
    public void run() {

        Log.e(TAG,"开始读线程");
//        ecgThread.start();
//        theThread.start();
//        boThread.start();
        while (!Thread.currentThread().isInterrupted()) {

            try {
                int available = inputStream.available();

                if (available > 0) {
                    size = inputStream.read(received);

                    if (size > 0) {
                        bpThread.run();
//                        ecgThread.run();
//                        theThread.run();
//                        boThread.run();
                    }
                } else {
                    // 暂停时间，防止循环导致CPU占用率过高
                    SystemClock.sleep(1);
                }

            } catch (IOException e) {
                Log.e(TAG,"读取数据失败", e);
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
     * 打包ecg数据包
     * 打包是否完成由list.size决定
     *
     * @param hexStr -可能的数据包元素
     * */
    private void packEcgData(String hexStr) {
        if (hexStr.equals("05") && ecgDataPack.isEmpty()) {
            ecgDataPack.add(hexStr);
        } else if (ecgDataPack.size() >= 1 && ecgDataPack.size() <= ECG_PACK_SIZE) {
            ecgDataPack.add(hexStr);
        }
    }

    /**
     * 打包bo数据包
     * 打包是否完成由list.size决定
     *
     * @param hexStr -可能的数据包元素
     * */
    private void packBoData(String hexStr) {
        if (hexStr.equals("17") && boDataPack.isEmpty()) {
            boDataPack.add(hexStr);
        } else if (boDataPack.size() >= 1 && boDataPack.size() <= BO_PACK_SIZE) {
            boDataPack.add(hexStr);
        }
    }

    /**
     * 打包the数据包
     * 打包是否完成由返回的布尔值决定
     *
     * @param hexStr -可能的数据包元素
     * */
    private Boolean packTheData(String hexStr) {

        theDataPack.add(hexStr);

        if (theDataPack.get(theDataPack.size()-1).equals("0A")) {
            // 去掉末尾结束符, 和休眠时的值
            theDataPack.remove("0A");
            theDataPack.remove("0D");
            //由于设备会传回多个00，因此需要循环剔除00
            while (theDataPack.remove("00")) Log.e(TAG, "剔除00");

            // list长度为3, 数据包保存的是错误信息，打包结束
            if (theDataPack.size() > 3) {
                // 移除包括引号在内的文字前缀，只保留数据
                while (!theDataPack.get(0).equals("3A")) {
                    theDataPack.remove(0);
                    if (theDataPack.isEmpty()) break;
                }
                theDataPack.remove("3A");
            }
            //list为空则说明数据包不符合解析规则
            return !theDataPack.isEmpty();
        }

        return false;
    }


}
