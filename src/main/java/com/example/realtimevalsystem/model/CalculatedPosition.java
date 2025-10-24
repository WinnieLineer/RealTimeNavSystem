package com.example.realtimevalsystem.model;

/**
 * [已升級] 代表一個已計算完畢的持倉價值
 */
public class CalculatedPosition {
    private final String symbol;
    private final String type; // <-- 新增
    private final double price;
    private final long qty;
    private final double value;

    public CalculatedPosition(String symbol, String type, double price, long qty, double value) {
        this.symbol = symbol;
        this.type = type; // <-- 新增
        this.price = price;
        this.qty = qty;
        this.value = value;
    }

    public String getSymbol() { return symbol; }
    public String getType() { return type; } // <-- 新增
    public double getPrice() { return price; }
    public long getQty() { return qty; }
    public double getValue() { return value; }
}