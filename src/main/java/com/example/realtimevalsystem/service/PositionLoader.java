package com.example.realtimevalsystem.service;

import com.example.realtimevalsystem.model.Position;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PositionLoader {

    /**
     * 從 classpath 讀取持倉 CSV 檔案
     * @param fileName 位於 'resources' 資料夾中的檔案名稱 (e.g., "positions.csv")
     * @return Position 列表
     */
    public List<Position> loadPositions(String fileName) {
        List<Position> positions = new ArrayList<>();
        // 透過 ClassLoader 從 'resources' 資料夾讀取檔案
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);

        if (is == null) {
            System.err.println("錯誤：找不到檔案 " + fileName);
            return positions; // 返回空列表
        }

        try (InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            // 讀取標頭行並丟棄
            reader.readLine();

            // 讀取資料行
            while ((line = reader.readLine()) != null) {
                // 移除 " " 並用逗號分割
                String[] parts = line.replaceAll("\\s+", "").split(",");
                if (parts.length == 2) {
                    String symbol = parts[0];
                    long positionSize = Long.parseLong(parts[1]);
                    positions.add(new Position(symbol, positionSize));
                }
            }

        } catch (Exception e) {
            System.err.println("讀取持倉檔案時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }

        return positions;
    }
}