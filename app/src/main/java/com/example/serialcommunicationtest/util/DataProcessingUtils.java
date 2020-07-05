package com.example.serialcommunicationtest.util;

import android.util.Log;
import android.widget.Switch;

import com.example.serialcommunicationtest.bean.BloodOxygenData;

import java.util.ArrayList;

public class DataProcessingUtils {

    private static final String TAG = "DataUtils";

    /**
     * 校验ecg、bo、bp数据包
     *
     * @param hexList -Hex数组，长度为8
     * @return 校验结果
     * */
    public static Boolean checkDataPack(ArrayList<String> hexList) {
        //校验和
        int checkSum = 0;
        //获取数据包中的最后一个值，即校验值
        int lastElem = Integer.parseInt(hexList.get(hexList.size()-1), 16);
        for (String hexStr : hexList) checkSum += Integer.parseInt(hexStr, 16);
        //数据包数据和减除校验值得到校验和
        checkSum -= lastElem;
        //返回校验结果
        return (checkSum & 0x7F) == (lastElem & 0x7F);
    }

    /**
     * 转换数据包内数据为真值
     *
     * @param dataPack -被处理数据包
     * */
    private static void getRealData(ArrayList<String> dataPack) {
        //获得数据头并处理
        int head = Integer.parseInt(dataPack.get(1), 16) & 0x7F;

        //减3减去的是包头、数据头、校验位
        for (int position = 1; position <= dataPack.size() - 3; position++) {
            int high = (head & (0x01 << position - 1)) << (8 - position);
            int item = Integer.parseInt(dataPack.get(position+1), 16) & 0x7F;
            String realItem = Integer.toHexString(item | high);
            if (realItem.length() == 1) realItem = "0" + realItem;
            dataPack.set(position+1, realItem);
        }

    }

    /**
     * 解析ecg数据包
     *
     * @param ecgDataPack -Hex数组，长度为8
     * @return 解析结果组成的数组{ECG1, ECG2}
     * */
    public static String unPackEcgData(ArrayList<String> ecgDataPack) {
        ArrayList<Integer> ECGs = new ArrayList<>();

        getRealData(ecgDataPack);

        //拼接[2,3] [4,5]
        int ECG1 = Integer.parseInt((ecgDataPack.get(2) + ecgDataPack.get(3)), 16);
        int ECG2 = Integer.parseInt((ecgDataPack.get(4) + ecgDataPack.get(5)), 16);
        ECGs.add(ECG1);
        ECGs.add(ECG2);

        StringBuilder ECGsBuilder = new StringBuilder();
        for(int intItem : ECGs){

            ECGsBuilder.append(intItem);
            ECGsBuilder.append(" ");
        }
        return ECGsBuilder.toString();
    }

    /**
     * 解析bo数据包
     *
     * @param boDataPack -目标数据包
     * */
    public static BloodOxygenData unPackBoData(ArrayList<String> boDataPack) {
        BloodOxygenData bloodOxygenData;
        // TODO：2020/7/3 负数数据也要解析，可能需要在真值转换中加入负数部分的判断处理

        //数据头不为0x80，数据无效
        if (!"80".equals(boDataPack.get(1))) {
            bloodOxygenData = new BloodOxygenData(false);
        } else {
            int isBoDownOrNot;
            int isSearchTimeTooLongOrNot;
            int signalStrength;
            int pulseRate;
            int bloodOxygen;

            getRealData(boDataPack);

            int stateItem = Integer.parseInt(boDataPack.get(2), 16);
            isBoDownOrNot = (stateItem & 0x20) >> 5;
            isSearchTimeTooLongOrNot = (stateItem & 0x10) >> 4;
            signalStrength = stateItem & 0x0F;

            pulseRate = Integer.parseInt(boDataPack.get(3) + boDataPack.get(4), 16) & 0x7F;
            bloodOxygen = Integer.parseInt(boDataPack.get(5), 16) & 0x7F;

            bloodOxygenData = new BloodOxygenData(isBoDownOrNot, isSearchTimeTooLongOrNot, signalStrength, pulseRate, bloodOxygen);
        }

        return bloodOxygenData;
    }

