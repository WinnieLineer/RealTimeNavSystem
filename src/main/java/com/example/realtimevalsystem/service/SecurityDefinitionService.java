package com.example.realtimevalsystem.service;

import com.example.realtimevalsystem.model.EuropeanCallOption;
import com.example.realtimevalsystem.model.EuropeanPutOption;
import com.example.realtimevalsystem.model.Security;
import com.example.realtimevalsystem.model.Stock;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SecurityDefinitionService {

    // H2 in-memory, DB_CLOSE_DELAY=-1 能確保資料庫在所有連線關閉後不會消失
    private static final String DB_URL = "jdbc:h2:mem:security_db;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    /**
     * 初始化資料庫：建立 Schema 並插入範例資料
     */
    public void initializeDatabase() throws SQLException {
        createSchema();
        insertSampleData();
    }

    private void createSchema() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE SECURITIES (" +
                    "    ticker VARCHAR(255) PRIMARY KEY," +
                    "    type VARCHAR(10) NOT NULL," +          // 'STOCK', 'CALL', 'PUT'
                    "    underlying_ticker VARCHAR(255)," +     // 選擇權的標的
                    "    mu DOUBLE PRECISION," +                // 股票的預期報酬率
                    "    sigma DOUBLE PRECISION," +             // 波動率 (股票和選擇權都需要)
                    "    strike_price DOUBLE PRECISION," +      // 選擇權的履約價
                    "    time_to_maturity DOUBLE PRECISION" +   // 選擇權的到期時間(年)
                    ")";

            stmt.executeUpdate(sql);
        }
    }

    private void insertSampleData() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // 1. AAPL (Stock)
            stmt.executeUpdate("INSERT INTO SECURITIES (ticker, type, mu, sigma) " +
                    "VALUES ('AAPL', 'STOCK', 0.05, 0.20)"); // 假設 mu=5%, sigma=20%

            // 2. AAPL Call (AAPL-OCT-2020-110-C)
            stmt.executeUpdate("INSERT INTO SECURITIES (ticker, type, underlying_ticker, strike_price, time_to_maturity, sigma) " +
                    "VALUES ('AAPL-OCT-2020-110-C', 'CALL', 'AAPL', 110.0, 0.25, 0.20)"); // 假設剩 3 個月 (0.25年) 到期, sigma 繼承 AAPL

            // 3. AAPL Put (AAPL-OCT-2020-110-P)
            stmt.executeUpdate("INSERT INTO SECURITIES (ticker, type, underlying_ticker, strike_price, time_to_maturity, sigma) " +
                    "VALUES ('AAPL-OCT-2020-110-P', 'PUT', 'AAPL', 110.0, 0.25, 0.20)"); // 同上

            // 4. TELSA (Stock)
            stmt.executeUpdate("INSERT INTO SECURITIES (ticker, type, mu, sigma) " +
                    "VALUES ('TELSA', 'STOCK', 0.10, 0.40)"); // 假設 mu=10%, sigma=40%

            // 5. TELSA Call (TELSA-NOV-2020-400-C)
            stmt.executeUpdate("INSERT INTO SECURITIES (ticker, type, underlying_ticker, strike_price, time_to_maturity, sigma) " +
                    "VALUES ('TELSA-NOV-2020-400-C', 'CALL', 'TELSA', 400.0, 0.40, 0.40)"); // 假設剩 ~5 個月 (0.4年) 到期, sigma 繼承 TELSA

            // 6. TELSA Put (TELSA-DEC-2020-400-P)
            stmt.executeUpdate("INSERT INTO SECURITIES (ticker, type, underlying_ticker, strike_price, time_to_maturity, sigma) " +
                    "VALUES ('TELSA-DEC-2020-400-P', 'PUT', 'TELSA', 400.0, 0.50, 0.40)"); // 假設剩 6 個月 (0.5年) 到期, sigma 繼承 TELSA
        }
    }

    /**
     * 從資料庫讀取所有證券定義，並存入 Map
     * @return 一個 Map<String, Security>，Key 是 ticker，Value 是 Security 物件
     */
    public Map<String, Security> loadSecurities() throws SQLException {
        Map<String, Security> securities = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM SECURITIES")) {

            while (rs.next()) {
                String ticker = rs.getString("ticker");
                String type = rs.getString("type");

                switch (type) {
                    case "STOCK":
                        Stock stock = new Stock(
                                ticker,
                                rs.getDouble("mu"),
                                rs.getDouble("sigma")
                        );
                        securities.put(ticker, stock);
                        break;
                    case "CALL":
                        EuropeanCallOption call = new EuropeanCallOption(
                                ticker,
                                rs.getString("underlying_ticker"),
                                rs.getDouble("strike_price"),
                                rs.getDouble("time_to_maturity"),
                                rs.getDouble("sigma") //  sigma 來自標的股票
                        );
                        securities.put(ticker, call);
                        break;
                    case "PUT":
                        EuropeanPutOption put = new EuropeanPutOption(
                                ticker,
                                rs.getString("underlying_ticker"),
                                rs.getDouble("strike_price"),
                                rs.getDouble("time_to_maturity"),
                                rs.getDouble("sigma") // sigma 來自標的股票
                        );
                        securities.put(ticker, put);
                        break;
                }
            }
        }
        return securities;
    }
}