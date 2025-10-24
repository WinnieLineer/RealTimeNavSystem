package com.example.realtimevalsystem.service;

import com.example.realtimevalsystem.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 核心估值服務
 * 1. 監聽市場價格
 * 2. 執行計算
 * 3. 將結果發布給訂閱者
 */
public class PortfolioValuationService implements MarketDataListener {

    // 靜態資料
    private final List<Position> positions;
    private final Map<String, Security> securityMap;
    private final OptionPricingService pricingService;

    // 動態資料
    private final Map<String, Double> currentPrices = new ConcurrentHashMap<>();
    private final Map<String, Double> currentStockPrices;

    private PortfolioResultListener resultListener;
    private final AtomicLong updateCounter = new AtomicLong(0); // 原子計數器

    public PortfolioValuationService(List<Position> positions,
                                     Map<String, Security> securityMap,
                                     Map<String, Double> initialStockPrices,
                                     OptionPricingService pricingService) {
        this.positions = positions;
        this.securityMap = securityMap;
        this.pricingService = pricingService;
        this.currentStockPrices = new ConcurrentHashMap<>(initialStockPrices);

        for (Position p : positions) {
            String ticker = p.getSymbol();
            currentPrices.put(ticker, currentStockPrices.getOrDefault(ticker, 0.0));
        }
    }

    public void setListener(PortfolioResultListener listener) {
        this.resultListener = listener;
    }

    @Override
    public void onStockPriceUpdate(String ticker, double newPrice) {
        currentStockPrices.put(ticker, newPrice);
        recalculatePortfolio(ticker, newPrice);
    }

    private void recalculatePortfolio(String updatedTicker, double updatedPrice) {
        double totalNAV = 0.0;
        List<CalculatedPosition> calculatedPositions = new ArrayList<>();

        // --- 1. 更新價格並計算價值 --------------------------------------------------------------------------------------
        for (Position pos : positions) {
            String ticker = pos.getSymbol();
            Security sec = securityMap.get(ticker);
            double newPrice = 0.0;
            String typeStr = "UNKNOWN";

            if (sec instanceof Stock) {
                typeStr = "STOCK";
                newPrice = currentStockPrices.getOrDefault(ticker, 0.0);
            } else if (sec instanceof EuropeanCallOption) {
                typeStr = "CALL";
                EuropeanCallOption call = (EuropeanCallOption) sec;
                double S = currentStockPrices.get(call.getUnderlyingTicker());
                newPrice = pricingService.calculateCallPrice(
                        S, call.getStrikePrice(), call.getSigma(), call.getTimeToMaturity()
                );
            } else if (sec instanceof EuropeanPutOption) {
                typeStr = "PUT";
                EuropeanPutOption put = (EuropeanPutOption) sec;
                double S = currentStockPrices.get(put.getUnderlyingTicker());
                newPrice = pricingService.calculatePutPrice(
                        S, put.getStrikePrice(), put.getSigma(), put.getTimeToMaturity()
                );
            }

            currentPrices.put(ticker, newPrice);

            long qty = pos.getPositionSize();
            double value = newPrice * qty;
            totalNAV += value;

            // --- 傳入 typeStr ---
            calculatedPositions.add(new CalculatedPosition(ticker, typeStr, newPrice, qty, value));
        }

        // --- 2. 發布結果給訂閱者 ----------------------------------------------------------------------------------------
        if (resultListener != null) {
            long currentUpdateNum = updateCounter.incrementAndGet();
            PortfolioUpdate update = new PortfolioUpdate(
                    currentUpdateNum,
                    updatedTicker,
                    updatedPrice,
                    calculatedPositions,
                    totalNAV
            );
            resultListener.onPortfolioUpdate(update);
        }
    }
}