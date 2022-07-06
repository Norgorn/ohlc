package com.ohlc;

import com.ohlc.model.Ohlc;
import com.ohlc.model.OhlcPeriod;

import java.util.List;

public interface OhlcService extends QuoteListener {
    /** latest non persisted OHLC */
    Ohlc getCurrent (long instrumentId, OhlcPeriod period);
    /** all OHLCs which are kept in a database */
    List<Ohlc> getHistorical(long instrumentId, OhlcPeriod period);
    /** latest non persisted OHLC and OHLCs which are kept in a database */
    List<Ohlc> getHistoricalAndCurrent (long instrumentId, OhlcPeriod period);
}
