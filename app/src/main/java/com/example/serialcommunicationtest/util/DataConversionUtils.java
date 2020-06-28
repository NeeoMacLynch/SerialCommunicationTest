package com.example.serialcommunicationtest.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataConversionUtils {
    private static String baseHex =  "0123456789ABCDEF";

    /**
     * 十六进制字节数组转字符串
     *
     * @param bytes -目标数组
     * @param dec -起始位置
     * @param length -长度
     * @return 十六进制字符串
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
     * 16进制转ASCII
     * */
    public static String hexStr2Ascii(String hexStr) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            builder.append((char) Integer.parseInt(str, 16));
        }
        return builder.toString();
    }

    /**
     * 十进制转二进制byte数组
     * 只保留四位
     * */
    public static byte[] dec2BinArray(int a) {
        return new byte[] {
                (byte) ((a >> 3) & 0x01),
                (byte) ((a >> 2) & 0x01),
                (byte) ((a >> 1) & 0x01),
                (byte) (a & 0x01)
        };
    }


}
