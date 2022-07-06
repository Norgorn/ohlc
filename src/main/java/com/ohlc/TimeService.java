package com.ohlc;


public interface TimeService {

    default long getTimestampMilliseconds() {
        // Just basic way
        return System.currentTimeMillis();
    }
}
