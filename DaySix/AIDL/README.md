# AIDL — ví dụ IPC quản lý sách

Project minh họa mục **3.2. AIDL** trong `AndroidTrainingExample/docs/android_basic_components.md`.

## Cấu trúc

| Module | Vai trò |
|---|---|
| `contract` | Android library chứa 3 file AIDL và `Book : Parcelable`, dùng chung cho hai app |
| `server` | App `com.example.server`, cung cấp `BookManagerService` trong process `:remote` |
| `client` | App `com.example.client`, bind bằng explicit ComponentName, gọi API và nhận callback |

AGP 9.1.1 dùng Kotlin tích hợp; không thêm `org.jetbrains.kotlin.android`. Compile/target SDK 36, min SDK 29, JDK 17 trở lên; có thể dùng JBR của Android Studio. Gradle wrapper 9.3.1. Không cần Compose hoặc plugin Parcelize: `Book` ghi/đọc Parcel thủ công để thấy cách truyền dữ liệu.

## Chạy

Mở thư mục `C:\Users\cuong.bui1\IdeaProjects\AIDL` bằng Android Studio và chờ Sync.

1. Chọn module `server`, Run để cài **AIDL Server**.
2. Chọn module `client`, Run **AIDL Client** trên cùng thiết bị (API 29+).
3. Client tự bind, đọc 2 sách mẫu và đăng ký callback. So sánh PID Client/Service: chúng khác nhau.
4. Giữ tên/tác giả mẫu hoặc nhập nội dung khác, bấm **Thêm sách qua AIDL**. Danh sách tăng một sách và khung callback hiển thị sách vừa thêm. Server tự cấp ID, giá demo 29.99.
5. Bấm **Ngắt kết nối**: nút đọc/thêm bị vô hiệu hóa. **Kết nối lại** tạo binding mới.
6. Nhấn Home hoặc chuyển app: Client unregister và unbind trong `onStop`; quay lại sẽ bind và đọc lại danh sách. Nếu không còn client nào giữ Service, dữ liệu RAM có thể reset.

Hai app phải ký cùng certificate để sử dụng permission `signature`; hai APK debug build trên cùng máy dùng chung debug keystore. Cài Server trước Client. Màn hình Server không cần mở để Service hoạt động; `BIND_AUTO_CREATE` tạo Service theo nhu cầu.

### Dùng dòng lệnh (PowerShell)

```powershell
cd C:\Users\cuong.bui1\IdeaProjects\AIDL
.\gradlew.bat :server:assembleDebug :client:assembleDebug
android install --apks=server\build\outputs\apk\debug\server-debug.apk
android install --apks=client\build\outputs\apk\debug\client-debug.apk
android run --apks=client\build\outputs\apk\debug\client-debug.apk --activity=com.example.client.ClientActivity
```

## Luồng xử lý

```text
ClientActivity → IBookManager.Proxy → Binder IPC → IBookManager.Stub → BookManagerService
Client listener ← Binder IPC ← IOnBookAddedListener (oneway) ← Server
```

- AIDL được bật ở `contract/build.gradle.kts`; hai app dùng `implementation(project(":contract"))`. Không sao chép contract sang hai nơi.
- `getBookList`, `addBook`, `getBookCount`, `getServerPid` là lời gọi đồng bộ. Client dùng worker FIFO để tránh chặn UI và giữ thứ tự register/request/unregister.
- Server khóa dữ liệu vì lời gọi đến từ Binder thread pool. `getBookList()` trả snapshot, không chia sẻ bộ nhớ mutable với Client.
- Server phát callback qua `RemoteCallbackList` trên main handler để tuần tự hóa `beginBroadcast/finishBroadcast`. Callback `oneway` không chờ Client xử lý xong.
- Listener Client nhận trên Binder thread rồi chuyển về UI thread. Mỗi phiên kết nối có listener riêng; callback cũ bị bỏ qua khi phiên đã đóng.
- Nếu Activity dừng khi register đang chạy, unregister được xếp sau register trên cùng worker. Việc unbind vẫn thực hiện ngay; lỗi khi Server đã chết được bắt.
- `onServiceDisconnected` giữ binding để hệ thống có thể kết nối lại; `onBindingDied`/`onNullBinding` giải phóng binding để người dùng thử lại.

## Kiểm tra

```powershell
.\gradlew.bat :server:assembleDebug :client:assembleDebug :server:lintDebug :client:lintDebug
# Cần emulator/device và cài Server trước:
.\gradlew.bat :server:installDebug
.\gradlew.bat :client:connectedDebugAndroidTest
```

4 bài kiểm tra trên thiết bị: Parcel round-trip; PID khác nhau và snapshot độc lập; callback và unregister; Server từ chối dữ liệu không hợp lệ. Các bài kiểm tra chạy IPC thật, không dùng mock Binder.

### Thử Server chết

Giữ Client đang hiển thị rồi dùng `adb shell am force-stop com.example.server`. Client phải thông báo mất kết nối và vô hiệu hóa thao tác gọi Service. Force-stop đặt app Server vào trạng thái stopped; mở **AIDL Server** để bỏ trạng thái này, quay lại Client và bấm **Kết nối lại**. PID Service đổi và dữ liệu trở về 2 sách mẫu. Không dùng `stopService()` để mô phỏng chết tiến trình khi Client vẫn bound.

## Giới hạn demo

Dữ liệu chỉ ở RAM, tối đa 100 sách; danh sách nhỏ để tránh truyền payload lớn qua Binder. App thực tế cần lưu database, phân trang và cân nhắc tương thích phiên bản contract. Bound Service ở ví dụ này không cần Foreground Service.

## Tài liệu chính thức

- [AIDL](https://developer.android.com/develop/background-work/services/aidl)
- [Bound services](https://developer.android.com/develop/background-work/services/bound-services)
- [RemoteCallbackList](https://developer.android.com/reference/android/os/RemoteCallbackList)

## Kết quả kiểm tra (05/10/2026)

Build hai APK debug và lintDebug đều đạt (lint không có lỗi). 4/4 kiểm thử IPC đạt trên Pixel_6, Android 15/API 35. Đã kiểm tra thao tác thêm sách/nhận callback, ngắt/kết nối lại và phục hồi sau force-stop Server.

Ảnh màn hình sau khi thêm sách: [Client demo](docs/client-demo.png).
