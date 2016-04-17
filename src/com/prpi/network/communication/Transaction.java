package com.prpi.network.communication;

abstract class Transaction {

    private Class objectTypeInTransaction;

    Transaction(Class type) {
        this.objectTypeInTransaction = type;
    }

    public abstract String getString(int offset, int length);

    public abstract int getLength();
}
