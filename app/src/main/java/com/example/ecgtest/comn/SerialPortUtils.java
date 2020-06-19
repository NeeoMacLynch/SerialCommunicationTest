package com.example.ecgtest.comn;

import android.util.Log;

import com.example.ecgtest.bean.Device;

import java.io.File;

import android_serialport_api.SerialPort;

public class SerialPortUtils {
    private static final String TAG = "SerialPortUtils";

    private SerialReadThread serialReadThread;

    private static class InstanceHolder {

        public static SerialPortUtils serialPortUtils = new SerialPortUtils();
    }

    public static SerialPortUtils instance() {
        return InstanceHolder.serialPortUtils;
    }

    private SerialPort serialPort;

    private SerialPortUtils() {
    }

    /**
     * 打开串口
     *
     * @param device -设备
     * @return -open(String devicePath, String baudrateString)
     */
    public SerialPort open(Device device) {
        return open(device.getPath(), device.getBaudrate());
    }

    /**
     * 打开串口
     *
     * @param devicePath -串口
     * @param baudrateString -波特率
     * @return -serialPort
     */
    public SerialPort open(String devicePath, String baudrateString) {
        if (serialPort != null) {
            close();
        }

        try {
            File device = new File(devicePath);
            int baudrate = Integer.parseInt(baudrateString);
            serialPort = new SerialPort(device, baudrate, 0);

            serialReadThread = new SerialReadThread(serialPort.getInputStream());
            serialReadThread.start();

            return serialPort;
        } catch (Throwable tr) {
            Log.e(TAG, "打开串口失败", tr);
            close();
            return null;
        }
    }

    /**
     * 关闭串口
     */
    public void close() {
        if (serialReadThread != null) {
            serialReadThread.close();
        }

        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
    }


}