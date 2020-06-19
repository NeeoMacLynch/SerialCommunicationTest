package com.example.ecgtest.util;

import android.util.Log;

import java.util.ArrayList;

public class DataUtils {

    private static final String TAG = "DataUtils";

    /**
     * 校验数据包
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
     * 解析数据包
     *
     * @param hexList -Hex数组，长度为8
     * @return 解析结果组成的数组{ECG1, ECG2}
     * */
    public static String unPackData(ArrayList<String> hexList) {
        ArrayList<Integer> ECGs = new ArrayList<>();
        //拼接[2,3] [4,5]并去掉第一位数（ECG数据不包含高位第一位子节）
        int ECG1 = Integer.parseInt((hexList.get(2) + hexList.get(3)).substring(1, 4), 16);
        int ECG2 = Integer.parseInt((hexList.get(4) + hexList.get(5)).substring(1, 4), 16);
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
     * 处理获取到的数据
     *
     * @param received -被处理数据
     * @param size -数组长度
     */
    public String onDataReceive(byte[] received, int size) {

        String hexStr = ByteUtils.bytes2HexStr(received, 0 , size);
        String msgStr = "接收数据：" + hexStr;

        Log.d(TAG, msgStr);

        return hexStr;

    }

}
