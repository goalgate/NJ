package com.nj.State.OperationState;



/**
 * Created by zbsz on 2017/9/26.
 */

public class Two_man_OperateState extends OperationState {

    @Override
    public void onHandle(Operation op) {
        op.setState(new No_one_OperateState());
    }

}
