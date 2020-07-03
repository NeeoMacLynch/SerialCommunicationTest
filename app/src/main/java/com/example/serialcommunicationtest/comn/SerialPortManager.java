package com.example.serialcommunicationtest.comn;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.example.serialcommunicationtest.bean.Device;
import com.example.serialcommunicationtest.util.DataConversionUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public class SerialPortManager {
    private static final String TAG = "SerialPortUtils";

    private SerialReadThread serialReadThread;
    private OutputStream outputStream;
    //private HandlerThread writeThread;

    private static class InstanceHolder {

        public static SerialPortManager serialPortManager = new SerialPortManager();
    }

    public static SerialPortManager instance() {
        return InstanceHolder.serialPortManager;
    }

    private SerialPort serialPort;

    private SerialPortManager() {
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

            outputStream = serialPort.getOutputStream();

            /*writeThread = new HandlerThread("WRITE_THREAD");
            writeThread.start();*/

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

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
    }

    /**
     * 发送数据
     *
     * @param data -被发送数据
     */
    public void sendData(byte[] data) throws Exception {
        outputStream.write(data);
    }

    /**
     * 发送命令
     *
     * @param command -被发送的命令
     * */
    public void sendCommand(String command) {
        try {
            SerialPortManager.instance().sendData(DataConversionUtils.hexStr2bytes(command));
        } catch (Exception e) {
            Log.e("发送：" + command + " 失败", e.toString());
        }
    }

}