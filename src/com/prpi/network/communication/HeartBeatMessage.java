package com.prpi.network.communication;

public class HeartBeatMessage extends Message<HeartBeat> {
    public HeartBeatMessage(HeartBeat obj, TransactionType transactionType) {
        super(obj, transactionType);
    }


}
