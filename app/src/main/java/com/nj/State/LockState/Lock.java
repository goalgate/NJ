package com.nj.State.LockState;



/**
 * Created by zbsz on 2017/9/28.
 */

public class Lock {

    private LockState lockState;
    private static Lock instance = null;

    public static Lock getInstance(){
        return instance;
    }

    public static Lock getInstance(LockState lockState){
        if (instance == null)
            instance = new Lock(lockState);
        return instance;
    }

    private Lock(LockState lockState) {

        this.lockState = lockState;

    }

    public LockState getLockState() {
        return lockState;
    }

    public void setLockState(LockState lockState) {
        this.lockState = lockState;
    }

    public void doNext(){
        lockState.onHandle(this);
    }


}
