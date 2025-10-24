package com.example.realtimevalsystem.service;

import com.example.realtimevalsystem.model.PortfolioUpdate;

/**
 * 訂閱者介面，用於接收完整的投資組合更新
 */
public interface PortfolioResultListener {
    void onPortfolioUpdate(PortfolioUpdate update);
}