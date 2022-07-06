package com.ohlc;

public interface Quote {
    double getPrice();

    long getInstrumentId();

    long getUtcTimestamp();
}
