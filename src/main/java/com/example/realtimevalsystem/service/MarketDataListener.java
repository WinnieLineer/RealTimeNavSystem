package com.example.realtimevalsystem.service;

/**
 * 監聽器介面，用於接收來自 MarketDataPublisher 的即時股票價格更新
 */
public interface MarketDataListener {

    /**
     * 當一支股票價格更新時被呼叫
     * @param ticker 股票代碼 (e.g., "AAPL")
     * @param newPrice 新的價格
     */
    void onStockPriceUpdate(String ticker, double newPrice);
}