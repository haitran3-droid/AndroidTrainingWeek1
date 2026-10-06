# Bài 5 — Các adapter callbackFlow

Ứng dụng minh họa ba adapter có `awaitClose`: Firebase Realtime Database, Fused Location Provider và trạng thái mạng Android. Màn hình có thể chạy ngay để thử network/location.

Để thử Firebase, thêm `app/google-services.json` của Firebase project và cấu hình Google Services plugin theo project đó. Khi chưa có cấu hình, ứng dụng hiển thị hướng dẫn thay vì crash.

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

APK debug nằm tại `app/build/outputs/apk/debug/app-debug.apk`.
