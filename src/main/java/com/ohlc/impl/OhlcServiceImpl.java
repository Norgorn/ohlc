package com.ohlc.impl;

import com.ohlc.*;
import com.ohlc.model.Ohlc;
import com.ohlc.model.OhlcPeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class OhlcServiceImpl implements OhlcService {

    // Prefer field injection
    @Autowired
    OhlcDao dao;

    @Autowired
    QuoteListenerInternal listener;

    @Autowired
    TimeService timeService;

    private final ConcurrentMap<Long, OhlcPeriodProcessor> accumulatorsMinute = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, OhlcPeriodProcessor> accumulatorsHour = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, OhlcPeriodProcessor> accumulatorsDay = new ConcurrentHashMap<>();

    @Override
    public void onQuote(Quote quote) {
        listener.onQuote(quote);
    }

    @Override
    @Nullable
    public Ohlc getCurrent(long instrumentId, OhlcPeriod period) {
        // Would prefer to return Optional here
        return listener.getCurrent(instrumentId, period).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ohlc> getHistorical(long instrumentId, OhlcPeriod period) {
        return dao.getHistorical(instrumentId, period);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ohlc> getHistoricalAndCurrent(long instrumentId, OhlcPeriod period) {
        List<Ohlc> list = new ArrayList<>(dao.getHistorical(instrumentId, period));
        Ohlc current = getCurrent(instrumentId, period);
        if (current != null) {list.add(current);}
        return list;
    }

    @Scheduled(cron = "* * * * 1 ?")
    @Transactional
    public void flush() {
        // The case when DAO is slow and cannot process transactions fast enough is omitted here
        long now = timeService.getTimestampMilliseconds();
        listener.collectAndClear(now).forEach(dao::store);
    }

    @PreDestroy
    @Transactional
    public void close() {
        listener.collectAndClear(Long.MAX_VALUE)
                .forEach(dao::store);
    }
}
