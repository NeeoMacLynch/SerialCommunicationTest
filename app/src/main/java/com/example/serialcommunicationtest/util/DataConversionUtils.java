package com.example.serialcommunicationtest.util;

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

    /**
     * 把十六进制表示的字节数组字符串，转换成十六进制字节数组
     * 只能转不带空格的hexStr
     *
     * @param hexStr -目标十六进制字符串
     * @return byte[]
     */
    public static byte[] hexStr2bytes(String hexStr) {
        int len = (hexStr.length() / 2);
        byte[] result = new byte[len];
        char[] chars = hexStr.toUpperCase().toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (hexChar2byte(chars[pos]) << 4 | hexChar2byte(chars[pos + 1]));
        }
        return result;
    }

    public static byte[] hexStr2BytesWithSpace(String hexString) {
        String[] hexStrings = hexString.split("\\s+ ");
        byte[] bytes = new byte[hexStrings.length];
        for (int i = 0; i < hexStrings.length; i++) {
            char[] hexChars = hexStrings[i].toCharArray();
            bytes[i] = (byte) (hexChar2byte(hexChars[0]) << 4 | hexChar2byte(hexChars[1]));
        }
        return bytes;
    }


    /**
     * 把16进制字符[0123456789abcdef]（含大小写）转成字节
     *
     * @param c -目标字符
     * @return 对应子节
     */
    private static int hexChar2byte(char c) {
        switch (c) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'a':
            case 'A':
                return 10;
            case 'b':
            case 'B':
                return 11;
            case 'c':
            case 'C':
                return 12;
            case 'd':
            case 'D':
                return 13;
            case 'e':
            case 'E':
                return 14;
            case 'f':
            case 'F':
                return 15;
            default:
                return -1;
        }
    }

}
