package com.nj.EventBus;

/**
 * Created by zbsz on 2018/2/6.
 */

public class AlarmEvent {
    private int type;

    public AlarmEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
