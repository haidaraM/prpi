package com.prpi.network.communication;

import com.prpi.network.timeout.TimeoutEnum;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeoutException;

public interface ResponseWaiting {

    void setWaitingForResponse(@NotNull String transactionID);
    Transaction waitForTransactionResponse(@NotNull String transactionID, TimeoutEnum timeoutEnum) throws TimeoutException;

    boolean responseIsAwaited(@NotNull String transactionID);
    void addReceivedResponse(@NotNull Transaction transaction);
}
