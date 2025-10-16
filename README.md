# AntiCheat - Minecraft 反外掛插件

一個專為 Minecraft 伺服器設計的高效反外掛插件，主要用於檢測和防止挖礦相關的外掛行為。

## 功能特色

### 🔍 智能檢測系統
- **挖礦軌跡檢測**: 檢測不自然的直線挖礦軌跡和規律性挖礦模式
- **挖礦速度檢測**: 監控異常的挖礦速度，防止速度外掛
- **多工具支持**: 支持各種挖礦工具的檢測
- **自適應閾值**: 根據配置靈活調整檢測敏感度

### 🚀 性能優化
- **低硬體負載**: 專為硬體較差的伺服器優化，避免頻繁的硬碟寫入
- **異步處理**: 所有檢測任務都在異步執行，不影響主線程
- **智能冷卻**: 防止重複通知，減少系統負載
- **記憶體管理**: 自動清理舊數據，避免記憶體洩漏

### 📢 通知系統
- **管理員通知**: 即時通知在線管理員
- **控制台日誌**: 詳細的檢測日誌記錄
- **距離過濾**: 只通知附近的管理員
- **Webhook 支持**: 預留 Discord/Slack 通知功能

### ⚙️ 靈活配置
- **白名單系統**: 支持玩家和世界白名單
- **權限控制**: 完整的權限管理系統
- **懲罰機制**: 可配置的自動懲罰系統
- **調試模式**: 詳細的調試信息輸出

## 安裝說明

### 系統需求
- Minecraft 1.20.4+
- Java 17+
- Spigot/Paper 伺服器

### 安裝步驟
1. 下載最新的 `AntiCheat.jar` 文件
2. 將文件放入伺服器的 `plugins` 資料夾
3. 重啟伺服器
4. 配置 `plugins/AntiCheat/config.yml` 文件
5. 使用 `/anticheat reload` 重新載入配置

## 配置說明

### 基本配置
```yaml
general:
  enabled: true                    # 是否啟用插件
  check-interval: 1000            # 檢測間隔 (毫秒)
  max-check-distance: 10          # 最大檢測距離
  debug: false                    # 調試模式
```

### 挖礦軌跡檢測
```yaml
mining-trace:
  enabled: true                   # 啟用挖礦軌跡檢測
  chunk-radius: 2                 # 軌跡檢測範圍
  max-trace-length: 50            # 最大軌跡長度
  sensitivity: 5                  # 檢測敏感度 (1-10)
  cooldown: 2000                  # 檢測冷卻時間
```

### 挖礦速度檢測
```yaml
mining-speed:
  enabled: true                   # 啟用挖礦速度檢測
  normal-speed-multiplier: 1.5    # 正常速度倍數
  detection-window: 5000          # 檢測時間窗口
  min-detection-count: 3          # 最小檢測次數
```

## 命令列表

| 命令 | 權限 | 描述 |
|------|------|------|
| `/anticheat reload` | `anticheat.admin` | 重新載入配置 |
| `/anticheat info` | `anticheat.admin` | 顯示插件信息 |
| `/anticheat stats` | `anticheat.admin` | 顯示統計信息 |
| `/anticheat help` | `anticheat.admin` | 顯示幫助信息 |

## 權限列表

| 權限 | 默認 | 描述 |
|------|------|------|
| `anticheat.admin` | OP | 管理員權限 |
| `anticheat.bypass` | false | 繞過檢測 |

## 編譯說明

### 使用 GitHub Actions
本項目使用 GitHub Actions 自動編譯，每次推送到主分支或創建 Release 時會自動構建。

### 本地編譯
```bash
# 克隆項目
git clone https://github.com/sunrisemc-tw/AntiCheat.git
cd AntiCheat

# 編譯項目
mvn clean package

# 編譯完成後，jar 文件位於 target/ 目錄
```

## 性能建議

### 硬體優化
- **檢測間隔**: 建議設置為 1000-2000ms，避免過於頻繁的檢測
- **軌跡長度**: 根據伺服器性能調整 `max-trace-length`
- **敏感度**: 從 5 開始調整，根據需要提高或降低

### 配置優化
- 啟用白名單功能，將信任的玩家加入白名單
- 使用世界白名單，在特定世界禁用檢測
- 適當調整通知範圍，減少不必要的通知

## 故障排除

### 常見問題
1. **插件無法載入**: 檢查 Java 版本是否為 17+
2. **檢測不準確**: 調整敏感度設置
3. **性能問題**: 增加檢測間隔，減少檢測範圍
4. **誤報過多**: 降低敏感度，增加白名單

### 調試模式
啟用 `debug: true` 可以獲得詳細的檢測日誌，幫助診斷問題。

## 更新日誌

### v1.0.0
- 初始版本發布
- 實現挖礦軌跡檢測
- 實現挖礦速度檢測
- 實現管理員通知系統
- 支持 GitHub Actions 自動編譯

## 貢獻指南

歡迎提交 Issue 和 Pull Request！

1. Fork 本項目
2. 創建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟 Pull Request

## 授權

本項目採用 Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0) 授權。

### 您可以：
- ✅ **分享** — 以任何媒介或格式複製和重新分發材料
- ✅ **改編** — 重新混合、轉換和基於材料進行構建

### 條件：
- 📝 **署名** — 您必須提供適當的署名，提供許可證鏈接，並說明是否進行了更改
- 🚫 **非商業性使用** — 您不得將材料用於商業目的

查看 [LICENSE](LICENSE) 文件了解完整詳情。

## 聯繫方式

- 項目地址: https://github.com/sunrisemc-tw/AntiCheat
- 問題回報: https://github.com/sunrisemc-tw/AntiCheat/issues

---

**注意**: 本插件專為 SunriseMC 伺服器設計，但歡迎其他伺服器使用和改進。
A minecraft plugin
