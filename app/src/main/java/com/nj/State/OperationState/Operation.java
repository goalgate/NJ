package com.nj.State.OperationState;



/**
 * Created by zbsz on 2017/9/26.
 */

public class Operation {

    private OperationState state;

    public Operation(OperationState state){
        this.state = state;
    }

    public OperationState getState() {
        return state;
    }

    public void setState(OperationState state) {
        this.state = state;
    }

    public void doNext(){
        state.onHandle(this);
    }
}
