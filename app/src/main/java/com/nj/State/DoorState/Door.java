package com.nj.State.DoorState;

/**
 * Created by zbsz on 2017/9/27.
 */

public class Door {

    private Door(){}

    private static Door instance = null;

    public static Door getInstance(){
        return instance;
    }

    public static Door getInstance(DoorState doorState){
        if (instance == null)
            instance = new Door(doorState);
        return instance;
    }

    private DoorState doorState;

    private Door(DoorState doorState) {
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
