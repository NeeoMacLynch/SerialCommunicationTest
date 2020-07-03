package com.example.serialcommunicationtest.bean;

public class BloodOxygenData {
     static class State {
        String isBoDownOrNot;
        String isSearchTimeTooLongOrNot;
        String signalStrength;

         public State(int isBoDownOrNot,
                      int isSearchTimeTooLongOrNot,
                      int signalStrength) {

             this.isBoDownOrNot = (1 == isBoDownOrNot)? "血氧下降" : "血氧正常";
             this.isSearchTimeTooLongOrNot = (1 == isSearchTimeTooLongOrNot)? "搜索超时" : "搜索完成";
             this.signalStrength = (15 == signalStrength)? "信号无效" : "脉搏信号强度："+signalStrength;
         }
     }

     State state;
     int pulseRate;
     int bloodOxygen;
     Boolean isEffectiveOrNot = true;

     public BloodOxygenData(Boolean isEffectiveOrNot){
         this.isEffectiveOrNot = isEffectiveOrNot;
     }

     public BloodOxygenData(int isBoDownOrNot,
                            int isSearchTimeTooLongOrNot,
                            int signalStrength,
                            int pulseRate,
                            int bloodOxygen){

            this.state = new State(isBoDownOrNot, isSearchTimeTooLongOrNot, signalStrength);
            this.pulseRate = pulseRate;
            this.bloodOxygen = bloodOxygen;

     }

     public int getPulseRate(){ return pulseRate; }
     public int getBloodOxygen(){ return bloodOxygen; }
}
