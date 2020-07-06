package com.example.serialcommunicationtest.bean;

public class BPStsData {
    String[] patientModeList = {
            "成人",
            "儿童",
            "新生儿"
    };

    String[] stateList = {
            "NBP复位完成",
            "手动测量中",
            "自动测量中",
            "STAT测量方法中",
            "校验中",
            "漏气检测中",
            "NBP复位"
    };

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

    String patientMode;
    String stateStr;
    String wrongStr;
    String cycleAndTimeMessage;

    String cycleStr;
    int remainingTime;

    public BPStsData(int statePosition, int wrongPosition, int patientModePosition, int cyclePosition, int remainingTime) {
        patientMode = "测量模式-" + patientModeList[patientModePosition];
        stateStr = stateList[statePosition];
        wrongStr = wrongList[wrongPosition];
        cycleStr = cycleList[cyclePosition];
        this.remainingTime = remainingTime;
        cycleAndTimeMessage = cycleStr + "剩余时间：" + remainingTime + "s";
    }

    public String getCycleAndTimeMessage() { return cycleAndTimeMessage; }
    public String getPatientMode() { return patientMode; }
    public String getWrongStr() { return wrongStr; }
    public String getStateStr() { return stateStr; }
}
