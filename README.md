# RealTimeNavSystem (即時投資組合淨值系統)

這是一個 Java 應用程式，用於即時計算一個交易投資組合的淨值 (NAV)。

此系統根據一份程式挑戰文件實作，包含以下功能：
* 從 H2 嵌入式資料庫讀取證券定義 (股票、歐式買權、歐式賣權)。
* 從 CSV 檔案讀取投資組合的持倉。
* 實作一個模擬的市場數據發布者，在單獨的線程中運行。
* 使用離散時間幾何布朗運動 (GBM) 模擬股價變動。
* 使用 Black-Scholes 公式計算即時的選擇權價格。
* 在控制台(Console)即時刷新並「漂亮地印出」投資組合的價值、百分比變化與總 NAV。

---

## 需求 (Requirements)

* **JDK 1.8**
* **Gradle**

---

## 依賴的第三方函式庫 (Libraries)

根據挑戰文件允許的範圍：

* **H2 Database**: `2.1.214` (用於嵌入式證券資料庫)
* **Google Guava**: `33.2.1-jre` (用於 `Strings.repeat()`)
* **JUnit 5**: (用於單元測試)

---

## 如何建置 (How to Build)

1.  開啟終端機 (Terminal)。
2.  導航至專案的根目錄。
3.  執行 Gradle 包裝器 (wrapper) 來建置專案：

    ```bash
    # (Windows)
    .\gradlew.bat build

    # (macOS / Linux)
    ./gradlew build
    ```

---

## 如何運行 (How to Run)

有兩種方式可以運行此應用程式：

### 1. (推薦) 透過 IntelliJ IDEA

1.  打開 `src/main/java/com/example/realtimevalsystem/MainApplication.java` 檔案。
2.  點擊 `main` 方法旁邊的綠色「▶」播放按鈕。
3.  在 "Run" 視窗中查看即時儀表板。

### 2. 透過 Gradle

1.  (若尚未設定) 請在 `build.gradle.kts` 中加入 `application` 插件並指定 `mainClass`。
2.  在終端機中執行：

    ```bash
    # (Windows)
    .\gradlew.bat run

    # (macOS / Linux)
    ./gradlew run
    ```