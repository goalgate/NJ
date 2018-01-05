package com.nj.State.LockState;



/**
 * Created by zbsz on 2017/9/28.
 */

public class Lock {

    private LockState lockState;

    public Lock(LockState lockState) {

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
