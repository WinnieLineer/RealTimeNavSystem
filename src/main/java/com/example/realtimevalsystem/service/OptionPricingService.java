package com.example.realtimevalsystem.service;

public class OptionPricingService {

    // 無風險利率 (r) 固定為 2%
    private static final double RISK_FREE_RATE = 0.02;

    /**
     * 計算歐式買權 (Call Option) 的價格
     * @param S     標的股票的目前價格
     * @param K     履約價 (Strike Price) [cite: 75]
     * @param sigma 標的股票的波動率
     * @param t     剩餘到期時間 (年) [cite: 75]
     * @return 買權價格 c
     */
    public double calculateCallPrice(double S, double K, double sigma, double t) {
        double d1 = calculateD1(S, K, sigma, t);
        double d2 = calculateD2(d1, sigma, t);

        // Black-Scholes 買權公式: c = S*N(d1) - K*e^(-rt)*N(d2)
        return (S * N(d1)) - (K * Math.exp(-RISK_FREE_RATE * t) * N(d2));
    }

    /**
     * 計算歐式賣權 (Put Option) 的價格
     * @param S     標的股票的目前價格
     * @param K     履約價 (Strike Price) [cite: 75]
     * @param sigma 標的股票的波動率
     * @param t     剩餘到期時間 (年) [cite: 75]
     * @return 賣權價格 p
     */
    public double calculatePutPrice(double S, double K, double sigma, double t) {
        double d1 = calculateD1(S, K, sigma, t);
        double d2 = calculateD2(d1, sigma, t);

        // Black-Scholes 賣權公式: p = K*e^(-rt)*N(-d2) - S*N(-d1)
        return (K * Math.exp(-RISK_FREE_RATE * t) * N(-d2)) - (S * N(-d1));
    }

    // 計算 d1
    private double calculateD1(double S, double K, double sigma, double t) {
        // d1 = ( ln(S/K) + (r + sigma^2 / 2) * t ) / ( sigma * sqrt(t) )
        return (Math.log(S / K) + (RISK_FREE_RATE + Math.pow(sigma, 2) / 2) * t) / (sigma * Math.sqrt(t));
    }

    // 計算 d2
    private double calculateD2(double d1, double sigma, double t) {
        // d2 = d1 - sigma * sqrt(t)
        return d1 - sigma * Math.sqrt(t);
    }
    /**
     * 計算標準常態分佈的累積機率函數 (CDF) - N(x)
     * 由於 JDK 1.8 限制，我們使用 Abramowitz and Stegun 的 "Formula 7.1.26" 逼近法。
     */
    private double N(double z) {
        // N(z) = 0.5 * (1 + erf(z / sqrt(2)))
        return 0.5 * (1.0 + erf(z / Math.sqrt(2.0)));
    }

    /**
     * 誤差函數 erf(x) 的高精度逼近實作 (Formula 7.1.26)
     */
    private double erf(double z) {
        // 逼近法所使用的常數
        double a1 =  0.254829592;
        double a2 = -0.284496736;
        double a3 =  1.421413741;
        double a4 = -1.453152027;
        double a5 =  1.061405429;
        double p  =  0.3275911;

        // 保存 z 的符號
        int sign = 1;
        if (z < 0) {
            sign = -1;
        }
        double absZ = Math.abs(z);

        // A&S formula
        double t = 1.0 / (1.0 + p * absZ);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-absZ * absZ);

        return sign * y;
    }
}