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
            // --- 1. 載入靜態資料 ---

            // 初始化資料庫並載入證券定義
            SecurityDefinitionService dbService = new SecurityDefinitionService();
            dbService.initializeDatabase(); // 建立並插入資料
            Map<String, Security> securityMap = dbService.loadSecurities();
            System.out.println("資料庫載入完畢. 證券定義: " + securityMap.size() + " 筆");

            // 載入 CSV 持倉
            PositionLoader positionLoader = new PositionLoader();
            List<Position> positions = positionLoader.loadPositions("positions.csv");
            System.out.println("CSV 載入完畢. 持倉: " + positions.size() + " 筆");

            // --- 2. 準備服務 ---

            // 建立定價引擎
            OptionPricingService pricingService = new OptionPricingService();

            // 設定股票的 "初始價格"
            Map<String, Double> initialStockPrices = new HashMap<>();
            initialStockPrices.put("AAPL", 110.00); // 範例中的初始價格
            initialStockPrices.put("TELSA", 450.00); // 範例中的初始價格

            // 建立 "大腦" 估值服務
            PortfolioValuationService valuationService = new PortfolioValuationService(
                    positions,
                    securityMap,
                    initialStockPrices,
                    pricingService
            );

            // 建立 "訂閱者" (印出服務)
            ConsoleResultSubscriber subscriber = new ConsoleResultSubscriber();
            // 將 "訂閱者" 註冊到 "大腦"
            valuationService.setListener(subscriber);

            // 建立 "心臟" 市場發布者
            MarketDataPublisher publisher = new MarketDataPublisher(
                    securityMap,
                    initialStockPrices // 傳入初始價格 Map
            );

            // 關鍵：將 "大腦" 註冊為 "心臟" 的監聽器
            publisher.setListener(valuationService);

            // --- 3. 啟動系統 ---

            // [cite: 29] 將 publisher 放到一個新線程中運行
            Thread marketDataThread = new Thread(publisher);
            marketDataThread.setName("MarketDataThread");
            marketDataThread.start();

            System.out.println("===== 系統已啟動：等待市場數據更新... =====");

        } catch (SQLException e) {
            System.err.println("資料庫初始化失敗!");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("系統啟動失敗!");
            e.printStackTrace();
        }
    }
}