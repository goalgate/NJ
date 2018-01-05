package com.nj.State.LockState;


import com.nj.Function.Func_Switch.mvp.presenter.SwitchPresenter;

/**
 * Created by zbsz on 2017/9/28.
 */

public class State_Lockup extends LockState {

    SwitchPresenter sp;

    public State_Lockup(SwitchPresenter sp) {
        this.sp = sp;
    }
    @Override
    public void onHandle(Lock lock) {
        sp.OutD9(true);
    }
}