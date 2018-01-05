package com.nj.State.DoorState;

/**
 * Created by zbsz on 2017/9/27.
 */

public class Door {

    private DoorState doorState;

    public Door(DoorState doorState) {
        this.doorState = doorState;
    }

    public DoorState getDoorState() {
        return doorState;
    }

    public void setDoorState(DoorState doorState) {
        this.doorState = doorState;
    }


    public void doNext(){
        doorState.onHandle(this);
    }
}
