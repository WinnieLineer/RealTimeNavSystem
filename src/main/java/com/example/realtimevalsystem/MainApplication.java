package com.example.realtimevalsystem;

import com.example.realtimevalsystem.model.Position;
import com.example.realtimevalsystem.model.Security;
import com.example.realtimevalsystem.service.MarketDataPublisher;
import com.example.realtimevalsystem.service.OptionPricingService;
import com.example.realtimevalsystem.service.PortfolioValuationService;
import com.example.realtimevalsystem.service.PositionLoader;
import com.example.realtimevalsystem.service.SecurityDefinitionService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.realtimevalsystem.service.ConsoleResultSubscriber;
public class MainApplication {

    public static void main(String[] args) {
        System.out.println("系統啟動中...");

        try {
            // --- 1. 載入靜態資料 ----------------------------------------------------------------------------------------
            // 初始化資料庫並載入證券定義
            SecurityDefinitionService dbService = new SecurityDefinitionService();
            dbService.initializeDatabase(); // 建立並插入資料
            Map<String, Security> securityMap = dbService.loadSecurities();
            System.out.println("資料庫載入完畢. 證券定義: " + securityMap.size() + " 筆");

            // 載入 CSV 持倉
            PositionLoader positionLoader = new PositionLoader();
            List<Position> positions = positionLoader.loadPositions("positions.csv");
            System.out.println("CSV 載入完畢. 持倉: " + positions.size() + " 筆");


            // --- 2. 準備服務 -------------------------------------------------------------------------------------------
            // 建立定價引擎
            Thread marketDataThread = getThread(positions, securityMap);
            marketDataThread.start();

            System.out.println("========================= 系統已啟動：等待市場數據更新...  ===============================");

        } catch (SQLException e) {
            System.err.println("資料庫初始化失敗!");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("系統啟動失敗!");
            e.printStackTrace();
        }
    }

    private static Thread getThread(List<Position> positions, Map<String, Security> securityMap) {
        OptionPricingService pricingService = new OptionPricingService();

        // 設定股票的 "初始價格"
        Map<String, Double> initialStockPrices = new HashMap<>();
        initialStockPrices.put("AAPL", 110.00);  // 範例中的初始價格
        initialStockPrices.put("TELSA", 450.00); // 範例中的初始價格

        // valuation engine
        PortfolioValuationService valuationService = new PortfolioValuationService(
            positions,
            securityMap,
            initialStockPrices,
            pricingService
        );

        // 建立 "訂閱者" (印出服務)
        ConsoleResultSubscriber subscriber = new ConsoleResultSubscriber();

        // 將 "訂閱者" 註冊到 "valuation engine"
        valuationService.setListener(subscriber);

        // 建立市場發布者
        MarketDataPublisher publisher = new MarketDataPublisher(
            securityMap,
            initialStockPrices
        );

        publisher.setListener(valuationService);

        // --- 3. 啟動系統 -----------------------------------------------------------------------------------------------
        Thread marketDataThread = new Thread(publisher);
        marketDataThread.setName("MarketDataThread");
        return marketDataThread;
    }
}