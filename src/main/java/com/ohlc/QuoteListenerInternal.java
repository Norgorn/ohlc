package com.ohlc;

import com.ohlc.model.Ohlc;
import com.ohlc.model.OhlcPeriod;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;

public interface QuoteListenerInternal {
    void onQuote(Quote quote);

    Optional<Ohlc> getCurrent(long instrumentId, OhlcPeriod period);

    List<Ohlc> collectAndClear(long now);

    @PreDestroy
    void close();
}
