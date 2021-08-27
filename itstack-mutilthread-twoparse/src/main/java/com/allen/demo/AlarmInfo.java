package com.allen.demo;

/**
 * @Auther: allen
 * @Date: 2021-08-27 21:56
 * @Description:
 */
public class AlarmInfo {

    private String id;

    public AlarmType type;

    private String extraInfo;

    public AlarmInfo(String id, AlarmType type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }
}
