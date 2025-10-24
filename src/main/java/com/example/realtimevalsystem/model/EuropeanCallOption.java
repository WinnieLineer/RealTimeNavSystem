package com.example.realtimevalsystem.model;

public class EuropeanCallOption implements Security {

    private final String ticker;
    private final String underlyingTicker; // 標的股票代碼 (e.g., "AAPL")
    private final double strikePrice;      // 履約價 (K)
    private final double timeToMaturity;   // 剩餘到期時間 (t, 以年為單位)
    private final double sigma;            // 標的股票的波動率

    public EuropeanCallOption(String ticker, String underlyingTicker, double strikePrice, double timeToMaturity, double sigma) {
        this.ticker = ticker;
        this.underlyingTicker = underlyingTicker;
        this.strikePrice = strikePrice;
        this.timeToMaturity = timeToMaturity;
        this.sigma = sigma;
    }

    @Override
    public String getTicker() {
        return ticker;
    }

    public String getUnderlyingTicker() {
        return underlyingTicker;
    }

    public double getStrikePrice() {
        return strikePrice;
    }

    public double getTimeToMaturity() {
        return timeToMaturity;
    }

    public double getSigma() {
        return sigma;
    }
}