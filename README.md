<img width="1536" height="579" alt="moneypopup_logo" src="https://github.com/user-attachments/assets/7837f4da-7e26-4c0c-b1ca-6fa4f623bb29" />
# MoneyPopup

MoneyPopup là plugin Paper dùng để hiện popup tiền bay lên trên đầu người chơi khi số dư thay đổi.

Plugin này hỗ trợ 2 nguồn chính:
- tiền economy qua Vault
- điểm từ PlayerPoints

Nó phù hợp với các server survival, farm, shop hoặc minigame, nơi người chơi nhận tiền liên tục và cần một hiệu ứng nhìn trực quan hơn chat message thông thường.

## Plugin làm được gì?

- Hiện popup khi người chơi nhận tiền
- Có thể bật thêm popup khi người chơi bị trừ tiền
- Hỗ trợ PlayerPoints riêng, không gộp chung với Vault
- Gộp nhiều giao dịch gần nhau thành một popup để đỡ spam màn hình
- Tùy chỉnh màu, format hiển thị, đơn vị và mức tối thiểu để hiện popup
- Tự dọn ArmorStand còn sót nếu server restart không sạch

## Cần những gì?

- Java 21
- Paper 1.21.1 trở lên
- [Vault](https://www.spigotmc.org/resources/vault.34315/)
- một plugin economy dùng với Vault, ví dụ EssentialsX
- (tùy chọn) [PlayerPoints](https://www.spigotmc.org/resources/playerpoints.80745/)

## Cài nhanh

1. Chạy `mvn clean package`
2. Lấy file jar trong thư mục `target/`
3. Bỏ vào thư mục `plugins/` của server
4. Khởi động server một lần để plugin tạo file config
5. Chỉnh `config.yml` theo ý bạn
6. Dùng `/moneypopup reload` để tải lại cấu hình

## Lệnh

| Lệnh | Công dụng | Quyền |
|---|---|---|
| `/moneypopup reload` | Reload config | `moneypopup.admin` |

Alias có sẵn: `/mpopup`, `/mp`

## Cấu hình nhanh

Plugin dùng 1 file cấu hình là `config.yml`.

Ba phần chính trong file này:
- `vault`: cấu hình popup cho tiền economy
- `playerpoints`: cấu hình popup cho điểm PlayerPoints
- `display`: thời gian hiển thị, độ cao bay và thời gian gộp popup

Ví dụ:

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

Placeholder hỗ trợ:
- `%amount%`: số tiền hoặc số điểm thay đổi
- `%currency%`: tên đơn vị hiển thị

## Một vài lưu ý

- Nếu chỉ muốn hiện khi người chơi nhận tiền, để `show_expense: false`
- Nếu server có nhiều nguồn thưởng nhỏ liên tục, nên giữ `merge_window_ticks` lớn hơn 0 để tránh spam
- Nếu muốn mỗi giao dịch hiện riêng, đặt `merge_window_ticks: 0`
- `min_amount` giúp bỏ qua các thay đổi quá nhỏ
