package com.wq.mqtttrabbitmq.mqtt.constant;

public enum QosType {

    MOST_ONCE (0,"最多一次"),
    LEAST_ONCE (1,"最少一次"),
    ONLYONCE (2,"刚好一次");

    private Integer type;

    private String desc;

    QosType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }


    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

}
