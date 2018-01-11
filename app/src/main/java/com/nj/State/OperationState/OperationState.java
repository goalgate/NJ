package com.nj.State.OperationState;


/**
 * Created by zbsz on 2017/9/26.
 */

public abstract class OperationState {

    public abstract void onHandle(Operation op,Operation.Callback_Operation callback);


}
