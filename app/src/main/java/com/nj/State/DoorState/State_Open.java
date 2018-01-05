package com.nj.State.DoorState;



import com.nj.EventBus.OpenDoorEvent;
import com.nj.State.LockState.Lock;
import com.nj.State.LockState.State_Lockup;
import com.nj.State.LockState.State_Unlock;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by zbsz on 2017/9/27.
 */

public class State_Open extends DoorState {

    Lock lock;

    public State_Open(Lock lock) {
        this.lock = lock;
    }


    @Override
    public void onHandle(Door door) {
        if (lock.getLockState().getClass().getName().equals(State_Lockup.class.getName())) {
            EventBus.getDefault().post(new OpenDoorEvent(false));
            lock.doNext();
        } else if (lock.getLockState().getClass().getName().equals(State_Unlock.class.getName())) {
            EventBus.getDefault().post(new OpenDoorEvent(true));
        }
    }
}
