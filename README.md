# MoneyPopup

Plugin Paper hiển thị popup tiền bay lên khi số dư thay đổi.

## Yêu cầu

- Java 21
- Paper 1.21.1 trở lên
- Vault
- EssentialsX
- PlayerPoints là tùy chọn

## Tính năng

- Hiển thị popup cho Vault và PlayerPoints
- Gộp nhiều thay đổi liên tiếp thành một popup
- Lệnh `/moneypopup reload`
- Xóa armor stand còn sót khi plugin bật lại

## Build

```bash
mvn clean package
```

File jar sau khi build nằm trong `target/`.

## Cấu hình

Chỉnh trong `src/main/resources/config.yml`.

Placeholder hỗ trợ:

- `%amount%`
- `%currency%`
