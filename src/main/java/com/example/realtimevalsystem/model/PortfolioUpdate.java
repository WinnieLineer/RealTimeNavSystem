package com.example.realtimevalsystem.model;

import java.util.List;

/**
 * [已升級] 代表一次完整的投資組合更新事件
 */
public class PortfolioUpdate {
    private final long updateNumber;
    private final String triggerTicker;
    private final double triggerPrice;
    private final List<CalculatedPosition> positions;
    private final double totalNAV;
    private final long timestamp; // <-- 新增

    public PortfolioUpdate(long updateNumber, String triggerTicker, double triggerPrice, List<CalculatedPosition> positions, double totalNAV) {
        this.updateNumber = updateNumber;
        this.triggerTicker = triggerTicker;
        this.triggerPrice = triggerPrice;
        this.positions = positions;
        this.totalNAV = totalNAV;
        this.timestamp = System.currentTimeMillis(); // <-- 新增: 自動記錄建立時間
    }

    public long getUpdateNumber() { return updateNumber; }
    public String getTriggerTicker() { return triggerTicker; }
    public double getTriggerPrice() { return triggerPrice; }
    public List<CalculatedPosition> getPositions() { return positions; }
    public double getTotalNAV() { return totalNAV; }
    public long getTimestamp() { return timestamp; } // <-- 新增
}