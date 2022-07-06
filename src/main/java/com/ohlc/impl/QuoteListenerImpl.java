package com.ohlc.impl;

import com.ohlc.Quote;
import com.ohlc.QuoteListenerInternal;
import com.ohlc.TimeService;
import com.ohlc.model.Ohlc;
import com.ohlc.model.OhlcPeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class QuoteListenerImpl implements QuoteListenerInternal {

    private static final long MINUTE_DELIMITER = TimeUnit.MINUTES.toMillis(1);
    private static final long HOUR_DELIMITER = TimeUnit.HOURS.toMillis(1);
    private static final long DAY_DELIMITER = TimeUnit.DAYS.toMillis(1);

    @Autowired
    TimeService timeService;

    // It is better to move map + delimiter to a separate class, but don't want to complicate things
    final ConcurrentMap<Long, OhlcPeriodProcessor> accumulatorsMinute = new ConcurrentHashMap<>();
    final ConcurrentMap<Long, OhlcPeriodProcessor> accumulatorsHour = new ConcurrentHashMap<>();
    final ConcurrentMap<Long, OhlcPeriodProcessor> accumulatorsDay = new ConcurrentHashMap<>();

    @Override
    public void onQuote(Quote quote) {
        enqueueToPeriod(quote, accumulatorsMinute, OhlcPeriod.M1, MINUTE_DELIMITER);
        enqueueToPeriod(quote, accumulatorsHour, OhlcPeriod.H1, HOUR_DELIMITER);
        enqueueToPeriod(quote, accumulatorsDay, OhlcPeriod.D1, DAY_DELIMITER);
    }

    @Override
    public Optional<Ohlc> getCurrent(long instrumentId, OhlcPeriod period) {
        long now = timeService.getTimestampMilliseconds();
        ConcurrentMap<Long, OhlcPeriodProcessor> map;
        long key;
        switch (period) {
            case M1:
                key = tsToKey(MINUTE_DELIMITER, now);
                map = accumulatorsMinute;
                break;
            case H1:
                key = tsToKey(HOUR_DELIMITER, now);
                map = accumulatorsHour;
                break;
            case D1:
                key = tsToKey(DAY_DELIMITER, now);
                map = accumulatorsDay;
                break;
            default:
                throw new IllegalArgumentException("Period not supported: " + period);
        }
        return Optional.ofNullable(map.get(key))
                .flatMap(a -> a.getCurrent(instrumentId));
    }

    @Override
    public List<Ohlc> collectAndClear(long now) {
        // The case when DAO is slow and cannot process transactions fast enough is omitted here
        List<Ohlc> values = new ArrayList<>();
        values.addAll(flushAndRemoveOld(this.accumulatorsMinute, tsToKey(MINUTE_DELIMITER, now)));
        values.addAll(flushAndRemoveOld(this.accumulatorsHour, tsToKey(HOUR_DELIMITER, now)));
        values.addAll(flushAndRemoveOld(this.accumulatorsDay, tsToKey(DAY_DELIMITER, now)));
        return values;
    }

    @Override
    @PreDestroy
    public void close() {
        // In normal flow this bean is closed by OhlcService (and all values stored)
        // But if that process fails, this method will ensure proper finalization of period processors
        collectAndClear(Long.MAX_VALUE);
    }

    private void enqueueToPeriod(Quote quote, ConcurrentMap<Long, OhlcPeriodProcessor> processors, OhlcPeriod period, long delimiter) {
        long key = tsToKey(delimiter, quote.getUtcTimestamp());
        OhlcPeriodProcessor processor = processors.computeIfAbsent(key,
                k -> new OhlcPeriodProcessor(period, toStartTime(delimiter, key), quote));
        processor.enqueue(quote);
    }

    private List<Ohlc> flushAndRemoveOld(ConcurrentMap<Long, OhlcPeriodProcessor> map, long excludeKey) {
        Set<Long> oldKeys = map.keySet().stream()
                .filter(k -> k != excludeKey)
                .collect(Collectors.toSet());
        return oldKeys.stream()
                .map(map::remove)
                .filter(Objects::nonNull) // just in case
                .flatMap(p -> p.awaitStop().stream())
                .collect(Collectors.toList());
    }

    private long tsToKey(long delimiter, long ts) {
        return ts / delimiter;
    }

    private long toStartTime(long delimiter, long key) {
        return key * delimiter;
    }
}
