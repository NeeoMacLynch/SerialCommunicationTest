package com.example.serialcommunicationtest.util;

import android.util.Log;

import com.example.serialcommunicationtest.bean.BPStsData;
import com.example.serialcommunicationtest.bean.BloodOxygenData;
import com.example.serialcommunicationtest.bean.BPCufPreData;

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
            dataPack.set(position + 1, realItem);
        }
    }

    /**
     * 从8位十六进制数取得有符号数
     *
     * @param hexStr -目标十六进制
     * @return 整型有符号数
     * */
    private static int getSignedFrom8BitsHex(String hexStr) {
        int unSignedData = Integer.parseInt(hexStr, 16);
        return (1 == (unSignedData & 0x80 >> 7))? -(unSignedData & 0x7F) : (unSignedData & 0x7F);
    }

    /**
     * 从16位十六进制数取得有符号数
     *
     * @param highByte -高字节
     * @param lowByte -低字节
     * @return 整型有符号数
     * */
    private static int getSignedFrom16BitsHex(String highByte, String lowByte) {
        int unSignedData = Integer.parseInt(highByte + lowByte, 16);
        return (1 == (unSignedData & 0x8000 >> 15))? -(unSignedData & 0x7FFF) : (unSignedData & 0x7FFF);
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

            pulseRate = getSignedFrom16BitsHex(boDataPack.get(3), boDataPack.get(4));
            bloodOxygen = getSignedFrom8BitsHex(boDataPack.get(5));

            bloodOxygenData = new BloodOxygenData(isBoDownOrNot, isSearchTimeTooLongOrNot, signalStrength, pulseRate, bloodOxygen);
        }

        return bloodOxygenData;
    }

    /**
     * 解析bp数据包
     * 按照包头判断不同的包
     *
     * @param bpDataPack -目标数据包
     * */
    // TODO：2020/7/6 将返回类型改为object
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

            case "20" : {
                //优先判断袖带错误信息
                int wrongFlag = Integer.parseInt(bpDataPack.get(4), 16);
                if (1 == wrongFlag) {
                    messageStr = "非新生儿模式下检测到新生儿袖带";
                    break;
                }

                BPCufPreData bpRealTimeData;
                int pressureData = getSignedFrom16BitsHex(bpDataPack.get(2), bpDataPack.get(3));
                //计算测量类型
                int measureTypePosition =  Integer.parseInt(bpDataPack.get(5), 16);
                bpRealTimeData = new BPCufPreData(pressureData, measureTypePosition);
                messageStr = bpRealTimeData.getRealTimeData();

                break;
            }

            case "21" : {
                String flag = "结束包消息：";

                String[] wrongList = {
                        "在手动测量方式下测量结束",
                        "在自动测量方式下测量结束",
                        "STAT测量结束",
                        "在校准方式下测量结束",
                        "在漏气检测中测量结束"
                };
                int wrongPosition = Integer.parseInt(bpDataPack.get(2), 16);
                if (10 != wrongPosition) {
                    messageStr = wrongList[wrongPosition];
                } else {
                    messageStr = "系统错误";
                    Log.e(TAG, flag + "错误信息见NBP状态包");
                }

                break;
            }

            case "22" : {
                // TODO：2020/7/6 未判断-100与数据范围
                int SP = getSignedFrom16BitsHex(bpDataPack.get(2), bpDataPack.get(3));  //收缩压
                int DP = getSignedFrom16BitsHex(bpDataPack.get(4), bpDataPack.get(5));  //舒张压
                int MAP = getSignedFrom16BitsHex(bpDataPack.get(6), bpDataPack.get(7)); //平均压
                messageStr =
                        "收缩压：" + SP + "\n" +
                        "舒张压" + DP + "\n" +
                        "平均压" + MAP;

                break;
            }

            case "23" : {
                // TODO：2020/7/6 未判断-100与数据范围
                int pulseRate = getSignedFrom16BitsHex(bpDataPack.get(2), bpDataPack.get(3)); //脉率
                messageStr = "收缩压：" + pulseRate;

                break;
            }

            case "24" : {
                String flag = "状态包消息：";

                //NBP状态信息获取
                int stateData = Integer.parseInt(bpDataPack.get(2), 16);
                int statePosition = (stateData & 0x0F);
                int patientModePosition = (stateData & 0x30) >> 4;
                int wrongPosition = Integer.parseInt(bpDataPack.get(4), 16);
                int cyclePosition = Integer.parseInt(bpDataPack.get(3), 16);
                int remainingTime = Integer.parseInt(bpDataPack.get(5) + bpDataPack.get(6), 16);

                BPStsData bpStsData = new BPStsData(statePosition, wrongPosition, patientModePosition, cyclePosition, remainingTime);

                //发送日志：测量模式
                Log.i(TAG, flag + bpStsData.getPatientMode());
                //发送日志：周期与剩余时间
                Log.i(TAG, flag + bpStsData.getCycleAndTimeMessage());
                if (10 != statePosition) {
                    messageStr = bpStsData.getStateStr();
                } else {
                    messageStr = "系统出错" + bpStsData.getWrongStr();
                }

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
