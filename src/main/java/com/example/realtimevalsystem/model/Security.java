package com.example.realtimevalsystem.model;

public interface Security {
    /**
     * 取得證券的唯一識別代碼 (e.g., "AAPL", "AAPL-OCT-2020-110-C")
     * @return ticker
     */
    String getTicker();
}