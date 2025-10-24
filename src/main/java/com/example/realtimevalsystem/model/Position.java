package com.example.realtimevalsystem.model;

public class Position {
    private final String symbol;
    private final long positionSize; // 使用 long 來儲存數量 [cite: 41, 42]

    public Position(String symbol, long positionSize) {
        this.symbol = symbol;
        this.positionSize = positionSize;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getPositionSize() {
        return positionSize;
    }
}