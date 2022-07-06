package com.ohlc.impl;

import com.ohlc.Quote;
import com.ohlc.model.Ohlc;
import com.ohlc.model.OhlcPeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class OhlcPeriodProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ConcurrentMap<Long, OhlcAccumulator> instrumentAccumulators = new ConcurrentHashMap<>();

    private final ArrayBlockingQueue<Quote> queue = new ArrayBlockingQueue<>(1_000_000);

    private final OhlcPeriod period;
    private final long startTime;

    private final AtomicBoolean keepPolling = new AtomicBoolean(true);
    private final Thread workerThread;
    private final Object workerThreadLock = new Object();

    public OhlcPeriodProcessor(OhlcPeriod period, long startTime, Quote initial) {
        this.period = period;
        this.startTime = startTime;
        processQuote(initial);
        workerThread = new Thread(this::awaitDequeue, "period-" + period.name() + "-worker");
        workerThread.start();
    }

    public Optional<Ohlc> getCurrent(long instrumentId) {
        return Optional.ofNullable(instrumentAccumulators.get(instrumentId))
                .map(a -> a.toOhlc(period, startTime));
    }

    public void enqueue(Quote quote) {
        if (!keepPolling.get()) {throw new IllegalStateException("Worker not running");}
        // Actual quote processing is decoupled from main thread to avoid any synchronization there
        // Queue is processed by a separate thread and all data is handled locally there
        queue.add(quote);
    }

    public List<Ohlc> awaitStop() {
        keepPolling.set(false);
        synchronized (workerThreadLock) {
            workerThread.interrupt();
        }
        Collection<Quote> remainingValues = new ArrayList<>(queue.size());
        queue.drainTo(remainingValues);
        remainingValues.forEach(this::processQuote);
        return instrumentAccumulators.values().stream()
                .map(a -> a.toOhlc(period, startTime))
                .collect(Collectors.toList());
    }

    private void awaitDequeue() {
        try {
            while (keepPolling.get()) {
                // Synchronized here is never actually blocked unless awaiting stop - but in that case it is necessary to prevent data loss
                synchronized (workerThreadLock) {
                    Quote quote = queue.poll(25, TimeUnit.MILLISECONDS);
                    if (quote != null) {
                        processQuote(quote);
                    }
                }
            }
        } catch (InterruptedException e) {
            logger.debug("Worker thread interrupted");
        }
    }

    private void processQuote(Quote quote) {
        instrumentAccumulators.computeIfAbsent(quote.getInstrumentId(), k -> new OhlcAccumulator(quote.getInstrumentId(), quote.getPrice()))
                .addValue(quote.getPrice());
    }
}
