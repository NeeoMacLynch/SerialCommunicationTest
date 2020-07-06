package com.example.serialcommunicationtest.bean;

public class BPCufPreData {
    static final String[] measureTypeList = {
            "在手动测量方式下，",
            "在自动测量方式下，",
            "在STAT测量方式下，",
            "在校准方式下，",
            "在漏气检测中，"
    };
    static final String dataStrHead = "袖带压力为：";

    int pressureData;
    String measureType;

    public BPCufPreData(int pressureData, int measureTypePosition) {
        this.pressureData = pressureData;
        measureType = measureTypeList[measureTypePosition];
    }

    /**
     * 返回构造好的信息
     * */
    public String getRealTimeData() {
        if (pressureData >= 0 && pressureData <= 300) {
            return measureType + dataStrHead + pressureData;
        } else {
            return "获取值无效";
        }
    }

}
