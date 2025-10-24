package com.example.realtimevalsystem.service;

import com.example.realtimevalsystem.model.Security;
import com.example.realtimevalsystem.model.Stock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MarketDataPublisher implements Runnable {

    // 儲存所有 "股票" 證券，我們只模擬股票價格
    private final List<Stock> stocks = new ArrayList<>();

    // 儲存股票的目前價格
    private final Map<String, Double> currentStockPrices;

    // 監聽器，用於通知價格變化
    private MarketDataListener listener;

    private final Random random = new Random();

    /**
     * @param securityMap 從 SecurityDefinitionService 載入的所有證券
     * @param initialPrices 股票的初始價格 Map
     */
    public MarketDataPublisher(Map<String, Security> securityMap, Map<String, Double> initialPrices) {
        // 1. 篩選出所有 Stock
        for (Security sec : securityMap.values()) {
            if (sec instanceof Stock) {
                stocks.add((Stock) sec);
            }
        }
        // 2. 儲存目前價格的 Map
        this.currentStockPrices = initialPrices;
    }

    /**
     * 註冊一個監聽器來接收價格更新
     */
    public void setListener(MarketDataListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        // 挑戰要求：在一個單獨的線程中運行
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // 1. 隨機選擇一支股票
                Stock stockToUpdate = stocks.get(random.nextInt(stocks.size()));

                // 2. 計算 GBM [cite: 62-67]
                double newPrice = calculateNewPriceGBM(stockToUpdate);

                // 3. 更新內部價格
                currentStockPrices.put(stockToUpdate.getTicker(), newPrice);

                // 4. 發布更新 [cite: 18]
                if (listener != null) {
                    listener.onStockPriceUpdate(stockToUpdate.getTicker(), newPrice);
                }

                // 5. 隨機休眠 0.5 - 2 秒
                long sleepTime = 500 + random.nextInt(1501); // 500ms to 2000ms
                Thread.sleep(sleepTime);
            }
        } catch (InterruptedException e) {
            System.out.println("市場數據發布者線程已中斷。");
            Thread.currentThread().interrupt(); // 保持中斷狀態
        }
    }

    /**
     * 根據離散時間幾何布朗運動 (GBM) 公式計算新價格
     */
    private double calculateNewPriceGBM(Stock stock) {
        double S = currentStockPrices.get(stock.getTicker()); // 目前價格
        double mu = stock.getMu();
        double sigma = stock.getSigma();

        // 挑戰要求：隨機 0.5 - 2 秒
        // 這裡我們用一個簡化的 delta T (dt)，假設為 "1 tick" 的時間單位
        // 7257600 看似是一個年度秒數的常數，我們將 dt/7257600 視為一個小的時間步長
        double dt_over_const = 1.0 / 7257600.0; // 簡化模擬

        double epsilon = random.nextGaussian(); // 標準常態分佈隨機變數 [cite: 66]

        // 公式: (ΔS / S) = μ(Δt/C) + σε√(Δt/C)  [cite: 64]
        double deltaS_over_S = (mu * dt_over_const) + (sigma * epsilon * Math.sqrt(dt_over_const));
        double deltaS = S * deltaS_over_S;

        double newPrice = S + deltaS;

        // 價格永遠不能小於 0 [cite: 67]
        return Math.max(0.0, newPrice);
    }
}