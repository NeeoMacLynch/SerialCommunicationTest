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

        switch(bpDataPack.get(0)) {
            case "04": {
                
            }
            case "24": {

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

        Log.d(TAG, msgStr);
        return hexStr;

    }

}
