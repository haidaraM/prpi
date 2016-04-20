package com.prpi.network.client;

import com.intellij.openapi.project.Project;
import com.prpi.network.communication.Transaction;
import io.netty.channel.SimpleChannelInboundHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractClientHandler extends SimpleChannelInboundHandler<String> {

    protected Project project = null;

    AbstractClientHandler(@NotNull Project project) {
        this.project = project;
    }

    AbstractClientHandler() {
        super();
    }

    public abstract @Nullable Transaction getTransactionResponse(@NotNull String transactionID);

    public Project getProject() {
        return project;
    }
}