    public static String unPackBpData(ArrayList<String> bpDataPack) {
        String messageStr;
        getRealData(bpDataPack);

        String[] ackMessage = {
                "命令成功",
                "校验和错误",
                "命令包长度错误",
                "无效命令",
                "命令参数数据错误",
                "命令不接受"
        };

        switch(bpDataPack.get(0)) {
            case "04" : {
                int position;
                position = Integer.parseInt(bpDataPack.get(3), 16);
                messageStr = ackMessage[position];
                break;
            }
            case "24" : {
                String flag = "状态包消息：";

                //NBP状态信息获取
                int stateData = Integer.parseInt(bpDataPack.get(2), 16);
                String[] patientModeList = {
                        "成人",
                        "儿童",
                        "新生儿"
                };
                int patientModePosition = (stateData & 0x30) >> 4;
                String patientMode = "测量模式-" + patientModeList[patientModePosition];
                //发送日志：测量模式
                Log.i(TAG, flag + patientMode);
                String[] stateList = {
                        "NBP复位完成",
                        "手动测量中",
                        "自动测量中",
                        "STAT测量方法中",
                        "校验中",
                        "漏气检测中",
                        "NBP复位"
                };
                int statePosition = (stateData & 0x0F);
                if (10 != statePosition) {
                    messageStr = stateList[statePosition];
                } else {
                    String[] wrongList = {
                            "无错误",
                            "袖带过松",
                            "漏气",
                            "气压错误",
                            "弱信号",
                            "超范围",
                            "过分运动",
                            "过压",
                            "信号饱和",
                            "漏气检测失败",
                            "系统错误",
                            "超时"
                    };
                    int wrongPosition = Integer.parseInt(bpDataPack.get(4), 16);
                    String wrongStr = wrongList[wrongPosition];
                    messageStr = "系统出错" + wrongStr;
                }

                String[] cycleList = {
                        "在手动测量方式下，",
                        "在自动测量方式下，对应周期为1分钟，",
                        "在自动测量方式下，对应周期为2分钟，",
                        "在自动测量方式下，对应周期为3分钟，",
                        "在自动测量方式下，对应周期为4分钟，",
                        "在自动测量方式下，对应周期为5分钟，",
                        "在自动测量方式下，对应周期为10分钟，",
                        "在自动测量方式下，对应周期为15分钟，",
                        "在自动测量方式下，对应周期为30分钟，",
                        "在自动测量方式下，对应周期为1小时，",
                        "在自动测量方式下，对应周期为1.5小时，",
                        "在自动测量方式下，对应周期为2小时，",
                        "在自动测量方式下，对应周期为3小时，",
                        "在自动测量方式下，对应周期为4小时，",
                        "在自动测量方式下，对应周期为8小时，",
                        "在STAT测量方式下，",
                };
                int cyclePosition = Integer.parseInt(bpDataPack.get(3), 16);
                String cycleStr = cycleList[cyclePosition];
                int remainingTime = Integer.parseInt(bpDataPack.get(5) + bpDataPack.get(6), 16);
                String cycleAndTimeMessage = cycleStr + "剩余时间：" + remainingTime + "s";
                //发送日志：周期与剩余时间
                Log.i(TAG, flag + cycleAndTimeMessage);

                break;
            }
            default: messageStr = "打包错误";
        }

        return messageStr;
    }

    /**
     * 解析the数据包
     *
     * @param theDataPack -体温结果字符数组
     * @return 解析结果为体温数字字符串或错误信息
     * */
    public static String unPackTheData(ArrayList<String> theDataPack) {

        StringBuilder builder = new StringBuilder();
        for (String strItem : theDataPack) {
            builder.append(DataConversionUtils.hexStr2Ascii(strItem));
        }
        String resStr = builder.toString();

        //解析错误信息
        if (theDataPack.size() == 3){
            switch (resStr) {
                case "ErL" : return "温度过低";
                case "ErH" : return "温度过高";
                case "ErP" : return "硬件错误";
            }
        }

        return resStr;
    }

    /**
     * 处理获取到的数据
     *
     * @param received -被处理数据
     * @param size -数组长度
     */
    public static String onDataReceive(byte[] received, int size, String flag) {

        String hexStr = DataConversionUtils.bytes2HexStr(received, 0 , size);
        String msgStr = flag + "接收数据：" + hexStr;

        //Log.d(TAG, msgStr);
        return hexStr;

    }

}
