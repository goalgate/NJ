package com.nj.State.DoorState;

import com.nj.State.LockState.Lock;

/**
 * Created by zbsz on 2017/9/27.
 */

public class State_Close extends DoorState {
    Lock lock;

    public State_Close(Lock lock) {
        this.lock = lock;
    }

    @Override
    public void onHandle(Door door) {

    }
}
