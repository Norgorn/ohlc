package com.ohlc.impl;

import com.ohlc.OhlcDao;
import com.ohlc.model.Ohlc;
import com.ohlc.model.OhlcPeriod;
import org.springframework.stereotype.Service;

import java.util.List;

// Just NOOP implementation to make app runnable
@Service
public class OhlcDaoImpl implements OhlcDao {

    @Override
    public void store(Ohlc ohlc) {

    }

    @Override
    public List<Ohlc> getHistorical(long instrumentId, OhlcPeriod period) {
        return List.of();
    }
}
