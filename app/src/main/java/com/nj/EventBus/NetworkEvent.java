package com.nj.EventBus;

/**
 * Created by zbsz on 2017/9/11.
 */

public class NetworkEvent {

    boolean network_state;

    public boolean getNetwork_state(){
        return network_state;
    }

    public NetworkEvent(boolean network_state){
        this.network_state = network_state;
    }



}
