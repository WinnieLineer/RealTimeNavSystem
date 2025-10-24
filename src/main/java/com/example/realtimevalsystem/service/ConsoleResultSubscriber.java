package com.example.realtimevalsystem.service;

import com.example.realtimevalsystem.model.CalculatedPosition;
import com.example.realtimevalsystem.model.PortfolioUpdate;
import com.google.common.base.Strings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * [已升級] 儀表板風格的即時訂閱者
 * - 支援 ANSI 顏色
 * - 支援 Δ% 變化
 * - 支援畫面刷新 (Clear Screen)
 */
public class ConsoleResultSubscriber implements PortfolioResultListener {

    // --- ANSI 顏色代碼 ---
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_GRAY = "\u001B[90m"; // 灰色 (無變化)
    public static final String ANSI_CYAN = "\u001B[36m"; // 標題用
    public static final String ANSI_BOLD = "\u001B[1m";

    // --- 畫面刷新代碼 ---
    public static final String ANSI_CLEAR_SCREEN = "\033[H\033[2J";

    // --- 狀態儲存 ---
    private Map<String, Double> previousPrices = new HashMap<>();
    private double previousTotalNAV = 0.0;
    private final Object printLock = new Object();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    @Override
    public void onPortfolioUpdate(PortfolioUpdate update) {
        synchronized (printLock) {
            StringBuilder output = new StringBuilder();

            // 1. 清除畫面
            output.append(ANSI_CLEAR_SCREEN);

            String time = timeFormat.format(new Date(update.getTimestamp()));

            // 2. 印出標題
            output.append(ANSI_BOLD + ANSI_CYAN);
            output.append(String.format("================= Portfolio Update (%s | #%d | Trigger: %s) =================\n",
                    time, update.getUpdateNumber(), update.getTriggerTicker()));
            output.append(ANSI_RESET);

            // 3. 印出欄位標頭 (調整寬度)
            output.append(String.format("%-25s %-8s %10s %12s %15s %12s\n",
                    "Symbol", "Type", "Qty", "Price", "Market Value", "ΔChange"));
            output.append(Strings.repeat("-", 86)).append("\n"); // 調整分隔線長度

            // 4. 遍歷所有持倉
            Map<String, Double> newPrices = new HashMap<>();
            for (CalculatedPosition pos : update.getPositions()) {
                double newPrice = pos.getPrice();
                double oldPrice = previousPrices.getOrDefault(pos.getSymbol(), newPrice);

                double pctChange = 0.0;
                if (oldPrice != 0.0) {
                    pctChange = (newPrice - oldPrice) / oldPrice;
                }

                String changeStr = formatChange(pctChange);
                String color = getColor(pctChange);

                // --- [修改] 提高價格精度 (%.4f) 並調整欄寬 ---
                output.append(String.format("%s%-25s %-8s %10d %12.4f %,15.2f %12s%s\n",
                        color,
                        pos.getSymbol(),
                        pos.getType(),
                        pos.getQty(),
                        newPrice,     // 價格
                        pos.getValue(),
                        changeStr,    // ΔChange
                        ANSI_RESET));

                newPrices.put(pos.getSymbol(), newPrice);
            }

            output.append(Strings.repeat("-", 86)).append("\n"); // 調整分隔線長度

            // 5. 印出總 NAV
            double navPctChange = 0.0;
            if (previousTotalNAV != 0.0) {
                navPctChange = (update.getTotalNAV() - previousTotalNAV) / previousTotalNAV;
            }
            String navChangeStr = formatChange(navPctChange);
            String navColor = getColor(navPctChange);

            // --- [修改] 調整欄寬 ---
            output.append(String.format("%-25s %-8s %10s %12s %s%,15.2f %12s%s\n",
                    ANSI_BOLD + "Total Portfolio NAV:", "", "", "",
                    navColor + ANSI_BOLD,
                    update.getTotalNAV(),
                    navChangeStr,
                    ANSI_RESET));

            // --- [修改] 調整分隔線長度 ---
            output.append(ANSI_CYAN + Strings.repeat("=", 86) + "\n" + ANSI_RESET);

            // 6. 更新狀態
            this.previousPrices = newPrices;
            this.previousTotalNAV = update.getTotalNAV();

            // 7. 一次性印出
            System.out.print(output.toString());
            System.out.flush();
        }
    }

    /**
     * 輔助方法：根據變化百分比回傳 ANSI 顏色
     */
    private String getColor(double pctChange) {
        // --- [修改] 降低門檻以捕捉更小的變化 ---
        if (pctChange > 0.000001) {
            return ANSI_GREEN;
        } else if (pctChange < -0.000001) {
            return ANSI_RED;
        }
        return ANSI_GRAY; // 無變化用灰色
    }

    /**
     * 輔助方法：格式化百分比字串
     */
    private String formatChange(double pctChange) {
        // --- [修改] 降低門檻並提高顯示精度 ---
        if (Math.abs(pctChange) < 0.000001) {
            return "0.00%"; // 顯示 2 位小數
        }
        String symbol = (pctChange > 0) ? "▲ +" : "▼ ";
        // --- [修改] 提高顯示精度 ---
        return String.format("%s%.2f%%", symbol, Math.abs(pctChange * 100));
    }
}