package com.ohlc.model;

import com.google.common.base.MoreObjects;

import java.util.Objects;

// BTW Lombok might be a fine addition here, but it isn't necessary, so keep as is
public class Ohlc {

    // Non-final cause it is also used in DAO.
    // It would be better to separate entity from DTO, but right now keep everything as simple a possible
    private OhlcPeriod period;
    private long instrumentId;
    private long startTime;

    private double open;
    private double high;
    private double low;
    private double close;

    public Ohlc() {
    }

    public Ohlc(OhlcPeriod period, long instrumentId, long startTime,
                double open, double high, double low, double close) {
        this.period = period;
        this.instrumentId = instrumentId;
        this.startTime = startTime;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public OhlcPeriod getPeriod() {
        return period;
    }

    public void setPeriod(OhlcPeriod period) {
        this.period = period;
    }

    public long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        Ohlc ohlc = (Ohlc) o;
        return instrumentId == ohlc.instrumentId && startTime == ohlc.startTime && Double.compare(ohlc.open, open) == 0 && Double.compare(ohlc.high, high) == 0 &&
                Double.compare(ohlc.low, low) == 0 && Double.compare(ohlc.close, close) == 0 && period == ohlc.period;
    }

    @Override
    public int hashCode() {
        return Objects.hash(period, instrumentId, startTime, open, high, low, close);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("period", period)
                .add("instrumentId", instrumentId)
                .add("startTime", startTime)
                .add("open", open)
                .add("high", high)
                .add("low", low)
                .add("close", close)
                .toString();
    }
}
