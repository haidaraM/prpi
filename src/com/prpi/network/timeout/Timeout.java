package com.prpi.network.timeout;

import java.util.concurrent.TimeoutException;

public class Timeout {

    private int counter = 0;
    private int nbSteps;
    private int millisBetweenSteps;

    Timeout(int nbSteps, int millisBetweenSteps) {
        this.nbSteps = nbSteps;
        this.millisBetweenSteps = millisBetweenSteps;
    }

    public void step() throws TimeoutException {
        if (++counter == nbSteps) {
            throw new TimeoutException("Timeout reached");
        }

        try {
            Thread.sleep(millisBetweenSteps);
        } catch (InterruptedException ignored) {
        }
    }
}
