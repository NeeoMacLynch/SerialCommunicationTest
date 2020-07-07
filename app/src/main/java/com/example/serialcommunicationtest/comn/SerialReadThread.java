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

    private ArrayList<String> bpAckDataPack = new ArrayList<>();
    private static final int BP_ACK_PACK_SIZE = 5;
    private ArrayList<String> bpStsDataPack = new ArrayList<>();
    private static final int BP_STS_PACK_SIZE = 8;
    private ArrayList<String> bpCufPreDataPack = new ArrayList<>();
    private static final int BP_CUF_PRE_PACK_SIZE = 7;
    private ArrayList<String> bpEndDataPack = new ArrayList<>();
    private static final int BP_END_PACK_SIZE = 4;
    private ArrayList<String> bpResult1DataPack = new ArrayList<>();
    private static final int BP_RESULT1_PACK_SIZE = 9;
    private ArrayList<String> bpResult2DataPack = new ArrayList<>();
    private static final int BP_RESULT2_PACK_SIZE = 5;

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
                msg.what = 0; //打印标志
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
            if (DataProcessingUtils.checkDataPack(boDataPack)) {
                BloodOxygenData bloodOxygenData;
                bloodOxygenData = DataProcessingUtils.unPackBoData(boDataPack);
                //可以对bo对象的属性判断并打印错误或正确信息
                Message msg = Message.obtain();
                msg.what = 0; //打印标志
                msg.obj = "脉率：" + bloodOxygenData.getPulseRate() + "，血氧：" + bloodOxygenData.getBloodOxygen();
                PortDetailActivity.handler.sendMessage(msg);
            }
            boDataPack.clear();
        }
    };

    /**
     * BP数据解析进程
     * */
    private Runnable bloodPressureRunnable = () -> {
        String flag = "BloodPressureRunnable";
        packBpData(DataProcessingUtils.onDataReceive(received, size, flag));
        //应答包解析
        if (BP_ACK_PACK_SIZE == bpAckDataPack.size()) {
            //Log.d(TAG, flag + " 应答包打包完成");
            if (DataProcessingUtils.checkDataPack(bpAckDataPack)) {
                //Log.d(TAG, flag + " 应答包解析通过");
                String ackMessage;
                ackMessage = DataProcessingUtils.unPackBpData(bpAckDataPack);
                Message msg = Message.obtain();
                msg.what = 1; //应答包标志
                msg.obj = "设备应答：" + ackMessage;
                Log.d(TAG, flag + msg.obj.toString());
                PortDetailActivity.workHandler.sendMessage(msg);
            }
            bpAckDataPack.clear();
        }

        //实时数据包解析
        if (BP_CUF_PRE_PACK_SIZE == bpCufPreDataPack.size()) {
            //Log.d(TAG, flag + " 实时数据包打包完成");
            if (DataProcessingUtils.checkDataPack(bpCufPreDataPack)) {
                //Log.d(TAG, flag + " 实时数据包解析通过");
                String ackMessage;
                ackMessage = DataProcessingUtils.unPackBpData(bpCufPreDataPack);
                Message msg = Message.obtain();
                msg.what = 0; //打印标志
                if ("非新生儿模式下检测到新生儿袖带".equals(ackMessage)) {
                    msg.obj = ackMessage;
                } else {
                    msg.obj = "实时血压记录：" + ackMessage;
                }
                PortDetailActivity.handler.sendMessage(msg);
            }
            bpCufPreDataPack.clear();
        }

        //结束包解析
        if (BP_END_PACK_SIZE == bpEndDataPack.size()) {
            //Log.d(TAG, flag + " 结束包打包完成");
            if (DataProcessingUtils.checkDataPack(bpEndDataPack)) {
                //Log.d(TAG, flag + " 结束包解析通过");
                String ackMessage;
                ackMessage = DataProcessingUtils.unPackBpData(bpEndDataPack);
                Message msg = Message.obtain();
                msg.what = 0; //打印标志
                msg.obj = "测量结束：" + ackMessage;
                PortDetailActivity.handler.sendMessage(msg);
            }
            bpEndDataPack.clear();
        }

        //结果包1解析
        if (BP_RESULT1_PACK_SIZE == bpResult1DataPack.size()) {
            //Log.d(TAG, flag + " 结果包1打包完成");
            if (DataProcessingUtils.checkDataPack(bpResult1DataPack)) {
                //Log.d(TAG, flag + " 结果包1解析通过");
                String ackMessage;
                ackMessage = DataProcessingUtils.unPackBpData(bpResult1DataPack);
                StringBuilder stringBuilder = new StringBuilder();
                for (String item : bpResult1DataPack) {
                    stringBuilder.append(item);
                    stringBuilder.append(" ");
                }
                ackMessage += "\n" + stringBuilder.toString();
                Message msg = Message.obtain();
                msg.what = 0; //打印标志
                msg.obj = ackMessage;
                PortDetailActivity.handler.sendMessage(msg);
            }
            bpResult1DataPack.clear();
        }

        //结果包2解析
        if (BP_RESULT2_PACK_SIZE == bpResult2DataPack.size()) {
            //Log.d(TAG, flag + " 结果包1打包完成");
            if (DataProcessingUtils.checkDataPack(bpResult2DataPack)) {
                //Log.d(TAG, flag + " 结果包1解析通过");
                String ackMessage;
                ackMessage = DataProcessingUtils.unPackBpData(bpResult2DataPack);
                Message msg = Message.obtain();
                msg.what = 0; //打印标志
                msg.obj = ackMessage;
                PortDetailActivity.handler.sendMessage(msg);
            }
            bpResult2DataPack.clear();
        }

        //状态包解析
        if (BP_STS_PACK_SIZE == bpStsDataPack.size()) {
            //Log.d(TAG, flag + " 状态包打包完成");
            if (DataProcessingUtils.checkDataPack(bpStsDataPack)) {
                //Log.d(TAG, flag + " 状态包解析通过");
                String ackMessage;
                ackMessage = DataProcessingUtils.unPackBpData(bpStsDataPack);
                Message msg = Message.obtain();
                msg.what = 0; //打印标志
                msg.obj = "设备状态：" + ackMessage;
                PortDetailActivity.handler.sendMessage(msg);
            }
            bpStsDataPack.clear();
        }

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
            msg.what = 0; //打印标志
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
     * 打包bp数据包
     * 打包是否完成由list.size决定
     *
     * @param hexStr -可能的数据包元素
     * */
    private void packBpData(String hexStr) {
        //应答包判断
        if (hexStr.equals("04") && bpAckDataPack.isEmpty()) {
            bpAckDataPack.add(hexStr);
        } else if (bpAckDataPack.size() >= 1 && bpAckDataPack.size() <= BP_ACK_PACK_SIZE) {
            bpAckDataPack.add(hexStr);
        }

        //实时数据包判断
        if (hexStr.equals("20") && bpCufPreDataPack.isEmpty()) {
            bpCufPreDataPack.add(hexStr);
        } else if (bpCufPreDataPack.size() >= 1 && bpCufPreDataPack.size() <= BP_CUF_PRE_PACK_SIZE) {
            bpCufPreDataPack.add(hexStr);
        }

        //结束包判断
        if (hexStr.equals("21") && bpEndDataPack.isEmpty()) {
            bpEndDataPack.add(hexStr);
        } else if (bpEndDataPack.size() >= 1 && bpEndDataPack.size() <= BP_END_PACK_SIZE) {
            bpEndDataPack.add(hexStr);
        }

        //结果包1判断
        if (hexStr.equals("22") && bpResult1DataPack.isEmpty()) {
            bpResult1DataPack.add(hexStr);
        } else if (bpResult1DataPack.size() >= 1 && bpResult1DataPack.size() <= BP_RESULT1_PACK_SIZE) {
            bpResult1DataPack.add(hexStr);
        }

        //结果包2判断
        if (hexStr.equals("23") && bpResult2DataPack.isEmpty()) {
            bpResult2DataPack.add(hexStr);
        } else if (bpResult2DataPack.size() >= 1 && bpResult2DataPack.size() <= BP_RESULT2_PACK_SIZE) {
            bpResult2DataPack.add(hexStr);
        }

        //状态包判断
        if (hexStr.equals("24") && bpStsDataPack.isEmpty()) {
            bpStsDataPack.add(hexStr);
        } else if (bpStsDataPack.size() >= 1 && bpStsDataPack.size() <= BP_STS_PACK_SIZE) {
            bpStsDataPack.add(hexStr);
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
