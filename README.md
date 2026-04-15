# MoneyPopup
![Supported server version](https://img.shields.io/badge/minecraft-1.21.1+-brightgreen)

Plugin hiển thị popup tiền bay lên phía trên đầu người chơi mỗi khi số dư thay đổi.  
Hỗ trợ cả tiền kinh tế qua Vault và điểm từ PlayerPoints, phù hợp cho server survival, minigame, farm, shop hoặc các plugin thưởng tiền theo sự kiện.

## Tính năng

- Hiển thị popup khi người chơi nhận hoặc mất tiền từ hệ thống kinh tế
- Hỗ trợ PlayerPoints như một nguồn giá trị riêng
- Gộp nhiều giao dịch liên tiếp thành một popup để tránh spam
- Tùy chỉnh định dạng hiển thị, màu sắc, đơn vị tiền tệ và ngưỡng tối thiểu
- Tự xóa ArmorStand còn sót khi plugin khởi động lại
- Hỗ trợ reload cấu hình bằng lệnh trong game

## Hướng dẫn sử dụng

**Cần có:**

- [Paper](https://papermc.io/) 1.21.1 trở lên
- [Vault](https://www.spigotmc.org/resources/vault.34315/)
- Một plugin economy tương thích Vault, ví dụ EssentialsX
- (Tùy chọn) [PlayerPoints](https://www.spigotmc.org/resources/playerpoints.80745/)

**Cài đặt:**

1. Build plugin bằng `mvn clean package`
2. Chép file jar trong thư mục `target/` vào `plugins/`
3. Khởi động server để plugin tạo `config.yml`
4. Chỉnh cấu hình theo nhu cầu
5. Dùng `/moneypopup reload` để tải lại cấu hình

## Danh sách lệnh

| Lệnh | Chức năng | Quyền |
|---|---|---|
| `/moneypopup reload` | Tải lại cấu hình plugin | `moneypopup.admin` |

> Alias: `/mpopup` và `/mp`

## Cấu hình

Plugin sử dụng một file `config.yml` với ba nhóm chính:

- `vault`: cấu hình popup cho hệ kinh tế qua Vault
- `playerpoints`: cấu hình popup cho PlayerPoints
- `display`: cấu hình thời gian hiển thị, độ cao bay và thời gian gộp popup

Ví dụ cấu hình:

```yaml
vault:
  enabled: true
  currency_name: "$"
  show_income: true
  show_expense: false
  income_format: "&a+%currency%%amount%"
  expense_format: "&c-%currency%%amount%"
  min_amount: 1

playerpoints:
  enabled: true
  currency_name: "Points"
  show_income: true
  show_expense: false
  income_format: "&6+%amount% %currency%"
  expense_format: "&c-%amount% %currency%"
  min_amount: 1

display:
  duration_ticks: 40
  float_height: 1.5
  offset_y: 2.0
  merge_window_ticks: 10
```

**Placeholder hỗ trợ:**

- `%amount%`: giá trị thay đổi
- `%currency%`: tên đơn vị hiển thị

## Ghi chú

- `show_expense: false` sẽ ẩn popup khi người chơi bị trừ tiền
- `min_amount` giúp bỏ qua các giao dịch quá nhỏ
- `merge_window_ticks` càng lớn thì popup càng dễ được gộp lại
- Đặt `merge_window_ticks: 0` nếu muốn mỗi giao dịch hiển thị riêng
