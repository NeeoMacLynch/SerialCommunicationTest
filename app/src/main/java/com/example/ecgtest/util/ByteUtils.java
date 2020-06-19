package com.example.ecgtest.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ByteUtils {

    /**
     * 十六进制字节数组转字符串
     *
     * @param bytes -目标数组
     * @param dec -起始位置
     * @param length -长度
     * @return
     */
    public static String bytes2HexStr(byte[] bytes, int dec, int length) {
        byte[] temp = new byte[length];
        System.arraycopy(bytes, dec, temp, 0, length);
        return bytes2HexStr(temp);
    }

    /**
     * 字节数组转换成对应的16进制表示的字符串
     *
     * @param bytes -目标数组
     * @return Hex字符串
     */
    public static String bytes2HexStr(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        if (bytes == null || bytes.length <= 0) {
            return "";
        }
        char[] buffer = new char[2];
        for (byte aByte : bytes) {
            buffer[0] = Character.forDigit((aByte >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(aByte & 0x0F, 16);
            builder.append(buffer);
        }
        //toUpperCase()字符串转换为大写
        return builder.toString().toUpperCase();
    }

    /**
     * 16进制字符串转十进制数字
     * 每两位转换为十进制
     *
     * @param hexStr -目标HEX字符串
     * @return 由整型数值组成的字符串
     */
    public static String hexStr2IntStr(String hexStr) {

        List<String> hexList = Arrays.asList(hexStr.split(" "));
        List<String> intList = new ArrayList<>();

        for (int position = 1 ; position < hexList.size(); position++) {
            intList.add(
                    String.valueOf(
                            Integer.parseInt(hexList.get(position), 16)
                    ));
        }

        StringBuilder builder = new StringBuilder();
        for(String intItem : intList){
            builder.append(intItem);
            builder.append(" ");
        }

        return builder.toString();
    }

}
