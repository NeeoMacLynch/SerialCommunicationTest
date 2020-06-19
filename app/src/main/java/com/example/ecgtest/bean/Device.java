package com.example.ecgtest.bean;

import androidx.annotation.NonNull;

/**
 * 串口设备参数
 */
public class Device {

    private String path;
    private String baudrate;

    public Device() {
    }

    public Device(String path, String baudrate) {
        this.path = path;
        this.baudrate = baudrate;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBaudrate() {
        return baudrate;
    }

    public void setBaudrate(String baudrate) {
        this.baudrate = baudrate;
    }

    @NonNull
    @Override
    public String toString() {
        return "Device{" + "path='" + path + '\'' + ", baudrate='" + baudrate + '\'' + '}';
    }
}