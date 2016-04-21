package com.prpi.network.timeout;

public class TimeoutFactory {

    public static Timeout create(TimeoutEnum timeoutEnum) {

        int nbSteps = 0;
        int timeBetweenSteps = 0; // milliseconds

        switch (timeoutEnum) {
            case NONE:
                nbSteps = -1;
                timeBetweenSteps = 1000;
                break;
            case SHORT:
                nbSteps = 10;
                timeBetweenSteps = 500;
                break;
            case MEDIUM:
                nbSteps = 30;
                timeBetweenSteps = 1000;
                break;
            case LONG:
                nbSteps = 30;
                timeBetweenSteps = 2000;
                break;

        }

        return new Timeout(nbSteps, timeBetweenSteps);
    }
}
