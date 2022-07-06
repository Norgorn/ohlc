package com.ohlc.impl;

import com.ohlc.OhlcDao;
import com.ohlc.Quote;
import com.ohlc.QuoteListenerInternal;
import com.ohlc.TimeService;
import com.ohlc.model.Ohlc;
import com.ohlc.model.OhlcPeriod;
import com.ohlc.model.QuoteImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OhlcServiceImplTest {

    @Mock
    TimeService timeService;

    @Mock
    OhlcDao dao;

    @Mock
    QuoteListenerInternal listener;

    @InjectMocks
    OhlcServiceImpl sut;

    @BeforeEach
    public void setUp() {
        // Can also do this with runner
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void onQuote() {
        Quote quote = new QuoteImpl(10, 456546, 80.0);

        sut.onQuote(quote);

        verify(listener).onQuote(quote);
    }

    @Test
    public void getHistorical() {
        sut.getHistorical(1, OhlcPeriod.H1);

        verify(dao).getHistorical(1, OhlcPeriod.H1);
    }

    @Test
    public void getCurrent() {
        Ohlc expected = new Ohlc(OhlcPeriod.M1, 456, 789, 456, 456, 123, 44);
        when(listener.getCurrent(anyLong(), any())).thenReturn(Optional.of(expected));

        Ohlc actual = sut.getCurrent(1, OhlcPeriod.M1);

        verify(listener).getCurrent(1, OhlcPeriod.M1);
        assertEquals(expected, actual);
    }

    @Test
    public void getHistoricalAndCurrent() {
        int instrument = 4;
        OhlcPeriod period = OhlcPeriod.D1;
        Ohlc old = new Ohlc(period, instrument, 55_000, 11, 20, 10, 15);
        Ohlc current = new Ohlc(period, instrument, 100_000, 8, 100, 6, 80);
        when(dao.getHistorical(instrument, period)).thenReturn(List.of(old));
        when(listener.getCurrent(instrument, period)).thenReturn(Optional.of(current));

        List<Ohlc> values = sut.getHistoricalAndCurrent(instrument, period);

        assertEquals(List.of(old, current), values);
    }
}