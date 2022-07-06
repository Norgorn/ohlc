package com.ohlc.impl;

import com.ohlc.Quote;
import com.ohlc.TimeService;
import com.ohlc.model.Ohlc;
import com.ohlc.model.OhlcPeriod;
import com.ohlc.model.QuoteImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class QuoteListenerImplTest {

    @Mock
    TimeService timeService;

    @InjectMocks
    QuoteListenerImpl sut;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getCurrent_whenNoData() {
        when(timeService.getTimestampMilliseconds()).thenReturn(800_000_000L);

        assertTrue(sut.getCurrent(1010101, OhlcPeriod.M1).isEmpty());
    }

    @Test
    public void getCurrent_whenHasData_minute() {
        int instrument = 150;
        OhlcPeriod period = OhlcPeriod.M1;
        long startTime = TimeUnit.DAYS.toMillis(10);
        double price = 100;
        Quote value = new QuoteImpl(instrument, startTime, price);
        when(timeService.getTimestampMilliseconds()).thenReturn(startTime);
        sut.accumulatorsMinute.put(14400L, new OhlcPeriodProcessor(period, startTime, value));

        Optional<Ohlc> actual = sut.getCurrent(instrument, period);

        Ohlc expected = new Ohlc(period, instrument, startTime, price, price, price, price);
        assertEquals(Optional.of(expected), actual);
    }

    @Test
    public void getCurrent_whenHasData_hour() {
        int instrument = 150;
        OhlcPeriod period = OhlcPeriod.H1;
        long startTime = TimeUnit.DAYS.toMillis(10);
        double price = 100;
        Quote value = new QuoteImpl(instrument, startTime, price);
        when(timeService.getTimestampMilliseconds()).thenReturn(startTime);
        sut.accumulatorsHour.put(240L, new OhlcPeriodProcessor(period, startTime, value));

        Optional<Ohlc> actual = sut.getCurrent(instrument, period);

        Ohlc expected = new Ohlc(period, instrument, startTime, price, price, price, price);
        assertEquals(Optional.of(expected), actual);
    }

    @Test
    public void getCurrent_whenHasData_day() {
        int instrument = 150;
        OhlcPeriod period = OhlcPeriod.D1;
        long startTime = TimeUnit.DAYS.toMillis(10);
        double price = 100;
        Quote value = new QuoteImpl(instrument, startTime, price);
        when(timeService.getTimestampMilliseconds()).thenReturn(startTime);
        sut.accumulatorsDay.put(10L, new OhlcPeriodProcessor(period, startTime, value));

        Optional<Ohlc> actual = sut.getCurrent(instrument, period);

        Ohlc expected = new Ohlc(period, instrument, startTime, price, price, price, price);
        assertEquals(Optional.of(expected), actual);
    }

    @Test
    public void onQuote_whenSingle() {
        int instrument = 1;
        double price = 40;
        long timestamp = TimeUnit.DAYS.toMillis(10);
        Quote quote = new QuoteImpl(instrument, timestamp, price);

        sut.onQuote(quote);

        assertTrue(sut.accumulatorsMinute.containsKey(14400L));
        assertTrue(sut.accumulatorsHour.containsKey(240L));
        assertTrue(sut.accumulatorsDay.containsKey(10L));
        Ohlc expected = new Ohlc(OhlcPeriod.M1, instrument, timestamp, price, price, price, price);
        Ohlc actual = sut.accumulatorsMinute.get(14400L).getCurrent(instrument).orElseThrow();
        assertEquals(expected, actual);
        expected = new Ohlc(OhlcPeriod.H1, instrument, timestamp, price, price, price, price);
        actual = sut.accumulatorsHour.get(240L).getCurrent(instrument).orElseThrow();
        assertEquals(expected, actual);
        expected = new Ohlc(OhlcPeriod.D1, instrument, timestamp, price, price, price, price);
        actual = sut.accumulatorsDay.get(10L).getCurrent(instrument).orElseThrow();
        assertEquals(expected, actual);
    }

    @Test
    public void onQuote_whenDifferentPeriods() {
        int instrument = 1;
        double price1 = 40;
        double price2 = 50;
        long timestamp1 = TimeUnit.DAYS.toMillis(10);
        long timestamp2 = TimeUnit.DAYS.toMillis(11);
        Quote quote1 = new QuoteImpl(instrument, timestamp1, price1);
        Quote quote2 = new QuoteImpl(instrument, timestamp2, price2);

        sut.onQuote(quote1);
        sut.onQuote(quote2);

        assertTrue(sut.accumulatorsMinute.containsKey(15840L));
        assertTrue(sut.accumulatorsMinute.containsKey(14400L));
        Ohlc expected = new Ohlc(OhlcPeriod.M1, instrument, timestamp1, price1, price1, price1, price1);
        Ohlc actual = sut.accumulatorsMinute.get(14400L).getCurrent(instrument).orElseThrow();
        assertEquals(expected, actual);
        expected = new Ohlc(OhlcPeriod.M1, instrument, timestamp2, price2, price2, price2, price2);
        actual = sut.accumulatorsMinute.get(15840L).getCurrent(instrument).orElseThrow();
        assertEquals(expected, actual);
    }

    @Test
    public void onQuote_whenDifferentInstruments() {
        int instrument1 = 1;
        int instrument2 = 2;
        double price1 = 40;
        double price2 = 50;
        long timestamp = TimeUnit.DAYS.toMillis(10);
        Quote quote1 = new QuoteImpl(instrument1, timestamp, price1);
        Quote quote2 = new QuoteImpl(instrument2, timestamp, price2);

        sut.onQuote(quote1);
        sut.onQuote(quote2);

        assertTrue(sut.accumulatorsMinute.containsKey(14400L));
        Ohlc expected = new Ohlc(OhlcPeriod.M1, instrument1, timestamp, price1, price1, price1, price1);
        OhlcPeriodProcessor processor = sut.accumulatorsMinute.get(14400L);
        Ohlc actual = processor.getCurrent(instrument1).orElseThrow();
        assertEquals(expected, actual);
        expected = new Ohlc(OhlcPeriod.M1, instrument2, timestamp, price2, price2, price2, price2);
        actual = processor.getCurrent(instrument2).orElseThrow();
        assertEquals(expected, actual);
    }

    @Test
    @Disabled("For manual run")
    // Probably better to use JMH for benchmarking - but this basic one just checks everything works
    public void manualTest() throws InterruptedException {
        Random rand = new Random(123);
        long now = 100_500_000;
        List<Quote> quotes = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            quotes.add(new QuoteImpl(rand.nextInt(2) + 1, now, rand.nextDouble() * 20));
        }


        long start = System.nanoTime();
        CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                for (int j = 0; j < 200; j++) {
                    quotes.forEach(sut::onQuote);
                }
                latch.countDown();
            }).start();
        }
        latch.await();
        List<Ohlc> values = sut.collectAndClear(now + TimeUnit.DAYS.toMillis(2));
        long finish = System.nanoTime();
        System.out.println((finish - start) + " ns");
        System.out.println((finish - start) / 1_000_000 + " ms");


        assertTrue(sut.accumulatorsMinute.isEmpty());
        assertTrue(sut.accumulatorsHour.isEmpty());
        assertTrue(sut.accumulatorsDay.isEmpty());
        double openExpected = quotes.stream().filter(q -> q.getInstrumentId() == 1).findFirst().orElseThrow().getPrice();
        double closeExpected = quotes.stream().filter(q -> q.getInstrumentId() == 1).reduce((first, second) -> second).orElseThrow().getPrice();
        assertEquals(openExpected, values.get(0).getOpen());
        assertEquals(closeExpected, values.get(0).getClose());

        double lowExpected = quotes.stream().filter(q -> q.getInstrumentId() == 1).mapToDouble(Quote::getPrice).min().orElseThrow();
        double highExpected = quotes.stream().filter(q -> q.getInstrumentId() == 1).mapToDouble(Quote::getPrice).max().orElseThrow();
        assertEquals(lowExpected, values.get(0).getLow());
        assertEquals(highExpected, values.get(0).getHigh());

        lowExpected = quotes.stream().filter(q -> q.getInstrumentId() == 2).mapToDouble(Quote::getPrice).min().orElseThrow();
        highExpected = quotes.stream().filter(q -> q.getInstrumentId() == 2).mapToDouble(Quote::getPrice).max().orElseThrow();
        assertEquals(lowExpected, values.get(1).getLow());
        assertEquals(highExpected, values.get(1).getHigh());
    }
}