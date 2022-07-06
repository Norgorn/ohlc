package com.ohlc.model;

import com.google.common.base.MoreObjects;
import com.ohlc.Quote;

import java.util.Objects;

public class QuoteImpl implements Quote {

    private long instrumentId;
    private long utcTimestamp;
    private double price;

    public QuoteImpl() {
    }

    public QuoteImpl(long instrumentId, long utcTimestamp, double price) {
        this.instrumentId = instrumentId;
        this.utcTimestamp = utcTimestamp;
        this.price = price;
    }

    @Override
    public long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(long instrumentId) {
        this.instrumentId = instrumentId;
    }

    @Override
    public long getUtcTimestamp() {
        return utcTimestamp;
    }

    public void setUtcTimestamp(long utcTimestamp) {
        this.utcTimestamp = utcTimestamp;
    }

    @Override
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        QuoteImpl quote = (QuoteImpl) o;
        return instrumentId == quote.instrumentId && utcTimestamp == quote.utcTimestamp && Double.compare(quote.price, price) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrumentId, utcTimestamp, price);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("instrumentId", instrumentId)
                .add("utcTimestamp", utcTimestamp)
                .add("price", price)
                .toString();
    }
}
