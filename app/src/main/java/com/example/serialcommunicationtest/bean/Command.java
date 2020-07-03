package com.example.serialcommunicationtest.bean;

public class Command {
    private String command;
    private String[] modeList;

    public Command(String command) {
        this.command = command;
    }

    public Command(String command, String[] modeList) {
        this.command = command;
        this.modeList = modeList.clone();
    }

    public String getCommand() {
        return command;
    }

    public String[] getModeList() {
        return modeList;
    }

    public String getMode(int position) {
        return modeList[position];
    }
}
