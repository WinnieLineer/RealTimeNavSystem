package com.example.realtimevalsystem.model;

public class Stock implements Security {

    private final String ticker;
    private final double mu;    // 預期報酬率
    private final double sigma; // 年化標準差 (波動率)

    public Stock(String ticker, double mu, double sigma) {
        this.ticker = ticker;
        this.mu = mu;
        this.sigma = sigma;
    }

    @Override
    public String getTicker() {
        return ticker;
    }

    public double getMu() {
        return mu;
    }

    public double getSigma() {
        return sigma;
    }
}