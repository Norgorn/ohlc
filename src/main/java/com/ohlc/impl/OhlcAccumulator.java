package com.ohlc.impl;

import com.ohlc.model.Ohlc;
import com.ohlc.model.OhlcPeriod;

public class OhlcAccumulator {

    private static class ValueHolder {

        static ValueHolder hold(ValueHolder previous, double newValue) {
            double high = Math.max(previous.high, newValue);
            double low = Math.min(previous.low, newValue);
            return new ValueHolder(high, low, newValue);
        }

        private final double high;
        private final double low;
        private final double close;

        ValueHolder(double high, double low, double close) {
            this.high = high;
            this.low = low;
            this.close = close;
        }
    }

    private final long instrumentId;

    // Should probably use BigDecimal for price? Since original data is in double, will just keep as is
    private final double open;

    // To avoid any inconsistencies
    private ValueHolder holder;

    public OhlcAccumulator(long instrumentId, double open) {
        this.instrumentId = instrumentId;
        this.open = open;
        this.holder = new ValueHolder(open, open, open);
    }

    public Ohlc toOhlc(OhlcPeriod period, long startTime) {
        return new Ohlc(period, instrumentId, startTime, open, holder.high, holder.low, holder.close);
    }

    public void addValue(Double value) {
        holder = ValueHolder.hold(holder, value);
    }
}
