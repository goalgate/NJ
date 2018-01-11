package com.nj.State.OperationState;



/**
 * Created by zbsz on 2017/9/26.
 */

public class No_one_OperateState extends OperationState {


    @Override
    public void onHandle(Operation op,Operation.Callback_Operation callback) {
        callback.uploadCallback();
    }

}
